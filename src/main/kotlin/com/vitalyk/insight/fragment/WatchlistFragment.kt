package com.vitalyk.insight.fragment

import com.vitalyk.insight.helpers.newYorkTimeZone
import com.vitalyk.insight.iex.*
import com.vitalyk.insight.ui.ChangeBlinkTableCell
import com.vitalyk.insight.ui.symbolfield
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.TableColumn
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.concurrent.Callable

class WatchlistFragment(private val watchlist: Watchlist) : Fragment() {

    private val priceFormat = "%.2f"
    private val percentFormat = "%.2f%%"
    private val priceFormatter = { price: Double -> priceFormat.format(price) }

    var symbol = SimpleStringProperty("")
    private var lastTradeTimeFormatter = SimpleDateFormat("HH:mm:ss").apply {
        timeZone = newYorkTimeZone
    }

    val tableItems = FXCollections.observableArrayList<TopsBean>()

    // https://github.com/edvin/tornadofx/wiki/TableView-SmartResize
    val table = tableview(tableItems) {
        val table = this

        multiSelect()

        column("Symbol", TopsBean::symbolProperty)
        TableColumn<TopsBean, String>("% Change").apply {
            setCellValueFactory {
                Bindings.createStringBinding(Callable {
                    val prevDayClose = IexSymbols.previousDay(it.value.symbol)?.close
                    if (prevDayClose != null)
                        percentFormat.format((it.value.lastSalePrice / prevDayClose - 1.0) * 100.0)
                    else
                        "--"
                }, it.value.lastSalePriceProperty)
            }
            table.columns.add(this)
        }
        TableColumn<TopsBean, Double>("Last Trade").apply {
            setCellValueFactory { it.value.lastSalePriceProperty.asObject() }
            setCellFactory {
                ChangeBlinkTableCell<TopsBean, Double>(naturalOrder(), priceFormatter)
            }
            table.columns.add(this)
        }
        column("Trade Time", TopsBean::lastSaleTimeProperty).cellFormat {
            if (it != null) {
                text = lastTradeTimeFormatter.format(it)
            }
        }
        column("Trade Size", TopsBean::lastSaleSizeProperty)
        TableColumn<TopsBean, Double>("Bid").apply {
            setCellValueFactory { it.value.bidPriceProperty.asObject() }
            setCellFactory {
                ChangeBlinkTableCell<TopsBean, Double>(naturalOrder(), priceFormatter)
            }
            table.columns.add(this)
        }
        TableColumn<TopsBean, Double>("Ask").apply {
            setCellValueFactory { it.value.askPriceProperty.asObject() }
            setCellFactory {
                ChangeBlinkTableCell<TopsBean, Double>(naturalOrder(), priceFormatter)
            }
            table.columns.add(this)
        }
        TableColumn<TopsBean, String>("Spread").apply {
            setCellValueFactory {
                Bindings.createStringBinding(Callable {
                        priceFormat.format(it.value.bidPrice - it.value.askPrice)
                    },
                    it.value.bidPriceProperty,
                    it.value.askPriceProperty
                )
            }
            table.columns.add(this)
        }
        column("Volume", TopsBean::volumeProperty)
        column("Bid Size", TopsBean::bidSizeProperty)
        column("Ask Size", TopsBean::askSizeProperty)
        column("Sector", TopsBean::sectorProperty)

        // TODO: add "change since last close" column

        contextmenu {
            item("Remove").action {
                // TODO: removed items sometimes reappear
                val symbols = table.selectionModel.selectedItems.map { it.symbol }
                watchlist.removeSymbols(symbols)
                tableItems.removeAll(table.selectionModel.selectedItems)
            }
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
                separator()
                item("Minute").action {
                    selectedItem?.let { selectedItem ->
                        // TODO: can find out trading days by looking at chart data
                        // for VOO or SPY for example
                        runAsync {
                            Iex.getMinuteChart(selectedItem.symbol, "20180518") ?: emptyList()
                        } ui { points ->
                            find(MinuteChartFragment::class).let {
                                it.updateChart(selectedItem.symbol, points)
                                it.openModal()
                            }
                        }
                    }
                }
            }
        }

        vgrow = Priority.ALWAYS
    }

    override val root = vbox {
        vgrow = Priority.ALWAYS

        toolbar {
            symbolfield(symbol, {
                promptText = "Add Symbol(s)"
                minWidth = 120.0
            }) {
                addSymbol()
            }
            button("Add").action {
                addSymbol()
            }
        }

        this += table
    }

    private fun addSymbol() {
        addSymbols(symbol.value.split(" "))
        symbol.value = ""
    }

    fun addSymbols(symbols: List<String>) {
        watchlist.addSymbols(symbols).forEach {
            tableItems.add(Iex.Tops(symbol = it).toBean())
        }
    }

    private fun getSymbolIndex(symbol: String): Int {
        tableItems.forEachIndexed { index, topsBean ->
            if (topsBean.symbol == symbol) return index
        }
        return -1
    }

    private fun replaceItem(tops: Iex.Tops): Int {
        val index = getSymbolIndex(tops.symbol)
        if (index >= 0) {
            tops.toBean(tableItems[index])
        }
        return index
    }

    private fun replaceOrAddItem(tops: Iex.Tops) {
        val replacedAt = replaceItem(tops)
        if (replacedAt < 0) {
            tableItems.add(tops.toBean())
        }
    }

    private fun removeSymbol(symbol: String) {
        val index = getSymbolIndex(symbol)
        if (index >= 0) tableItems.removeAt(index)
    }

//    override fun onDock() {
//        super.onDock()
//        refresh()
//    }
//
//    override fun onUndock() {
//        super.onUndock()
//        listener?.let { watchlist.removeListener(it) }
//    }

    private fun refresh() {
        tableItems.setAll(watchlist.tops.map { it.toBean() })
    }

    init {
        watchlist.addListener { old, new ->
            if (new != null) runLater {
                val selected = table.selectionModel.selectedIndices.toList()
                replaceOrAddItem(new)
                selected.forEach { table.selectionModel.select(it) }
            } else if (old != null) runLater {
                removeSymbol(old.symbol)
            }
        }
        refresh()
    }
}
