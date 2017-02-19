import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.*
import java.time.LocalDate
import java.util.*

open class FetcherTask(symbol: String, url: String, threadId: Int, startTime: LocalDate) {
    var symbol: String by property(symbol)
    fun symbolProperty() = getProperty(FetcherTask::symbol)

    var url: String by property(url)
    fun urlProperty() = getProperty(FetcherTask::url)

    var threadId: Int by property(threadId)
    fun threadIdProperty() = getProperty(FetcherTask::threadId)

    var startTime: LocalDate by property(startTime)
    fun startProperty() = getProperty(FetcherTask::startTime)

}

class DataFetcherView : View() {

    override val root = vbox()

    init {

        hbox {
            spacing = 10.0
            padding = Insets(10.0)
            alignment = Pos.CENTER_LEFT

            button("Back") {
                setOnAction {
                    replaceWith(SymbolTableView::class)
                }
            }

            button("Fetch some stuff") {
                setOnAction {

                }
            }
        }

        tableview(listOf<FetcherTask>().observable()) {
            column("Symbol", FetcherTask::symbol)
            column("URL", FetcherTask::url).minWidth(400)
            column("Thread ID", FetcherTask::threadId)
            column("Start Time", FetcherTask::startTime)
        }

        listview(listOf("hello", "hi").observable()) {
            vgrow = Priority.ALWAYS
        }
    }

}