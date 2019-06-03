package com.vitalyk.insight.view

import com.vitalyk.insight.bond.Yield
import com.vitalyk.insight.bond.getUsYieldData
import com.vitalyk.insight.fragment.InfoFragment
import com.vitalyk.insight.helpers.aggregate
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.control.ScrollBar
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.*

class YieldCurveView : View("Yield Curve") {
    private val dateFormat = SimpleDateFormat("d MMM, yyyy")
    private var data: List<Yield>? = null

    val toolbox = toolbar {
        button("Back").action { replaceWith(EconomyView::class) }
        button("Update") {
            action { updateData() }
        }
        button("?").action {
            find(InfoFragment::class.java).apply {
                setInfo("Yield Curve", """
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
                """, trim = true)
                openModal()
            }
        }
    }

    val yieldChart = linechart(null, CategoryAxis(), NumberAxis()) {
        animated = false
        createSymbols = false
        isLegendVisible = false
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS
    }

    val sliceSize = 60
    val shortYieldChart = linechart(null, CategoryAxis(), NumberAxis()) {
        animated = false
        createSymbols = false
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS
    }

    val spreadChart = linechart(null, CategoryAxis(), NumberAxis()) {
        animated = false
        createSymbols = false
        isLegendVisible = false
        isHorizontalGridLinesVisible = false
        verticalGridLinesVisible = false
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS
    }

    val yieldScrollBar = ScrollBar().apply {
        isDisable = true
        valueProperty().onChange {
            val index = it.toInt()
            data?.let { updateYieldChart(it[index]) }
        }
    }

    val shortYieldScrollBar = ScrollBar().apply {
        isDisable = true
        valueProperty().onChange {
            val index = it.toInt()
            data?.let { updateShortYieldChart(it.slice(index .. index + sliceSize).asReversed()) }
        }
    }

    override val root = vbox {
        this += toolbox
        hbox {
            vbox {
                this += yieldChart
                this += yieldScrollBar

                this += spreadChart

                hgrow = Priority.ALWAYS
            }
            vbox {
                this += shortYieldChart
                this += shortYieldScrollBar
                hgrow = Priority.ALWAYS
            }
            vgrow = Priority.ALWAYS
        }
    }

    override fun onDock() {
        if (data == null)
            runLater { // this gives just enough time for toolbar to render
                updateData()
            }
    }

    fun updateData() {
        toolbox.runAsyncWithProgress {
            yieldScrollBar.isDisable = true
            getUsYieldData()
        } ui {
            data = it
            yieldScrollBar.apply {
                isDisable = false
                min = 0.0
                max = (it.count() - 1).toDouble()
                value = 0.0
            }
            shortYieldScrollBar.apply {
                isDisable = false
                min = 0.0
                max = (it.count() - 1 - sliceSize).toDouble()
                value = 0.0
            }
            updateYieldChart(it[0])
            updateShortYieldChart(it.slice(0..sliceSize).asReversed())
            updateSpreadChart(it)
        }
    }

    fun updateYieldChart(rec: Yield) {
        var title = dateFormat.format(rec.date)
        val yr2 = rec.yr2
        val yr10 = rec.yr10
        if (yr2 != null && yr10 != null)
            title += "\n10-year minus 2-year spread: %.2f%%".format(yr10 - yr2)

        yieldChart.let { chart ->
            chart.title = title
            chart.data.clear()
            chart.series("Yield Curve") {
                rec.mo1?.let { data("${Yield::mo1.name}\n$it%", it) }
                rec.mo3?.let { data("${Yield::mo3.name}\n$it%", it) }
                rec.mo6?.let { data("${Yield::mo6.name}\n$it%", it) }
                rec.yr1?.let { data("${Yield::yr1.name}\n$it%", it) }
                rec.yr2?.let { data("${Yield::yr2.name}\n$it%", it) }
                rec.yr3?.let { data("${Yield::yr3.name}\n$it%", it) }
                rec.yr5?.let { data("${Yield::yr5.name}\n$it%", it) }
                rec.yr7?.let { data("${Yield::yr7.name}\n$it%", it) }
                rec.yr10?.let { data("${Yield::yr10.name}\n$it%", it) }
                rec.yr20?.let { data("${Yield::yr20.name}\n$it%", it) }
                rec.yr30?.let { data("${Yield::yr30.name}\n$it%", it) }
            }
        }
    }

    fun updateShortYieldChart(records: List<Yield>) {
        shortYieldChart.let { chart ->
            chart.title = title
            chart.data.clear()
            chart.series("2-year") {
                records.forEach { rec ->
                    rec.yr2?.let { data(dateFormat.format(rec.date), it) }
                }
            }
            chart.series("10-year") {
                records.forEach { rec ->
                    rec.yr10?.let { data(dateFormat.format(rec.date), it) }
                }
            }
            chart.series("Spread") {
                records.forEach { rec ->
                    val yr2 = rec.yr2
                    val yr10 = rec.yr10
                    if (yr2 != null && yr10 != null) {
                        data(dateFormat.format(rec.date), yr10 - yr2)
                    }
                }
            }
        }
    }

    fun updateSpreadChart(yields: List<Yield>) {
        data class YieldSpread(
            val date: Date,
            val spread: Double
        )
        spreadChart.let { chart ->
            chart.title = "10-year minus 2-year yield curve spread"
            chart.data.clear()
            chart.style {
                strokeWidth = 1.px
            }
            chart.series("10y") {
                val spreads = yields.aggregate(500) {
                    YieldSpread(it.last().date, it.sumByDouble { (it.yr10 ?: 0.0) - (it.yr2 ?: 0.0) } / it.size.toDouble() )
                }
                spreads.forEach {
                    data(dateFormat.format(it.date), it.spread)
                }
            }
        }
    }
}