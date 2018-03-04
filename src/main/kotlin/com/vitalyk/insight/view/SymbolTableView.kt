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
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
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

    override val root = vbox {
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
                        val range = timeRangeCombo.selectedItem ?: IexApi.Range.Y
                        runAsyncWithProgress {
                            IexApi.getDayChart(symbol.value, range).map { point -> point.toDayChartPointBean() }
                        } ui { items ->
                            symbolTable.items = items.observable()
                        }
                    }
                }
            }

            label("Period:")
            timeRangeCombo = combobox(
                SimpleObjectProperty(IexApi.Range.Y),
                IexApi.Range.values().toList().observable()
            )

            button("Chart") {
                setOnAction {
                    replaceWith(ChartView::class)
                }
            }

            button("News") {
                setOnAction {
                    replaceWith(NewsView::class)
                }
            }

            button("Quotes") {
                setOnAction {
                    replaceWith(QuoteView::class)
                }
            }
        }

//        hbox {
//            spacing = 10.0
//            padding = Insets(10.0)
//            alignment = Pos.CENTER_LEFT
//
//            label("Start date: ")
//            this += startDate
//
//            label("End date: ")
//            this += endDate
//        }

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

            vgrow = Priority.ALWAYS
        }
        this += symbolTable
    }

    init {
        primaryStage.minWidth = 900.0
        primaryStage.minHeight = 600.0
    }

}