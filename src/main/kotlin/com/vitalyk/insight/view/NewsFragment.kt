package com.vitalyk.insight.view

import com.vitalyk.insight.helpers.bindVisibleAndManaged
import com.vitalyk.insight.helpers.browseTo
import com.vitalyk.insight.helpers.newYorkTimeZone
import com.vitalyk.insight.ui.symbolfield
import com.vitalyk.insight.yahoo.NewsItem
import com.vitalyk.insight.yahoo.fetchNews
import com.vitalyk.insight.yahoo.marketIndexes
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.input.Clipboard
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import tornadofx.*
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant

class NewsFragment : Fragment("News") {

    private val newsItems = FXCollections.observableArrayList<NewsItem>()

    // Last time symbol's news have been fetched.
    private val fetchTimes = mutableMapOf<String, Instant>()
    // The news fetched last time itself.
    private val cache = mutableMapOf<String, List<NewsItem>>()
    private val cacheKeepTime = 30_000 // ms
    private var cacheInvalidationJob: Job? = null

    var symbol = SimpleStringProperty("").apply {
        onChange {
            if (it != null && !isFetching)
                root.fetchSymbolNews(it)
        }
    }
    private val dateFormatter = SimpleDateFormat("dd MMM HH:mm:ss zzz").apply {
        timeZone = newYorkTimeZone
    }

    val toolbarVisible = SimpleBooleanProperty(true)

    override val root = vbox {
        vgrow = Priority.ALWAYS
        toolbar {
            label("Symbol:")
            symbolfield { fetchSymbolNews(it) }

            marketIndexes.forEach { symbol, name ->
                button(name) {
                    action {
                        fetchSymbolNews(symbol)
                    }
                }
            }

            bindVisibleAndManaged(toolbarVisible)
        }
        listview(newsItems) {
            vgrow = Priority.ALWAYS

            cellFormat { item ->
                graphic = hbox {
                    alignment = Pos.CENTER_LEFT
                    text(dateFormatter.format(item.date)) {
                        font = Font.font("Monaco, Menlo, Courier", 12.0)
                        fill = Color(0.3, 0.3, 0.3, 1.0)
                    }
                    hyperlink(item.headline) {
                        tooltip(item.url)
                        action {
                            browseTo(item.url)
                        }
                        contextmenu {
                            item("Open in browser").action {
                                browseTo(item.url)
                            }
                            item("Copy link").action {
                                val clipboard = Clipboard.getSystemClipboard()
                                clipboard.putString(item.url)
                            }
                        }
                    }
                }
            }
        }
    }

    private var isFetching = false

    private fun Node.fetchSymbolNews(symbol: String) {
        if (isFetching) return

        val now = Instant.now()
        val lastTime = fetchTimes[symbol]
        if (lastTime != null && Duration.between(lastTime, now).toMillis() < cacheKeepTime) {
            newsItems.setAll(cache[symbol])
            return
        }

        isFetching = true
        fetchTimes[symbol] = now

        runAsyncWithProgress {
            fetchNews(symbol)
        } ui {
            cache[symbol] = it
            newsItems.setAll(it)
            isDisable = false
            isFetching = false

            // If `cacheKeepTime` since last fetch, then all of the cached items
            // are past their expiration.
            cacheInvalidationJob?.cancel()
            cacheInvalidationJob = launch {
                delay(cacheKeepTime)
                cache.clear()
                fetchTimes.clear()
            }
        }
    }
}