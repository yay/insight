package com.vitalyk.insight.view

import com.sun.javaws.exceptions.InvalidArgumentException
import com.vitalyk.insight.bond.Yield
import com.vitalyk.insight.bond.getUsYieldData
import com.vitalyk.insight.fragment.InfoFragment
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
        isHorizontalGridLinesVisible = false
        verticalGridLinesVisible = false
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
        this += scrollBar
        this += spreadChart
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

    fun updateSpreadChart(yields: List<Yield>) {
        data class YieldSpread(
            val date: Date,
            val spread: Double
        )
        spreadChart.let { chart ->
            chart.title = "10-year minus 2-year yield curve spread"
            chart.data.clear()
            chart.series("10y") {
                val spreads = yields.aggregate(500, {
                    YieldSpread(it.last().date, it.sumByDouble { (it.yr10 ?: 0.0) - (it.yr2 ?: 0.0) } / it.size.toDouble() )
                })
                spreads.forEach {
                    data(dateFormat.format(it.date), it.spread)
                }
            }
        }
    }
}

fun <T, R> List<T>.aggregate(count: Int, transform: (List<T>) -> R): List<R> {
    if (count <= 0) throw InvalidArgumentException(arrayOf("'count' should be greater than zero."))
    val size = size
    val step: Int = Math.max(1, size / count)
    val list = mutableListOf<R>()
    var i = 0
    while (i < size) {
        val mean = transform(slice(i until Math.min(i + step, size)))
        list.add(0, mean)
        i += step
    }
    return list
}

fun main(args: Array<String>) {
    val list = listOf(2, 8, 4, 6, 7, 3)
    val result1 = list.aggregate(3, { it.average() })
    println(result1)
    val result2 = list.aggregate(2, { it.average() })
    println(result2)
    val result3 = list.aggregate(1, { it.average() })
    println(result3)
    val result4 = list.aggregate(6, { it.average() })
    println(result4)
    val result5 = list.aggregate(4, { it.average() })
    println(result5)
    val list2 = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
    val result6 = list2.aggregate(4, { it.average() })
    println(result6)
}