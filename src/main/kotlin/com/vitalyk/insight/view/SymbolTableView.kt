package com.vitalyk.insight.view

import com.vitalyk.insight.iex.DayChartPointBean
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.toBean
import com.vitalyk.insight.ui.symbolfield
import com.vitalyk.insight.ui.toolbox
import com.vitalyk.insight.yahoo.getDistributionInfo
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.ComboBox
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.*

class SymbolTableView : View("Instrument Data") {

    lateinit var symbolTable: TableView<DayChartPointBean>
    lateinit var rangeCombo: ComboBox<Iex.Range>

    var symbol = SimpleStringProperty("AAPL")
    var range = SimpleObjectProperty(Iex.Range.Y)

    private fun Node.updateSymbolTable() {
        runAsyncWithProgress {
            Iex.getDayChart(symbol.value, range.value)?.map { point ->
                point.toBean()
            } ?: emptyList()
        } ui { items ->
            symbolTable.items = items.observable()
        }
    }

    override val root = vbox {
        toolbox {
            label("Symbol:")
            symbolfield(symbol, { updateSymbolTable() }) {
                maxWidth = 80.0
            }
            button("Go") {
                setOnAction {
                    updateSymbolTable()
                }
            }

            label("Period:")
            rangeCombo = combobox(range, Iex.Range.values().toList().observable()) {
                setOnAction { updateSymbolTable() }
            }

            button("Chart").action { replaceWith(ChartView::class) }

            button("News").action { replaceWith(NewsView::class) }

            button("Quotes").action { replaceWith(QuoteView::class) }
        }

        toolbox(border = false) {
            button("Watchlists").action { replaceWith(WatchlistView::class) }

            button("Market Distribution") {
                setOnAction {
                    alert(
                        Alert.AlertType.INFORMATION,
                        "Market Distribution Days",
                        getDistributionInfo()
                    )
                }
            }

            button("Symbol Distribution") {
                setOnAction {
                    alert(
                        Alert.AlertType.INFORMATION,
                        "${symbol.value} Distribution Days",
                        getDistributionInfo(setOf(symbol.value))
                    )
                }
            }

            button("Canvas").action {
                replaceWith(CanvasView::class)
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