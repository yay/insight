package com.vitalyk.insight.view

import com.vitalyk.insight.bond.UsYield
import com.vitalyk.insight.bond.getUsYieldData
import com.vitalyk.insight.ui.toolbox
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.control.ScrollBar
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat

class YieldCurveView : View("Yield Curve") {
    private val dateFormat = SimpleDateFormat("d MMM, yyyy")
    private lateinit var data: List<UsYield>

    val updateButton = button("Update") {
        action { updateData() }
    }

    val toolbox = toolbox {
        button("Back").action { replaceWith(SymbolTableView::class) }
        this += updateButton
    }

    val chart = linechart(null, CategoryAxis(), NumberAxis()) {
        animated = false
        createSymbols = false
        isLegendVisible = false
        vgrow = Priority.ALWAYS
    }

    val scrollBar = ScrollBar().apply {
        isDisable = true
        valueProperty().onChange {
            updateChart(it.toInt())
        }
    }

    override val root = vbox {
        this += toolbox
        this += chart
        this += scrollBar
    }

    override fun onDock() {
        updateData()
    }

    fun updateData() {
        toolbox.runAsyncWithProgress {
            scrollBar.isDisable = true
            getUsYieldData()
        } ui {
            data = it
            scrollBar.apply {
                isDisable = false
                min = 0.0
                max = (it.count() - 1).toDouble()
                value = 0.0
            }

            updateChart(0)
        }
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