package com.vitalyk.insight.view

import com.vitalyk.insight.ui.toolbox
import com.vitalyk.insight.yahoo.NewsItem
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import org.jsoup.Jsoup
import tornadofx.*
import java.awt.Desktop
import java.net.URI


class NewsView : View() {

    var symbol = SimpleStringProperty("AAPL")
    val timeout: Int = 10000
    lateinit var listview: ListView<NewsItem>
    lateinit var browserTab: Tab
    val storyWebView = webview {
        vgrow = Priority.ALWAYS
    }

    // https://developer.yahoo.com/finance/company.html

    val baseUrl = "http://finance.yahoo.com"
    val ipoUrl = "$baseUrl/news/category-ipos/?bypass=true"
    val companyUrl = "$baseUrl/rss/headline?s="
    val upgradesUrl = "$baseUrl/news/category-upgrades-and-downgrades/?bypass=true"

    override fun onDock() {
    }

    override fun onUndock() {
    }

    override val root = vbox()

    init {
        with(root) {

            toolbox {
                button("Back") {
                    setOnAction {
                        replaceWith(SymbolTableView::class)
                    }
                }
            }

            val tabPane = tabpane {

                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                vgrow = Priority.ALWAYS

                tab("News") {
                    vbox {
                        toolbox {
                            label("Symbol:")
                            textfield(symbol) {
                                textProperty().onChange { value ->
                                    this.text = value?.toUpperCase()
                                }
                                onKeyReleased = EventHandler { key ->
                                    if (key.code == KeyCode.ENTER) {

                                        listview.items.clear()
                                        var code = 0
                                        val list = mutableListOf<NewsItem>()
                                        val url = companyUrl + symbol.value
                                        val connection = Jsoup.connect(url).timeout(timeout)

                                        runAsyncWithProgress {
                                            val doc = connection.get()
                                            code = connection.data().response().statusCode()
                                            if (code == 200) {
                                                val items = doc.select("item")
                                                for (item in items) {
                                                    val title = item.select("title")
                                                    val link = item.select("link")
                                                    val pubDate = item.select("pubDate")
                                                    val newsItem = NewsItem(
                                                        headline = title.text(),
                                                        url = link.text()
//                                                            cite = pubDate.text()
                                                    )
                                                    list.add(newsItem)
                                                }
                                            }
                                        } ui {
                                            listview.items = FXCollections.observableList(list)
                                            if (code != 200) {
                                                log.warning { "$symbol request status code: $code\nURL: $url" }
                                            }
                                            isDisable = false
                                        }
                                    }
                                }
                            }
                        }

                        listview = listview {
                            cellCache {
                                val item = it
                                vbox {
                                    hyperlink(item.headline) {
                                        tooltip(item.url)
                                        setOnAction {
                                            Desktop.getDesktop().browse(URI(item.url))
                                        }
                                    }
                                }
                            }

                            vgrow = Priority.ALWAYS
                        }
                    }
                }
            }
        }
    }

}