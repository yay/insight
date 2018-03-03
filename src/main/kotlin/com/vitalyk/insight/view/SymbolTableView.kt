package com.vitalyk.insight.view

import com.vitalyk.insight.iex.DayChartPointBean
import com.vitalyk.insight.iex.IexApi
import com.vitalyk.insight.iex.toDayChartPointBean
import com.vitalyk.insight.main.*
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TabPane
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import tornadofx.*
import java.time.LocalDate

class SymbolTableView : View("Security Data") {

    lateinit var symbolTable: TableView<StockSymbol>
    lateinit var iexSymbolTable: TableView<DayChartPointBean>

    var symbol = SimpleStringProperty("AAPL")
    val startDate = datepicker {
        value = LocalDate.now().minusYears(1)
    }
    val endDate = datepicker {
        value = LocalDate.now()
    }
    val period = SimpleStringProperty("Week")
    val periodValues = DataFrequency.values().map { it -> it.toString().toLowerCase().capitalize() }
    var symbolData = SimpleStringProperty("")
    var symbolSummary = SimpleStringProperty("")

    override val root = vbox {
        hbox {
            spacing = 10.0
            padding = Insets(10.0)
            alignment = Pos.CENTER_LEFT

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
                            val frequency = DataFrequency.valueOf(period.value.toUpperCase())
                            val dataRequest = YahooData(symbol.value, frequency)
//                            var summaryRequest = YahooSummary(symbol.value, HttpClients.yahoo)

                            runAsyncWithProgress {

//                                fetchDailyData(symbol.value, 50)

//                                println(data)
//                                dataRequest
//                                        .startDate(startDate.value)
//                                        .endDate(endDate.value)
//                                        .execute()
//                                        .parse()

//                                summaryRequest
//                                        .execute()
//                                        .parse()

                                IexApi.getDayChart("AAPL").map { point -> point.toDayChartPointBean() }
                            } ui { items ->
                                iexSymbolTable.items = items.observable()
//                                symbolTable.items = data.parseYahooCSV().toStockList().observable()
//                                symbolData.value = data

//                                symbolData.value = dataRequest.data()
//                                symbolTable.items = dataRequest.list().observable()
//                                symbolSummary.value = summaryRequest.prettyData()
                            }
                    }
                }
            }

            label("Period:")
            combobox(period, FXCollections.observableArrayList(periodValues))

//                button {
//
//                    text = "Fetch Data"
//
//                    setOnAction {
//                        isDisable = true
//                        runAsyncWithProgress {
//                            controller.fetchData(root, symbol.value)
//                        } ui {
//                            isDisable = false
//                        }
//                    }
//                }
//
//                button {
//                    text = "Fetch Summary"
//
//                    setOnAction {
//                        isDisable = true
//                        runAsyncWithProgress {
//                            controller.fetchSummary(root, symbol.value)
//                        } ui {
//                            isDisable = false
//                        }
//                    }
//                }

            button("Chart") {
                setOnAction {
                    replaceWith(
                        ChartView::class
//                            ViewTransition.Slide(
//                                    0.3.seconds,
//                                    ViewTransition.Direction.LEFT
//                            )
                    )
                }
            }

            button("yahoo.News") {
                setOnAction {
                    replaceWith(NewsView::class)
                }
            }
        }

        hbox {
            spacing = 10.0
            padding = Insets(10.0)
            alignment = Pos.CENTER_LEFT

            label("Start date: ")
            this += startDate

            label("End date: ")
            this += endDate

            button("Data Fetcher") {
                setOnAction {
                    replaceWith(DataFetcherView::class)
//                        var hs = HostServices(insight.yahoo.InsightApp::class.objectInstance)
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

            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            vgrow = Priority.ALWAYS

            tab("Data") {
//                symbolTable = tableview(listOf<StockSymbol>().observable()) {
//                    column("Date", StockSymbol::dateProperty).minWidth(250)
//                    column("Open", StockSymbol::openProperty)
//                    column("High", StockSymbol::highProperty)
//                    column("Low", StockSymbol::lowProperty)
//                    column("Close", StockSymbol::closeProperty)
//                    column("Volume", StockSymbol::volumeProperty)
//                    column("Adj Close", StockSymbol::adjCloseProperty).minWidth(150)
//
//                    vgrow = Priority.ALWAYS
////                        columnResizePolicy = SmartResize.POLICY
//                }
                iexSymbolTable = tableview(listOf<DayChartPointBean>().observable()) {
                    column("Date", DayChartPointBean::dateProperty)
                    column("Open", DayChartPointBean::openProperty)
                    column("High", DayChartPointBean::highProperty)
                    column("Low", DayChartPointBean::lowProperty)
                    column("Close", DayChartPointBean::closeProperty)
                    column("Volume", DayChartPointBean::volumeProperty)
                    column("Change", DayChartPointBean::changeProperty)
                    column("ChangePercent", DayChartPointBean::changePercentProperty)
                    column("ChangeOverTime", DayChartPointBean::changeOverTimeProperty)
                    column("Label", DayChartPointBean::labelProperty)
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

        }
    }

    init {
        primaryStage.minWidth = 900.0
        primaryStage.minHeight = 600.0
    }

}