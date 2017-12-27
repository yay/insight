package com.vitalyk.insight.view

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ListView
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import com.vitalyk.insight.main.NewsItem
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

            hbox {
                spacing = 10.0
                padding = Insets(10.0)
                alignment = Pos.CENTER_LEFT

                button("Back") {
                    setOnAction {
                        replaceWith(SymbolTableView::class)
                    }
                }
            }

            val tabPane = tabpane {

                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                vgrow = Priority.ALWAYS

                tab("yahoo.News") {
                    vbox {
                        hbox {
                            spacing = 10.0
                            padding = Insets(10.0)
                            alignment = Pos.CENTER_LEFT

                            button("IPO") {
                                setOnAction {
                                    isDisable = true
                                    listview.items.clear()
                                    var code: Int = 0
                                    val list = mutableListOf<NewsItem>()
                                    val connection = Jsoup.connect(ipoUrl).timeout(timeout)
//                                            .followRedirects(false)
                                    runAsyncWithProgress {
                                        val doc = connection.get()
                                        code = connection.data().response().statusCode()
                                        if (code == 200) {
                                            val listItems = doc.select(".yom-top-story .yom-list li")
                                            for (listItem in listItems) {
                                                val a = listItem.select("a")
                                                val cite = a.next().text()
                                                var href = a.attr("href")
                                                if (href.indexOf("http") != 0) {
                                                    href = baseUrl + href
                                                }
                                                val newsItem = NewsItem(
                                                    headline = a.text(),
                                                    url = href
//                                                        cite = cite
                                                )
                                                list.add(newsItem)
                                            }
                                        }
                                    } ui {
                                        listview.items = FXCollections.observableList(list)
                                        if (code != 200) {
                                            // http://code.makery.ch/blog/javafx-dialogs-official/
                                            alert(Alert.AlertType.INFORMATION, "Request error",
                                                "Request status code: " + code)
                                        }
                                        isDisable = false
                                    }

                                }
                            }

                            label("Symbol:")
                            textfield(symbol) {
                                tooltip("Fetches symbol data and summary") {
                                    font = Font.font("Verdana")
                                }
                                textProperty().onChange { value ->
                                    this.text = value?.toUpperCase()
                                }
                                onKeyReleased = EventHandler { key ->
                                    if (key.code == KeyCode.ENTER) {

                                        listview.items.clear()
                                        var code: Int = 0
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
//                                setOnMouseClicked {
//                                    storyWebView.engine.load(item.url)
//                                    // http://stackoverflow.com/questions/6902377/javafx-tabpane-how-to-set-the-selected-tab
//                                    tabPane.selectionModel.select(browserTab)
//                                }
                                vbox {
                                    hyperlink(item.headline) {
                                        tooltip(item.url)
                                        setOnAction {
                                            Desktop.getDesktop().browse(URI(item.url));
                                        }
                                    }
//                                    label(item.cite)
                                }
//                    form {
//                        fieldset {
////                            field("Headline") {
////                                label(it.headline)
////                            }
////                            field("URL") {
////                                label(it.url)
////                            }
//                        }
//
//                    }
                            }

                            vgrow = Priority.ALWAYS
                        }

//            piechart("Imported Fruits") {
//                data("Grapefruit", 12.0)
//                data("Oranges", 25.0)
//                data("Plums", 10.0)
//                data("Pears", 22.0)
//                data("Apples", 30.0)
//
//                vgrow = Priority.ALWAYS
//            }
                    }
                }

//                browserTab = tab("Browser") {
//                    vbox {
//                        hbox {
//                            spacing = 10.0
//                            padding = Insets(10.0)
//                            alignment = Pos.CENTER_LEFT
//
//                            button("Back") {
//                                setOnAction {
//                                    storyWebView.engine.executeScript("history.back()")
//                                }
//                            }
//                        }
//
//                        this += storyWebView
//                    }
//                }

            }
        }
    }

}