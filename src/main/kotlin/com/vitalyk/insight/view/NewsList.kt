package com.vitalyk.insight.view

import com.vitalyk.insight.ui.symbolfield
import com.vitalyk.insight.ui.toolbox
import com.vitalyk.insight.yahoo.NewsItem
import com.vitalyk.insight.yahoo.fetchNews
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.input.Clipboard
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*
import java.awt.Desktop
import java.net.URI
import java.text.SimpleDateFormat

class NewsList : Fragment("News") {

    var symbol = SimpleStringProperty("AAPL")
    val dateFormatter = SimpleDateFormat("dd MMM HH:mm:ss zzz")

    val listview: ListView<NewsItem> = listview {
        vgrow = Priority.ALWAYS

        cellCache {
            val item = it
            hbox {
                alignment = Pos.CENTER_LEFT
                text(dateFormatter.format(item.date)) {
                    font = Font.font("Monaco, Menlo, Courier", 12.0)
                    fill = Color(0.3, 0.3, 0.3, 1.0)
                }
                hyperlink(item.headline) {
                    tooltip(item.url)
                    setOnAction {
                        Desktop.getDesktop().browse(URI(item.url))
                    }
                    contextmenu {
                        item("Open in browser").action {
                            Desktop.getDesktop().browse(URI(item.url))
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

    val toolbox = toolbox(border = false) {
        label("Symbol:")
        val symbol = SimpleStringProperty("")
        symbolfield(symbol) {
            onKeyReleased = EventHandler { key ->
                if (key.code == KeyCode.ENTER) {
                    fetchSymbolNews(text)
                }
            }
        }
    }

    init {
        symbol.onChange {
            if (it != null && !isFetching)
                root.fetchSymbolNews(it)
        }
    }

    override val root = vbox {
        this += toolbox
        this += listview
    }

    private var isFetching = false

    private fun Node.fetchSymbolNews(symbol: String) {
        if (isFetching) return

        isFetching = true
        runAsyncWithProgress {
            fetchNews(symbol)
        } ui {
            listview.items = it.observable()
            isDisable = false
            isFetching = false
        }
    }
}