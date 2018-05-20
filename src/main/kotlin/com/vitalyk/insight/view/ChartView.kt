package com.vitalyk.insight.view

import com.vitalyk.insight.iex.DayChartPointBean
import com.vitalyk.insight.iex.IexSymbols
import javafx.collections.ObservableList
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat
import com.vitalyk.insight.style.Styles as styles

class ChartView : Fragment("Chart") {

    private var chart = linechart(null, CategoryAxis(), NumberAxis().apply{
        isForceZeroInRange = false
    }) {
        animated = false
        createSymbols = false
        isLegendVisible = false
        vgrow = Priority.ALWAYS
    }

    override val root = vbox {
        toolbar {
            button("Back").action { replaceWith(SymbolTableView::class) }
        }

        this += chart
    }

    fun updateChart(symbol: String, points: ObservableList<DayChartPointBean>) {
        chart.apply {
            title = "$symbol - ${IexSymbols.name(symbol) ?: "Unknown symbol"}"
            val showGridLines = points.count() < 100
            isHorizontalGridLinesVisible = showGridLines
            verticalGridLinesVisible = showGridLines

            series(symbol) {
                val dateFormat = SimpleDateFormat("d MMM, yyyy")
                points.forEach {
                    data(dateFormat.format(it.date), it.close)
                }
            }
        }
    }
}