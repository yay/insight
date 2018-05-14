package com.vitalyk.insight.view

import com.vitalyk.insight.iex.IexSymbols
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat
import com.vitalyk.insight.style.Styles as styles

class ChartView : View("Chart") {

    val symbolTableView = find(SymbolTableView::class)
    private lateinit var chart: LineChart<String, Number>

    override fun onDock() {
        val symbol = symbolTableView.symbol.value
        chart.title = "$symbol - ${IexSymbols.name(symbol) ?: "Unknown symbol"}"
        val showGridLines = symbolTableView.symbolTable.items.count() < 100
        chart.isHorizontalGridLinesVisible = showGridLines
        chart.verticalGridLinesVisible = showGridLines

        chart.series(symbol) {
            val dateFormat = SimpleDateFormat("d MMM, yyyy")
            for (item in symbolTableView.symbolTable.items) {
                data(dateFormat.format(item.date), item.close)
            }
        }
    }

    override fun onUndock() {
        chart.data.clear()
    }

    override val root = vbox {
        toolbar {
            button("Back") {
                action {
                    replaceWith(SymbolTableView::class)
                }
            }
        }

        chart = linechart(null, CategoryAxis(), NumberAxis()) {
            animated = false
            createSymbols = false
            isLegendVisible = false
            vgrow = Priority.ALWAYS
        }
    }
}