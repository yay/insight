package com.vitalyk.insight.view

import com.vitalyk.insight.iex.DayChartPointBean
import com.vitalyk.insight.iex.IexApi
import com.vitalyk.insight.iex.toDayChartPointBean
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.TabPane
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import tornadofx.*
import java.time.LocalDate

class SymbolTableView : View("Security Data") {

    lateinit var symbolTable: TableView<DayChartPointBean>

    lateinit var timeRangeCombo: ComboBox<IexApi.Range>
    var symbol = SimpleStringProperty("AAPL")
    val startDate = datepicker {
        value = LocalDate.now().minusYears(1)
    }
    val endDate = datepicker {
        value = LocalDate.now()
    }
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
                        val range = timeRangeCombo.selectedItem ?: IexApi.Range.Y
                        runAsyncWithProgress {
                            IexApi.getDayChart(symbol.value, range).map { point -> point.toDayChartPointBean() }
                        } ui { items ->
                            symbolTable.items = items.observable()
                            symbolData.value = items.toString()
                        }
                    }
                }
            }

            label("Period:")
            timeRangeCombo = combobox(
                SimpleObjectProperty(IexApi.Range.Y),
                IexApi.Range.values().toList().observable()
            ) {
//                selectionModel.select(0)
            }

            button("Chart") {
                setOnAction {
                    replaceWith(ChartView::class)
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

        tabpane {

            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            vgrow = Priority.ALWAYS

            tab("Data") {
                symbolTable = tableview(listOf<DayChartPointBean>().observable()) {
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