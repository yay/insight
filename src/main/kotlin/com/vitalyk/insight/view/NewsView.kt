package com.vitalyk.insight.view

import com.vitalyk.insight.ui.toolbox
import com.vitalyk.insight.yahoo.NewsItem
import com.vitalyk.insight.yahoo.fetchNews
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.control.TabPane
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*
import java.awt.Desktop
import java.net.URI
import java.text.SimpleDateFormat


class NewsView : View("Headlines") {

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
                }
            }
        }
    }

    override fun onDock() {
    }

    override fun onUndock() {
    }

    override val root = vbox {
        toolbox {
            button("Back") {
                setOnAction {
                    replaceWith(SymbolTableView::class)
                }
            }
        }

        tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            vgrow = Priority.ALWAYS

            tab("News") {
                vbox {
                    toolbox {
                        label("Symbol:")
                        textfield(symbol) {
                            textProperty().onChange { value ->
                                text = value?.toUpperCase()
                            }
                            onKeyReleased = EventHandler { key ->
                                if (key.code == KeyCode.ENTER) {
                                    fetchSymbolNews()
                                }
                            }
                        }
                    }

                    this += listview
                }
            }
        }
    }

    private fun Node.fetchSymbolNews() {
        listview.items.clear()

        runAsyncWithProgress {
            fetchNews(symbol.value)
        } ui {
            listview.items = it.observable()
            isDisable = false
        }
    }
}