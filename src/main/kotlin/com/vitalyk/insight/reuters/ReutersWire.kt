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

    private val logger = LoggerFactory.getLogger(javaClass)
    private val updateInterval = 10_000 // as on reuters.com
    private val url = "https://www.reuters.com/assets/jsonWireNews"

    private data class Response(
        val headlines: List<Headline>
    )


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

    private val triggerList = mutableListOf<TextTrigger>()
    val triggers get() = triggerList.toList()

    fun addTrigger(trigger: TextTrigger) {
        triggerList.add(trigger)
    }

    fun removeTrigger(trigger: TextTrigger) {
        triggerList.remove(trigger)
    }

    private const val alertCacheSize = 100
    private val alertMap = linkedMapOf<String, HeadlineAlert>()

    val alerts get() = alertMap.values.reversed()

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
            val triggers = triggerList.toList()
            val newAlerts = mutableListOf<HeadlineAlert>()
            headlines.forEach { headline ->
                triggers.forEach {
                    val text = headline.headline
                    // Alerts only trigger for the same text once (first time).
                    if (text !in alertMap && it.check(text)) {
                        // TODO: remove older alerts and keep newer ones, while keeping no more than alertCacheSize
                        if (alertMap.size >= alertCacheSize) alertMap.clear()
                        val alert = HeadlineAlert(Date(), headline, it)
                        alertMap[text] = alert
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