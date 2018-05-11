package com.vitalyk.insight.reuters

import com.vitalyk.insight.helpers.objectMapper
import com.vitalyk.insight.main.httpGet
import com.vitalyk.insight.trigger.TextTrigger
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

data class Headline(
    val id: String,
    val headline: String,
    val dateMillis: String,
    val formattedDate: String,
    val url: String,
    val mainPicUrl: String
)

data class HeadlineAlert(
    val date: Date,
    val headline: Headline,
    val trigger: TextTrigger
)

typealias HeadlineListener = (headlines: List<Headline>) -> Unit
typealias AlertListener = (alerts: List<HeadlineAlert>) -> Unit

object ReutersWire {
    private val mutex = this

    data class State(
        val triggers: List<TextTrigger>,
        val alerts: List<HeadlineAlert>
    )

    private val logger = LoggerFactory.getLogger(javaClass)
    private val updateInterval = 10_000 // as on reuters.com
    const val baseUrl = "https://www.reuters.com"
    private const val url = "$baseUrl/assets/jsonWireNews"

    private data class Response(
        val headlines: List<Headline>
    )

    fun saveState(): State = State(
        triggers = triggers,
        // Not using `alerts` property, as it returns a reversed copy.
        // Would have to reverse on load as well.
        // Instead, save and restore alerts in natural order.
        alerts = _alerts.values.toList()
    )

    fun loadState(state: State?) {
        if (state != null) {
            val triggers = _triggers
            triggers.clear()
            triggers.addAll(state.triggers)

            val alerts = _alerts
            alerts.clear()
            state.alerts.forEach { alert ->
                alerts[alert.headline.headline] = alert
            }
        }
    }

    private val headlineListeners = mutableSetOf<HeadlineListener>()

    fun addHeadlineListener(listener: HeadlineListener) {
        headlineListeners.add(listener)
        startFetching()
    }

    fun removeHeadlineListener(listener: HeadlineListener) {
        headlineListeners.remove(listener)
        stopFetching()
    }


    private val alertListeners = mutableListOf<AlertListener>()

    fun addAlertListener(listener: AlertListener) {
        alertListeners.add(listener)
        startFetching()
    }

    fun removeAlertListener(listener: AlertListener) {
        alertListeners.remove(listener)
        stopFetching()
    }

    private val _triggers = mutableListOf<TextTrigger>()
    val triggers get() = _triggers.toList()

    fun addTrigger(trigger: TextTrigger) {
        _triggers.add(trigger)
    }

    fun removeTrigger(trigger: TextTrigger) {
        _triggers.remove(trigger)
    }

    // Same stories can reappear in headlines with more updates.
    // To prevent alerting the user repeatedly, we keep an ordered map
    // of headlines to recently triggered alerts. This also allows us
    // to know at what time the news first surfaced.
    // If a trigger is recurring, it will result in an alert,
    // even if the headline is already in the cache, but this will
    // not violate the order of cache items.
    private const val alertCacheSize = 100
    private val _alerts = linkedMapOf<String, HeadlineAlert>()

    val alerts get() = _alerts.values.reversed()

    fun clearAlerts() {
        // TODO: read more about mutexes and concurrency in Java
        // What happens if the map is cleared from another (e.g. UI) thread
        // when the `fetch` is running in a coroutine?
//        synchronized(mutex) {
            _alerts.clear()
//        }
    }

    fun clearTriggers() {
        _triggers.clear()
    }

    private fun fetch() {
        try {
            val responseText = httpGet(url)
            val response = objectMapper.readValue(responseText, Response::class.java)
            val headlines = response.headlines

            // Make a local copy of triggers before iteration for thread safety.
            val triggers = _triggers.toList()
            val newAlerts = mutableListOf<HeadlineAlert>()
            headlines.forEach { headline ->
                triggers.forEach {
                    val text = headline.headline
                    // Alerts only trigger for the same text once (first time).
                    if ((it.recurring || text !in _alerts) && it.check(text)) {
                        // TODO: remove older alerts and keep newer ones, while keeping no more than alertCacheSize
                        if (_alerts.size >= alertCacheSize) _alerts.clear()
                        val alert = HeadlineAlert(Date(), headline, it)
                        _alerts[text] = alert
                        newAlerts.add(alert)
                    }
                }
            }

            if (headlines.isNotEmpty())
                headlineListeners.forEach { it(headlines) }

            if (newAlerts.isNotEmpty())
                alertListeners.forEach { it(newAlerts) }

        } catch (e: IOException) {
            logger.warn("Fetching $url failed.")
        }
    }

    var fetchJob: Job? = null

    private fun startFetching() {
        val job = fetchJob
        if (job == null || !job.isActive) {
            fetchJob = launch {
                while (isActive && headlineListeners.isNotEmpty()) {
                    fetch()
                    delay(updateInterval)
                }
            }
        }
    }

    private fun stopFetching() {
        if (headlineListeners.isEmpty() && alertListeners.isEmpty()) {
            fetchJob?.cancel()
        }
    }
}