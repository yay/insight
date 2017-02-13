import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import tornadofx.*
import java.time.LocalDate

class SymbolTable : View() {

    val controller: SymbolTableController by inject()

    lateinit var symbolView: TableView<StockSymbol>
    var symbolProperty = SimpleStringProperty("AAPL")
    val periodProperty = SimpleStringProperty("Week")
    private lateinit var symbolData: String

    fun symbolData(): String { return symbolData }

    override val root = vbox {

        primaryStage.minWidth = 700.0
        primaryStage.minHeight = 500.0

        hbox {
            spacing = 10.0
            padding = Insets(10.0)
            alignment = Pos.CENTER_LEFT

            label("Symbol:")
            textfield(symbolProperty) {
                textProperty().onChange { value ->
                    this.text = value?.toUpperCase()
                }
                onKeyReleased = EventHandler { key ->
                    if (key.code == KeyCode.ENTER) {
                        runAsyncWithProgress {
                            val period = DataFrequency.valueOf(periodProperty.value.toUpperCase())
                            YahooDataRequest(symbolProperty.value, period).execute().parse().list().observable()
                        } ui { items ->
                            symbolView.items = items
                        }
                    }
                }
            }

            label("Period:")
            val periodValues = DataFrequency.values().map { it -> it.toString().toLowerCase().capitalize() }
            combobox(periodProperty, FXCollections.observableArrayList(periodValues))

            button {

                text = "Fetch Data"

                setOnAction {
                    isDisable = true
                    runAsyncWithProgress {
                        val period = DataFrequency.valueOf(periodProperty.value.toUpperCase())
                        val request = YahooDataRequest(symbolProperty.value, period).execute()
                        symbolData = request.data()
                        request.parse().list().observable()
                    } ui { items ->
                        symbolView.items = items
                        isDisable = false
                    }
                }
            }

            button("Show Chart") {
                setOnAction {
                    replaceWith(
                            SymbolChart::class
//                            ViewTransition.Slide(
//                                    0.3.seconds,
//                                    ViewTransition.Direction.LEFT
//                            )
                    )
                }
            }

            button("Show CSV") {
                setOnAction {
                    replaceWith(SymbolCsv::class)
                }
            }
        }

        hbox {
            spacing = 10.0
            padding = Insets(10.0)
            alignment = Pos.CENTER_LEFT

            label("Start date: ")
            datepicker {
                value = LocalDate.now()
            }

            label("End date: ")
            datepicker {
                value = LocalDate.now()
            }
        }

//        button {
//            text = "Press me"
//            setOnAction {
//                openInternalWindow(MyFragment::class)
////                find(MyFragment::class).openModal(stageStyle = StageStyle.UTILITY)
//            }
//        }
        symbolView = tableview(listOf<StockSymbol>().observable()) {
            column("Date", StockSymbol::dateProperty)
            column("Open", StockSymbol::openProperty)
            column("High", StockSymbol::highProperty)
            column("Low", StockSymbol::lowProperty)
            column("Close", StockSymbol::closeProperty)
            column("Volume", StockSymbol::volumeProperty)
            column("Adj Close", StockSymbol::adjCloseProperty)

            vgrow = Priority.ALWAYS
            columnResizePolicy = SmartResize.POLICY
        }
    }
}

class MyFragment : Fragment() {
    override val root = label("This is a popup!")
}