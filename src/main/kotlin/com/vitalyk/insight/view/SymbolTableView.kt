package com.vitalyk.insight.view

import com.vitalyk.insight.iex.DayChartPointBean
import com.vitalyk.insight.iex.IexApi
import com.vitalyk.insight.iex.toBean
import com.vitalyk.insight.ui.toolbox
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import tornadofx.*

class SymbolTableView : View("Instrument Data") {

    lateinit var symbolTable: TableView<DayChartPointBean>
    lateinit var rangeCombo: ComboBox<IexApi.Range>

    var symbol = SimpleStringProperty("AAPL")
    var range = SimpleObjectProperty(IexApi.Range.Y)

    private fun Node.updateSymbolTable() {
        runAsyncWithProgress {
            IexApi.getDayChart(symbol.value, range.value)?.map { point ->
                point.toBean()
            } ?: emptyList()
        } ui { items ->
            symbolTable.items = items.observable()
        }
    }

    override val root = vbox {
        toolbox(border = false) {
            label("Symbol:")
            textfield(symbol) {
                maxWidth = 80.0
                textProperty().onChange { value ->
                    this.text = value?.toUpperCase()
                }
                onKeyReleased = EventHandler { key ->
                    if (key.code == KeyCode.ENTER) {
                        updateSymbolTable()
                    }
                }
            }
            button("Go") {
                setOnAction { updateSymbolTable() }
            }

            label("Period:")
            rangeCombo = combobox(range, IexApi.Range.values().toList().observable()) {
                setOnAction { updateSymbolTable() }
            }

            button("Chart") {
                setOnAction { replaceWith(ChartView::class) }
            }

            button("News") {
                setOnAction { replaceWith(NewsView::class) }
            }

            button("Quotes") {
                setOnAction {
                    replaceWith(QuoteView::class)
                }
            }

            button("Big Picture") {
                setOnAction { replaceWith(BigPictureView::class) }
            }
        }

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
    }

    init {
        primaryStage.minWidth = 900.0
        primaryStage.minHeight = 600.0
    }

}