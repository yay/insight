package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.AssetProfileFragment
import com.vitalyk.insight.fragment.DayChartFragment
import com.vitalyk.insight.helpers.toReadableNumber
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.IexSymbols
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.screener.ChangeSinceClose
import com.vitalyk.insight.screener.getChangeSinceClose
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.control.TextFormatter
import javafx.scene.control.ToggleGroup
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

    val marketCapProperty = SimpleLongProperty()
    var marketCap by marketCapProperty

    val industryProperty = SimpleStringProperty()
    var industry by industryProperty

    val sectorProperty = SimpleStringProperty()
    var sector by sectorProperty
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
        it.industry = industry
        it.sector = sector
        it
    }

private fun getChangeSinceCloseView(iex: Iex) = VBox().apply {
    vgrow = Priority.ALWAYS

    val items = getChangeSinceClose(iex, IexSymbols.assetStats, IexSymbols.companies, 2.0, 50_000_000)
        .map { it.toFxBean() }.asObservable()
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

        val toggleGroup = ToggleGroup()
        radiobutton ("50M+", toggleGroup) { isSelected = true }
        radiobutton ("500M+", toggleGroup)
        radiobutton("5B+", toggleGroup)
        radiobutton("50B+", toggleGroup)
        toggleGroup.selectedToggleProperty().addListener(ChangeListener { _, _, _ ->
            toggleGroup.selectedToggle?.let {
                val index = toggleGroup.toggles.indexOf(it)
                val minCap: Long = when (index) {
                    1 -> 500_000_000
                    2 -> 5_000_000_000
                    3 -> 50_000_000_000
                    else -> 50_000_000
                }
                // TODO: toggles don't work if tableview is sorted by some column
                // other than market cap
                filteredItems.predicate = {
                    it.marketCap >= minCap
                }
            }
        })

        spacer { }
        val upCount = items.count { it.changePercent >= 0.0 }
        val downCount = items.size - upCount
        label("${items.size} results, $upCount up, $downCount down")
    }

    fun showChart(symbol: String, range: Iex.Range) {
        runAsync {
            iex.getDayChartWithQuote(symbol, range) ?: mutableListOf()
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
    val newsFragment = NewsFragment()

    splitpane {
        vgrow = Priority.ALWAYS

        splitpane(orientation = Orientation.VERTICAL) {
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
                column("Industry", ChangeSinceCloseBean::industry)
                column("Sector", ChangeSinceCloseBean::sector)

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
                        iex.getDayChartWithQuote(symbol, Iex.Range.M3) ?: mutableListOf()
                    } ui { points ->
                        chart.updateChart(symbol, points)
                    }

                    profileFragment.fetch(symbol)

                    runLater {
                        newsFragment.symbol.value = symbol
                    }
                }
            }
            this += newsFragment.apply {
                toolbarVisible.set(false)
            }

            setDividerPositions(.75, .25)

        }
        splitpane(orientation = Orientation.VERTICAL) {
            this += chartBox
            this += profileBox
        }
    }
}

class ScreenerView : View("Screener") {

    private val iex = Iex(HttpClients.main)

    override val root = vbox {
        val vbox = this
        toolbar {
            button("Back").action { replaceWith(MainView::class) }
            button("Get Change Since Close").action {
                if (children.size > 1)
                    vbox.children.last().removeFromParent()
                runAsyncWithProgress {
                    getChangeSinceCloseView(iex)
                } ui {
                    vbox += it
                }
            }

//            button("Fetch stats") {
//                val label = Label().apply {
//                    style {
//                        fontSize = 0.9.em
//                        fontFamily = "Menlo"
//                        textFill = Color.CHOCOLATE
//                    }
//                }
//                graphic = label
//                onClickActor {
//                    val counter = actor<Int>(JavaFx) {
//                        var counter = 0
//                        for (total in channel) {
//                            label.text = "${++counter} / $total"
//                        }
//                    }
//                    iex.mapSymbolsWithProgress(iex::getAssetStats, counter)
//                        .toPrettyJson()
//                        .writeToFile(AppSettings.Paths.assetStats)
//                }
//            }
//
//            // TODO: it seems like only one (stats or companies) can run at a time
//            button("Fetch companies") {
//                val label = Label().apply {
//                    style {
//                        fontSize = 0.9.em
//                        fontFamily = "Menlo"
//                        textFill = Color.CHOCOLATE
//                    }
//                }
//                graphic = label
//                onClickActor {
//                    val counter = actor<Int>(JavaFx) {
//                        var counter = 0
//                        for (total in channel) {
//                            label.text = "${++counter} / $total"
//                        }
//                    }
//                    iex.mapSymbolsWithProgress(iex::getCompany, counter)
//                        .toPrettyJson()
//                        .writeToFile(AppSettings.Paths.companyInfo)
//                }
//            }
        }
    }
}
