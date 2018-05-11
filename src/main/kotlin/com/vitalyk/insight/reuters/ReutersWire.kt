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

data class ReutersHeadline(
    val id: String,
    val headline: String,
    val dateMillis: String,
    val formattedDate: String,
    val url: String,
    val mainPicUrl: String
)

data class ReutersHeadlineAlert(
    val date: Date,
    val headline: ReutersHeadline,
    val trigger: TextTrigger
)

typealias ReutersWireListener = (headlines: List<ReutersHeadline>, alerts: List<ReutersHeadlineAlert>) -> Unit

object ReutersWire {
    val mutex = this

    private val logger = LoggerFactory.getLogger(javaClass)
    private val updateInterval = 10_000 // as on reuters.com
    private val url = "https://www.reuters.com/assets/jsonWireNews"

    private val listeners = mutableSetOf<ReutersWireListener>()

    private data class Response(
        val headlines: List<ReutersHeadline>
    )

    fun addListener(listener: ReutersWireListener) {
        listeners.add(listener)
        val job = fetchJob
        if (job == null || !job.isActive) {
            start()
        }
    }

    fun removeListener(listener: ReutersWireListener) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            stop()
        }
    }

    val triggers = mutableListOf<TextTrigger>()

    fun addTrigger(trigger: TextTrigger) {
        triggers.add(trigger)
    }

    fun removeTrigger(trigger: TextTrigger) {
        triggers.remove(trigger)
    }

    private const val alertCacheSize = 100
    private val alertMap = mutableMapOf<String, ReutersHeadlineAlert>()

    val alerts get() = alertMap.values.toList()

    fun clearAlerts() {
        // TODO: read more about mutexes and concurrency in Java
        // What happens if the map is cleared from another (e.g. UI) thread
        // when the `fetch` is running in a coroutine?
        synchronized(mutex) {
            alertMap.clear()
        }
    }

    private fun fetch() {
        try {
            val responseText = httpGet(url)
            val response = objectMapper.readValue(responseText, Response::class.java)
            val headlines = response.headlines

            // Make a local copy of triggers before iteration for thread safety.
            val triggers = triggers.toList()
            val newAlerts = mutableListOf<ReutersHeadlineAlert>()
            headlines.forEach { headline ->
                triggers.forEach {
                    val text = headline.headline
                    // Alerts only trigger for the same text once (first time).
                    if (text !in alertMap && it.check(text)) {
                        // TODO: remove older alerts and keep newer ones, while keeping no more than alertCacheSize
                        if (alertMap.size >= alertCacheSize) alertMap.clear()
                        val alert = ReutersHeadlineAlert(Date(), headline, it)
                        alertMap[text] = alert
                        newAlerts.add(alert)
                    }
                }
            }

            listeners.forEach { it(headlines, newAlerts) }
        } catch (e: IOException) {
            logger.warn("Fetching $url failed.")
        }
    }

    var fetchJob: Job? = null

    private fun start() {
        fetchJob = launch {
            while (isActive && listeners.isNotEmpty()) {
                fetch()
                delay(updateInterval)
            }
        }
    }

    private fun stop() {
        fetchJob?.cancel()
    }
}