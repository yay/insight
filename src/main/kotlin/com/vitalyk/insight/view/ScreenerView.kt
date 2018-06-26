package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.AssetProfileFragment
import com.vitalyk.insight.fragment.DayChartFragment
import com.vitalyk.insight.helpers.toPrettyJson
import com.vitalyk.insight.helpers.toReadableNumber
import com.vitalyk.insight.helpers.writeToFile
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.main.AppSettings
import com.vitalyk.insight.screener.ChangeSinceClose
import com.vitalyk.insight.screener.getAssetStats
import com.vitalyk.insight.screener.getChangeSinceClose
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextFormatter
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.javafx.JavaFx
import tornadofx.*

class ChangeSinceCloseBean {
    val symbolProperty = SimpleStringProperty()
    var symbol by symbolProperty

    val closeProperty = SimpleDoubleProperty()
    var close by closeProperty

    val priceProperty = SimpleDoubleProperty()
    var price by priceProperty

    val changeProperty = SimpleDoubleProperty()
    var change by changeProperty

    val changePercentProperty = SimpleDoubleProperty()
    var changePercent by changePercentProperty

    val marketCapProperty = SimpleLongProperty()
    var marketCap by marketCapProperty
}

fun ChangeSinceClose.toFxBean(): ChangeSinceCloseBean =
    ChangeSinceCloseBean().let {
        it.symbol = symbol
        it.close = close
        it.price = price
        it.close = close
        it.change = change
        it.changePercent = changePercent
        it.marketCap = marketCap
        it
    }

private fun getChangeSinceCloseView() = VBox().apply {
    vgrow = Priority.ALWAYS

    val items = getChangeSinceClose().map { it.toFxBean() }.observable()
    val filteredItems = SortedFilteredList(items)

    toolbar {
        textfield {
            promptText = "Symbol"

            textFormatter = TextFormatter<String> {
                it.text = it.text.toUpperCase()
                it
            }

            textProperty().onChange { value ->
                filteredItems.predicate = {
                    value != null && it.symbol.startsWith(value)
                }
            }
        }
        spacer { }
        val upCount = items.count { it.changePercent >= 0.0 }
        val downCount = items.size - upCount
        label("${items.size} results, $upCount up, $downCount down")
    }

    fun showChart(symbol: String, range: Iex.Range) {
        runAsync {
            Iex.getDayChart(symbol, range) ?: emptyList()
        } ui { points ->
            find(DayChartFragment::class).let {
                it.updateChart(symbol, points)
                it.openModal()
            }
        }
    }

    val chartBox = VBox()
    val profileFragment = AssetProfileFragment()
    val profileBox = VBox().apply {
        this += profileFragment
    }

    splitpane {
        vgrow = Priority.ALWAYS

        tableview(filteredItems) {
            vgrow = Priority.ALWAYS

            column("Symbol", ChangeSinceCloseBean::symbol)
            column("Change %", ChangeSinceCloseBean::changePercent).cellFormat {
                text = "%.2f".format(it)
            }
            column("Close", ChangeSinceCloseBean::close)
            column("Price", ChangeSinceCloseBean::price)
            column("Change", ChangeSinceCloseBean::change).cellFormat {
                text = "%.2f".format(it)
            }
            column("Mkt Cap", ChangeSinceCloseBean::marketCap).cellFormat {
                text = it.toReadableNumber()
            }

            contextmenu {
                menu("Chart") {
                    Iex.Range.values().forEach { range ->
                        item(range.value.name).action {
                            selectedItem?.let { showChart(it.symbol, range) }
                        }
                    }
                }
            }

            onUserSelect {
                val symbol = it.symbol
                val chart = DayChartFragment()
                chartBox.children.clear()
                chartBox += chart

                runAsync {
                    Iex.getDayChart(symbol, Iex.Range.M3) ?: emptyList()
                } ui { points ->
                    chart.updateChart(symbol, points)
                }

                profileFragment.fetch(symbol)
            }
        }
        splitpane(orientation = Orientation.VERTICAL) {
            this += chartBox
            this += profileBox
        }
    }
}

class ScreenerView : View("Screener") {
    override val root = vbox {
        val vbox = this
        toolbar {
            button("Back").action { replaceWith(MainView::class) }
            button("Change since close").action {
                if (children.size > 1)
                    vbox.children.last().removeFromParent()
                runAsyncWithProgress {
                    getChangeSinceCloseView()
                } ui {
                    vbox += it
                }
            }
            val statProgressLabel = Label()
            fun Button.onClickActor(action: suspend (MouseEvent) -> Unit) {
                val eventActor = actor<MouseEvent>(JavaFx) {
                    for (event in channel) action(event)
                }
                onMouseClicked = EventHandler { event ->
                    eventActor.offer(event)
                }
            }
            button("Fetch stats") {
                onClickActor {
                    val stats = getAssetStats { done, total ->
                        runLater { statProgressLabel.text = "$done / $total" }
                    }
                    stats.toPrettyJson().writeToFile(AppSettings.Paths.assetStats)
                }
            }
            this += statProgressLabel
        }
    }
}
