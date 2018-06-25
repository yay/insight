package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.DayChartFragment
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.screener.ChangeSinceClose
import com.vitalyk.insight.screener.getChangeSinceClose
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.scene.control.TextFormatter
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
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
}

fun ChangeSinceClose.toFxBean(): ChangeSinceCloseBean =
    ChangeSinceCloseBean().let {
        it.symbol = symbol
        it.close = close
        it.price = price
        it.close = close
        it.change = change
        it.changePercent = changePercent
        it
    }

private fun getChangeSinceCloseView() = VBox().apply {
    vgrow = Priority.ALWAYS

    val items = getChangeSinceClose().map { it.toFxBean() }.observable()
    val filteredItems = FilteredList(items)

    toolbar {
        textfield {
            promptText = "Symbol"

            textFormatter = TextFormatter<String> {
                it.text = it.text.toUpperCase()
                it
            }

            textProperty().onChange { value ->
                filteredItems.setPredicate {
                    value != null && it.symbol.startsWith(value)
                }
            }
        }
    }

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

        contextmenu {
            menu("Chart") {
                Iex.Range.values().forEach { range ->
                    item(range.value.name).action {
                        selectedItem?.let { selectedItem ->
                            runAsync {
                                Iex.getDayChart(selectedItem.symbol, range) ?: emptyList()
                            } ui { points ->
                                find(DayChartFragment::class).let {
                                    it.updateChart(selectedItem.symbol, points)
                                    it.openModal()
                                }
                            }
                        }
                    }
                }
            }
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
                vbox += getChangeSinceCloseView()
            }
        }
    }
}
