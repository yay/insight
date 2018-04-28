package com.vitalyk.insight.view

import com.vitalyk.insight.bond.UsYield
import com.vitalyk.insight.bond.getUsYieldCurveData
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.control.ScrollBar
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat
import kotlin.reflect.full.memberProperties

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
            rec.mo1?.let { data(UsYield::mo1.name, it) }
            data(UsYield::mo3.name, rec.mo3 ?: 0.0)
            data(UsYield::mo6.name, rec.mo6 ?: 0.0)
            data(UsYield::yr1.name, rec.yr1 ?: 0.0)
            data(UsYield::yr2.name, rec.yr2 ?: 0.0)
            data(UsYield::yr3.name, rec.yr3 ?: 0.0)
            data(UsYield::yr5.name, rec.yr5 ?: 0.0)
            data(UsYield::yr7.name, rec.yr7 ?: 0.0)
            data(UsYield::yr10.name, rec.yr10 ?: 0.0)
            data(UsYield::yr10.name, rec.yr10 ?: 0.0)
            rec.yr30?.let { data(UsYield::yr30.name, it) }
        }
    }
}