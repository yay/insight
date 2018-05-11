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

    private val alerts = mutableMapOf<String, ReutersHeadlineAlert>()

    private fun fetch() {
        try {
            val responseText = httpGet(url)
            val response = objectMapper.readValue(responseText, Response::class.java)
            val headlines = response.headlines

            headlines.forEach { headline ->
                triggers.forEach {
                    val text = headline.headline
                    // Alerts only trigger for the same text once (first time).
                    if (text !in alerts && it.check(text)) {
                        alerts[text] = ReutersHeadlineAlert(Date(), headline, it)
                    }
                }
            }

            listeners.forEach { it(headlines, alerts.values.toList()) }
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