package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.DayChartFragment
import com.vitalyk.insight.iex.DayChartPointBean
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.toBean
import com.vitalyk.insight.ui.symbolfield
import com.vitalyk.insight.yahoo.getDistributionInfo
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.ComboBox
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat

class SymbolTableView : Fragment("Instrument Data") {

    lateinit var symbolTable: TableView<DayChartPointBean>
    lateinit var rangeCombo: ComboBox<Iex.Range>

    private var dataPoints = emptyList<Iex.DayChartPoint>()
    val dataBeans = mutableListOf<DayChartPointBean>().observable()
    var symbol = SimpleStringProperty("AAPL")
    var range = SimpleObjectProperty(Iex.Range.Y)

    private fun Node.updateSymbolTable() {
        val symbol = symbol.value
        val range = range.value
        runAsyncWithProgress {
            Iex.getDayChart(symbol, range) ?: emptyList()
        } ui {
            dataPoints = it
            dataBeans.setAll(it.map { it.toBean() })
        }
    }

    override val root = vbox {
        toolbar {
            button("Back").action { replaceWith(MainView::class) }
            label("Symbol:")
            symbolfield(symbol) { updateSymbolTable() }
            button("Go").action { updateSymbolTable() }
            label("Period:")
            rangeCombo = combobox(range, Iex.Range.values().toList().observable()) {
                // TODO: ComboBoxBase could have `action` extension as well
                setOnAction { updateSymbolTable() }
            }
            button("Chart").action {
                find(DayChartFragment::class).let {
                    it.updateChart(symbol.value, dataPoints)
                    replaceWith(it)
                }
            }
        }

        toolbar {
            button("Market Distribution").action {
                alert(
                    Alert.AlertType.INFORMATION,
                    "Market Distribution Days",
                    getDistributionInfo()
                )
            }
            button("Symbol Distribution").action {
                alert(
                    Alert.AlertType.INFORMATION,
                    "${symbol.value} Distribution Days",
                    getDistributionInfo(setOf(symbol.value))
                )
            }
            button("Canvas").action {
                replaceWith(CanvasView::class)
            }
        }

        val dateFormat = SimpleDateFormat("dd MMM, yy")
        symbolTable = tableview(dataBeans) {
            column("Date", DayChartPointBean::dateProperty) {
                cellFormat {
                    if (it != null) text = dateFormat.format(it)
                }
            }
            column("Open", DayChartPointBean::openProperty)
            column("High", DayChartPointBean::highProperty)
            column("Low", DayChartPointBean::lowProperty)
            column("Close", DayChartPointBean::closeProperty)
            column("Volume", DayChartPointBean::volumeProperty)
            column("Change", DayChartPointBean::changeProperty)
            column("Change %", DayChartPointBean::changePercentProperty) {
                cellFormat { text = "$it%" }
            }
            column("Change Over Time", DayChartPointBean::changeOverTimeProperty)

            vgrow = Priority.ALWAYS
        }
    }
}