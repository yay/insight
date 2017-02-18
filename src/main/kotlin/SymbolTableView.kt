import javafx.application.HostServices
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.DatePicker
import javafx.scene.control.TabPane
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import tornadofx.*
import java.time.LocalDate

class SymbolTableView : View("Stock Data") {

    val controller: SymbolTableController by inject()

    lateinit var symbolTable: TableView<StockSymbol>

    var symbol = SimpleStringProperty("AAPL")
    val period = SimpleStringProperty("Week")
    var symbolData = SimpleStringProperty("")
    var symbolSummary = SimpleStringProperty("")

    override val root = vbox()

    init {

        primaryStage.minWidth = 900.0
        primaryStage.minHeight = 600.0

        with (root) {
            hbox {
                spacing = 10.0
                padding = Insets(10.0)
                alignment = Pos.CENTER_LEFT

                label("Symbol:")
                textfield(symbol) {
                    textProperty().onChange { value ->
                        this.text = value?.toUpperCase()
                    }
                    onKeyReleased = EventHandler { key ->
                        if (key.code == KeyCode.ENTER) {
                            runAsyncWithProgress {
                                controller.fetch(root, symbol.value)
                            }
                        }
                    }
                }

                label("Period:")
                val periodValues = DataFrequency.values().map { it -> it.toString().toLowerCase().capitalize() }
                combobox(period, FXCollections.observableArrayList(periodValues)) {
                    id = "period"
                }

                button {
                    text = "Fetch All"
                    tooltip("Fetches both data and summary") {
                        font = Font.font("Verdana")
                    }

                    setOnAction {
                        isDisable = true
                        runAsyncWithProgress {
                            controller.fetch(root, symbol.value)
                        } ui {
                            isDisable = false
                        }
                    }
                }

                button {

                    text = "Fetch Data"

                    setOnAction {
                        isDisable = true
                        runAsyncWithProgress {
                            controller.fetchData(root, symbol.value)
                        } ui {
                            isDisable = false
                        }
                    }
                }

                button {
                    text = "Fetch Summary"

                    setOnAction {
                        isDisable = true
                        runAsyncWithProgress {
                            controller.fetchSummary(root, symbol.value)
                        } ui {
                            isDisable = false
                        }
                    }
                }

                button("Show Chart") {
                    setOnAction {
                        replaceWith(
                                SymbolChartView::class
//                            ViewTransition.Slide(
//                                    0.3.seconds,
//                                    ViewTransition.Direction.LEFT
//                            )
                        )
                    }
                }

                button("Show CSV") {
                    setOnAction {
                        replaceWith(SymbolCsvView::class)
                    }
                }
            }

            hbox {
                spacing = 10.0
                padding = Insets(10.0)
                alignment = Pos.CENTER_LEFT

                label("Start date: ")
                datepicker {
                    id = "startDate"
                    value = LocalDate.now().minusYears(1)
                }

                label("End date: ")
                datepicker {
                    id = "endDate"
                    value = LocalDate.now()
                }

                button("Data Fetcher") {
                    setOnAction {
                        replaceWith(DataFetcherView::class)
//                        var hs = HostServices(insight.InsightApp::class.objectInstance)
//                        getHostServices().showDocument("http://www.yahoo.com");
                    }
                }
            }

//            class MyFragment : Fragment() {
//                override val root = label("This is a popup!")
//            }
//        button {
//            text = "Press me"
//            setOnAction {
//                openInternalWindow(MyFragment::class)
////                find(MyFragment::class).openModal(stageStyle = StageStyle.UTILITY)
//            }
//        }
            tabpane {

                tab("Data") {
                    symbolTable = tableview(listOf<StockSymbol>().observable()) {
                        column("Date", StockSymbol::dateProperty).minWidth(250)
                        column("Open", StockSymbol::openProperty)
                        column("High", StockSymbol::highProperty)
                        column("Low", StockSymbol::lowProperty)
                        column("Close", StockSymbol::closeProperty)
                        column("Volume", StockSymbol::volumeProperty)
                        column("Adj Close", StockSymbol::adjCloseProperty).minWidth(150)

                        vgrow = Priority.ALWAYS
//                        columnResizePolicy = SmartResize.POLICY
                    }
                }

                tab("Raw Data") {
                    textarea(symbolData) {
                        vgrow = Priority.ALWAYS
                    }
                }

                tab("Raw Summary") {
                    textarea(symbolSummary) {
                        vgrow = Priority.ALWAYS
                    }
                }

                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                vgrow = Priority.ALWAYS
            }


        }
    }

}