package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.AssetProfileFragment
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.ui.symbolfield
import com.vitalyk.insight.ui.toolbox
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Bounds
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.text.Font
import javafx.scene.text.Text
import tornadofx.*
import java.text.SimpleDateFormat

class ResearchView : View("Research") {
    var symbolProperty = SimpleStringProperty("")

    val profile = AssetProfileFragment()
    val earnings = EarningsFragment()

    override val root = vbox {
        toolbox {
            button("Main").action { replaceWith(MainView::class) }
            label("Symbol:")
            symbolfield(symbolProperty) {
                profile.fetch(it)
                earnings.fetch(it)
            }
        }

        hbox {
            vbox {
                this += earnings
            }
            vbox {
                this += profile
            }
        }
    }
}

class EarningsFragment : Fragment("Earnings") {
    private val chart = barchart("Earnings", CategoryAxis(), NumberAxis()) {
        animated = false
    }

    private val chartLabels = mutableListOf<Text>()
    private val dateFormat = SimpleDateFormat("dd MMM, yy")

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

    fun fetch(symbol: String) {
        runAsync {
            Iex.getEarnings(symbol)
        } ui {
            it?.earnings?.asReversed()?.let { earnings ->
                chart.data.clear()

                chart.series("Year Ago") {
                    earnings.forEach {
                        data(it.fiscalPeriod, it.yearAgo)

                        node?.tooltip(it.yearAgo.toString())
                    }
                }
                chart.series("Estimated") {
                    earnings.forEach {
                        data(it.fiscalPeriod, it.estimatedEps)
                    }
                }
                chart.series("Actual") {
                    earnings.forEach {
                        data(it.fiscalPeriod, it.actualEps)
                    }
                }

                // The node (bar) representing the data is only available
                // when a series has rendered.
                chartLabels.forEach { it.removeFromParent() }
                chartLabels.clear()
                runLater {
                    chart.data.forEach { series ->
                        series.data.forEachIndexed { index, datum ->
                            datum.node?.apply {
                                val data = earnings[index]
                                tooltip("${dateFormat.format(data.epsReportDate)} (${data.announceTime})")

                                val text = Text(datum.yValue.toString()).apply {
                                    font = Font("Tahoma", labelFontSize)
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