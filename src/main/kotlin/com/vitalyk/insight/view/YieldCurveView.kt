package com.vitalyk.insight.view

import com.vitalyk.insight.bond.Yield
import com.vitalyk.insight.bond.getUsYieldData
import com.vitalyk.insight.fragment.InfoFragment
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.control.ScrollBar
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat

class YieldCurveView : View("Yield Curve") {
    private val dateFormat = SimpleDateFormat("d MMM, yyyy")
    private var data: List<Yield>? = null

    val toolbox = toolbar {
        button("Back").action { replaceWith(SymbolTableView::class) }
        button("Update").action { updateData() }
        button("?").action {
            find(InfoFragment::class.java).setInfo("Yield Curve", """
                Yield Curve - a summary of yields across a spectrum of maturities in the bond market.

                Normally, the yield curve slopes upwards, where short term interest rates are lower then
                long term interest rates, because if you are're going to tie up your money for a longer
                period of time, you usually want more compensation for the risk that you're taking.

                The yield curve flattens or sometimes can even invert when the Fed tightens the monetary
                policy by raising short term interest rates, causing the growth and inflation to slow.

                When the yield curve inverts, it can signal a recession coming in the next year or so.
                One of the reasons behind that is that the difference between short and long term rates
                is a proxy for bank lending profitability. Banks tend to borrow at short term rates and
                lend at longer term rates, earning the yield spread. And when the yield curve collapses
                or inverts, it's less profitable for banks to lend, and consequently harder for consumers
                and businesses to borrow, which can lead to a recession.
            """, trim = true).openModal()
        }
    }

    val yieldChart = linechart(null, CategoryAxis(), NumberAxis()) {
        animated = false
        createSymbols = false
        isLegendVisible = false
        vgrow = Priority.ALWAYS
    }

    val spreadChart = linechart(null, CategoryAxis(), NumberAxis()) {
        animated = false
        createSymbols = false
        isLegendVisible = false
        vgrow = Priority.ALWAYS
    }

    val scrollBar = ScrollBar().apply {
        isDisable = true
        valueProperty().onChange {
            val index = it.toInt()
            data?.let { updateYieldChart(it[index]) }
        }
    }

    override val root = vbox {
        this += toolbox
        this += yieldChart
        this += spreadChart
        this += scrollBar
    }

    override fun onDock() {
        if (data == null)
            runLater { // this gives just enough time for toolbar to render
                updateData()
            }
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
            updateYieldChart(it[0])
//            updateSpreadChart(it)
        }
    }

    fun updateYieldChart(rec: Yield) {
        yieldChart.title = dateFormat.format(rec.date)
        yieldChart.data.clear()
        yieldChart.series("Yield Curve") {
            rec.mo1?.let { data("${Yield::mo1.name}\n${rec.mo3}%", it) }
            data("${Yield::mo3.name}\n${rec.mo3}%", rec.mo3 ?: 0.0)
            data("${Yield::mo6.name}\n${rec.mo6}%", rec.mo6 ?: 0.0)
            data("${Yield::yr1.name}\n${rec.yr1}%", rec.yr1 ?: 0.0)
            data("${Yield::yr2.name}\n${rec.yr2}%", rec.yr2 ?: 0.0)
            data("${Yield::yr3.name}\n${rec.yr3}%", rec.yr3 ?: 0.0)
            data("${Yield::yr5.name}\n${rec.yr5}%", rec.yr5 ?: 0.0)
            data("${Yield::yr7.name}\n${rec.yr7}%", rec.yr7 ?: 0.0)
            data("${Yield::yr10.name}\n${rec.yr10}%", rec.yr10 ?: 0.0)
            rec.yr20?.let { data("${Yield::yr20.name}\n$it%", it) }
            rec.yr30?.let { data("${Yield::yr30.name}\n$it%", it) }
        }
    }

    fun updateSpreadChart(yields: List<Yield>) {
        spreadChart.title = "10-year minus 2-year yield curve spread"
        spreadChart.data.clear()
        spreadChart.series("10y") {
            yields.forEach {
                data(dateFormat.format(it.date), (it.yr10 ?: 0.0) - (it.yr2 ?: 0.0))
            }
        }
    }
}