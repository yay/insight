package com.vitalyk.insight.fragment

import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.IexSymbols
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.layout.Priority
import tornadofx.*

class MinuteChartFragment : Fragment("Minute Chart") {

    private var chart = linechart(null, CategoryAxis(), NumberAxis().apply{
        isForceZeroInRange = false
    }) {
        animated = false
        createSymbols = false
        isLegendVisible = false
        minWidth = 900.0
        minHeight = 600.0
        vgrow = Priority.ALWAYS
    }

    override val root = vbox {
        this += chart
    }

    fun updateChart(symbol: String, points: List<Iex.MinuteChartPoint>) {
        chart.apply {
            title = "$symbol - ${IexSymbols.name(symbol) ?: "Unknown symbol"}"
            val showGridLines = points.count() < 100
            isHorizontalGridLinesVisible = showGridLines
            verticalGridLinesVisible = showGridLines

            series(symbol) {
                points.forEach {
                    // TODO: what's the difference between `close` and `marketClose`?
                    // TODO: why can they be zero? is this because there were not trades that minute?
                    if (it.close != 0.0) {
                        data(it.label, it.close)
                    }
//                    else if (it.marketClose != 0.0) {
//                        data(it.label, it.marketClose)
//                    }
                }
            }
        }
    }
}