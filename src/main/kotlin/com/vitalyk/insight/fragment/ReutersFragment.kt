package com.vitalyk.insight.fragment

import com.vitalyk.insight.helpers.browseTo
import com.vitalyk.insight.helpers.objectMapper
import com.vitalyk.insight.main.appLogger
import com.vitalyk.insight.main.httpGet
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import tornadofx.*
import java.io.IOException

data class ReutersHeadline(
    val id: String,
    val headline: String,
    val dateMillis: String,
    val formattedDate: String,
    val url: String,
    val mainPicUrl: String
)

typealias ReutersWireListener = (headlines: List<ReutersHeadline>) -> Unit

object ReutersWire {
    private val updateInterval = 10_000 // as on reuters.com
    private val url = "https://www.reuters.com/assets/jsonWireNews"

    private val listeners = mutableSetOf<ReutersWireListener>()

    data class Headlines(
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

    private fun fetch() {
        try {
            val responseText = httpGet(url)
            val headlines = objectMapper.readValue(responseText, Headlines::class.java)
            listeners.forEach { it(headlines.headlines) }
        } catch (e: IOException) {
            appLogger.warn("Fetching $url failed.")
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

// Non-UI Reuters news fetcher singleton
// https://www.reuters.com/assets/jsonWireNews?startTime=1525694056000

class ReutersFragment : Fragment("Reuters Wire") {
    val listview: ListView<ReutersHeadline> = listview {
        val listview = this
        vgrow = Priority.ALWAYS
        minWidth = 200.0

        cellCache { headline ->
            vbox {
                label(headline.formattedDate) {
                    textFill = Color.GRAY
                }
                label(headline.headline) {
                    textFill = Color.BLACK
                    isWrapText = true
                    style {
                        font = Font.font("Tahoma", 9.0)
                        fontWeight = FontWeight.BOLD
                    }
                    prefWidthProperty().bind(listview.widthProperty().subtract(36))
                }
            }
        }

        onUserSelect {
            browseTo("https://www.reuters.com" + it.url)
        }
    }

    override val root = vbox {
        this += listview
    }

    init {
        ReutersWire.addListener { headlines ->
            runLater {
                listview.items = headlines.observable()
            }
        }
    }
}