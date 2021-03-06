package com.vitalyk.insight.fragment

import com.vitalyk.insight.helpers.browseTo
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.main.HttpClients
import javafx.geometry.Bounds
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.text.Font
import javafx.scene.text.Text
import tornadofx.*
import java.text.SimpleDateFormat

class EarningsFragment : Fragment("Earnings") {
    private val chart = barchart("Earnings", CategoryAxis(), NumberAxis()) {
        animated = false

        contextmenu {
            item("Earnings Calendar").action {
                browseTo("http://hosting.briefing.com/cschwab/Calendars/EarningsCalendar5Weeks.htm")
            }
            item("Nasdaq Earnings").action {
                browseTo("https://www.nasdaq.com/earnings/report/${symbol.toLowerCase()}")
            }
            item("Nasdaq Institutional Holdings").action {
                browseTo("https://www.nasdaq.com/symbol/${symbol.toLowerCase()}/institutional-holdings")
            }
            item("Yahoo Finance").action {
                browseTo("https://finance.yahoo.com/quote/$symbol")
            }
            menu("Chart") {
                Iex.Range.values().forEach { range ->
                    item(range.value.name).action {
                        runAsync {
                            val iex = Iex(HttpClients.main)
                            iex.getDayChartWithQuote(symbol, range) ?: mutableListOf()
                        } ui { points ->
                            find(DayChartFragment::class).let {
                                it.updateChart(symbol, points)
                                it.openModal()
                            }
                        }
                    }
                }
            }
        }
    }

    private val chartLabels = mutableListOf<Text>()
    private val dateFormat = SimpleDateFormat("E MMM dd, yy")
    private var symbol: String = ""

    override val root = vbox {
        this += chart
    }

    private val labelFontSize = 9.0
    private val labelGap = 2.0

    private fun positionText(text: Text, bounds: Bounds) {
        var y = bounds.minY - labelGap
        if (y < labelFontSize + labelGap * 2) { // bars reaching the top will have minY close to 0
            y = bounds.minY + labelGap + labelFontSize
        }
        text.isVisible = bounds.width > 16
        text.layoutX = bounds.minX + bounds.width * 0.5 - text.prefWidth(-1.0) * 0.5
        text.layoutY = y
    }

    private fun getCategory(earnings: Iex.Earnings): String =
        earnings.fiscalPeriod ?: dateFormat.format(earnings.fiscalEndDate)

    fun fetch(symbol: String) {
        this.symbol = symbol
        runAsync {
            val iex = Iex(HttpClients.main)
            iex.getEarnings(symbol)
        } ui {
            (it?.earnings ?: emptyList()).asReversed().let { earnings ->
                val chart = chart
                chart.data.clear()

                chart.series("Year Ago") {
                    earnings.forEach {
                        data(getCategory(it), it.yearAgo)

                        node?.tooltip(it.yearAgo.toString())
                    }
                }
                chart.series("Estimate") {
                    earnings.forEach { earnings ->
                        earnings.estimatedEps?.let {
                            data(getCategory(earnings), it)
                        }
                    }
                }
                val actualEarningsSeries = chart.series("Actual") {
                    earnings.forEach {
                        data(getCategory(it), it.actualEps)
                    }
                }

                // The node (bar) representing the data is only available
                // when a series has rendered.
                chartLabels.forEach { it.removeFromParent() }
                chartLabels.clear()
                runLater {
                    chart.data.forEach { series ->
                        val isActual = series == actualEarningsSeries
                        series.data.forEachIndexed { index, datum ->
                            datum.node?.apply {
                                val data = earnings[index]
                                if (isActual)
                                    tooltip(
                                        dateFormat.format(data.epsReportDate) +
                                            (data.announceTime?.let { " ($it)" } ?: ""))

                                val text = Text(datum.yValue.toString()).apply {
                                    font = Font("Tahoma", labelFontSize)
                                    isMouseTransparent = true
                                }
                                chartLabels.add(text)
                                parent.add(text)

                                positionText(text, boundsInParent)

                                boundsInParentProperty().addListener(ChangeListener { _, _, bounds ->
                                    positionText(text, bounds)
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}