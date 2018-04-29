package com.vitalyk.insight.view

import com.vitalyk.insight.bond.UsYield
import com.vitalyk.insight.bond.getUsYieldCurveData
import com.vitalyk.insight.ui.toolbox
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.control.ScrollBar
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat

class YieldCurveView : View("Yield Curve") {
    private val dateFormat = SimpleDateFormat("d MMM, yyyy")
    private val data = getUsYieldCurveData()

    val chart = linechart(null, CategoryAxis(), NumberAxis()) {
        animated = false
        createSymbols = false
        isLegendVisible = false
        vgrow = Priority.ALWAYS
    }

    override val root = vbox {
        toolbox {
            button("Back").action { replaceWith(SymbolTableView::class) }
        }

        this += chart

        this += ScrollBar().apply {
            min = 0.0
            max = (data.count() - 1).toDouble()
            value = 0.0

            valueProperty().onChange {
                updateChart(it.toInt())
            }
        }

        updateChart(0)
    }

    fun updateChart(index: Int) {
        val rec = data[index]

        chart.title = dateFormat.format(rec.date)
        chart.data.clear()
        chart.series("Yield Curve") {
            rec.mo1?.let { data("${UsYield::mo1.name}\n${rec.mo3}%", it) }
            data("${UsYield::mo3.name}\n${rec.mo3}%", rec.mo3 ?: 0.0)
            data("${UsYield::mo6.name}\n${rec.mo6}%", rec.mo6 ?: 0.0)
            data("${UsYield::yr1.name}\n${rec.yr1}%", rec.yr1 ?: 0.0)
            data("${UsYield::yr2.name}\n${rec.yr2}%", rec.yr2 ?: 0.0)
            data("${UsYield::yr3.name}\n${rec.yr3}%", rec.yr3 ?: 0.0)
            data("${UsYield::yr5.name}\n${rec.yr5}%", rec.yr5 ?: 0.0)
            data("${UsYield::yr7.name}\n${rec.yr7}%", rec.yr7 ?: 0.0)
            data("${UsYield::yr10.name}\n${rec.yr10}%", rec.yr10 ?: 0.0)
            rec.yr20?.let { data("${UsYield::yr20.name}\n$it%", it) }
            rec.yr30?.let { data("${UsYield::yr30.name}\n$it%", it) }
        }
    }
}