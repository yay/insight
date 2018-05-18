package com.vitalyk.insight.view

import com.vitalyk.insight.iex.IexSymbols
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat
import com.vitalyk.insight.style.Styles as styles

class ChartView : View("Chart") {

    val symbolTableView = find(SymbolTableView::class)
    private var chart = linechart(null, CategoryAxis(), NumberAxis().apply{
        isForceZeroInRange = false
    }) {
        animated = false
        createSymbols = false
        isLegendVisible = false
        vgrow = Priority.ALWAYS
    }

    override fun onDock() {
        val symbol = symbolTableView.symbol.value
        val items = symbolTableView.symbolTable.items

        chart.apply {
            title = "$symbol - ${IexSymbols.name(symbol) ?: "Unknown symbol"}"
            val showGridLines = items.count() < 100
            isHorizontalGridLinesVisible = showGridLines
            verticalGridLinesVisible = showGridLines

            series(symbol) {
                val dateFormat = SimpleDateFormat("d MMM, yyyy")
                items.forEach {
                    data(dateFormat.format(it.date), it.close)
                }
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

        this += chart
    }
}