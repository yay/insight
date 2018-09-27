package com.vitalyk.insight.reuters

import com.fasterxml.jackson.annotation.JsonProperty
import com.vitalyk.insight.helpers.objectMapper
import com.vitalyk.insight.main.httpGet
import com.vitalyk.insight.trigger.TextTrigger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

data class Story(
    val id: String,
    val headline: String,
    @JsonProperty("dateMillis")
    val date: Date,
    val formattedDate: String,
    val url: String,
    val mainPicUrl: String
)

data class StoryAlert(
    val date: Date,
    val story: Story
)

// Receives an updated list of latest stories.
typealias StoryListener = (stories: List<Story>) -> Unit

// Receives a set of alerts for new stories that satisfied triggers on the latest update.
// Because it's a set, one can easily check if the alert is a new one
// or from previous updates.
typealias AlertListener = (alerts: Set<StoryAlert>) -> Unit

object ReutersWire {
//    private val mutex = this

    data class State(
        val triggers: List<TextTrigger>,
        val alerts: List<StoryAlert>
    )

    private val logger = LoggerFactory.getLogger(javaClass)
    private val updateInterval = 10_000 // as on reuters.com
    const val baseUrl = "https://www.reuters.com"
    private const val url = "$baseUrl/assets/jsonWireNews"

    private data class Response(
        val headlines: List<Story>
    )

    fun saveState(): State = State(
        triggers = triggers,
        alerts = alerts
    )

    fun loadState(state: State?) {
        if (state != null) {
            val triggers = _triggers
            triggers.clear()
            triggers.addAll(state.triggers)

            val alerts = _alerts
            alerts.clear()
            state.alerts.forEach { alert ->
                alerts[alert.story.headline] = alert
            }
        }
    }

    private val storyListeners = mutableSetOf<StoryListener>()

    fun addStoryListener(listener: StoryListener) {
        storyListeners.add(listener)
        startFetching()
    }

    fun removeStoryListener(listener: StoryListener) {
        storyListeners.remove(listener)
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

    // Same stories can reappear with more updates.
    // To prevent alerting the user repeatedly, we keep a map
    // of stories to recently triggered alerts.
    private const val alertCacheSize = 100
    private val _alerts = mutableMapOf<String, StoryAlert>()

    val alerts get() = _alerts.values.sortedByDescending { it.date }

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
            val stories = response.headlines

            // Make a local copy of triggers before iteration for thread safety.
            val triggers = _triggers.toList()
            val alerts = if (alertListeners.isNotEmpty()) linkedSetOf<StoryAlert>() else null

            stories.forEach { story ->
                triggers.forEach { trigger ->
                    val text = story.headline
                    val isNewStory = text !in _alerts
                    val isStoryUpdate = _alerts[text]?.let {
                        it.story.date != story.date
                    } ?: false
                    if ((isNewStory || isStoryUpdate) && trigger.matches(text)) {
                        if (_alerts.size >= alertCacheSize) _alerts.clear()
                        val alert = StoryAlert(Date(), story)
                        _alerts[text] = alert
                        alerts?.add(alert)
                    }
                }
            }

            if (stories.isNotEmpty())
                storyListeners.forEach { it(stories) }

            if (alerts?.isNotEmpty() == true)
                alertListeners.forEach { it(alerts) }

        } catch (e: IOException) {
            logger.warn("Fetching $url failed.")
        }
    }

    var fetchJob: Job? = null

    private fun startFetching() {
        val job = fetchJob
        if (job == null || !job.isActive) {
            fetchJob = launch {
                while (isActive && storyListeners.isNotEmpty()) {
                    fetch()
                    delay(updateInterval)
                }
            }
        }
    }

    private fun stopFetching() {
        if (storyListeners.isEmpty() && alertListeners.isEmpty()) {
            fetchJob?.cancel()
        }
    }
}