import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import org.jsoup.Jsoup
import tornadofx.*
import java.awt.Desktop
import java.net.URI


class NewsItem(val headline: String, val url: String, val cite: String = "")

class SymbolCsvView : View() {

    val symbolTable = find(SymbolTableView::class)
    lateinit var listview: ListView<NewsItem>

    override fun onDock() {
    }

    override fun onUndock() {
    }

    override val root = vbox()

    init {
        with (root) {

            hbox {
                spacing = 10.0
                padding = Insets(10.0)
                alignment = Pos.CENTER_LEFT

                button("Back") {
                    setOnAction {
                        replaceWith(SymbolTableView::class)
                    }
                }

                button("Parse IPO") {
                    setOnAction {
                        isDisable = true
                        var code: Int = 0
                        val list = mutableListOf<NewsItem>()
                        val connection = Jsoup.connect("http://finance.yahoo.com/news/category-ipos/?bypass=true")
//                                .followRedirects(false)
                        runAsyncWithProgress {
                            val doc = connection.get()
                            code = connection.data().response().statusCode()
                            if (code == 200) {
                                val listItems = doc.select(".yom-top-story .yom-list li")
                                for (listItem in listItems) {
                                    val a = listItem.select("a")
                                    var cite = a.next().text()
                                    val newsItem = NewsItem(
                                            headline = a.text(),
                                            url = a.attr("href"),
                                            cite = cite
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
            }

            listview = listview {
                cellCache {
                    val item = it
                    vbox {
                        hyperlink(item.headline) {
                            tooltip(item.url)
                            setOnAction {
                                Desktop.getDesktop().browse(URI(item.url));
                            }
                        }
                        label(item.cite)
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

}