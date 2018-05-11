package com.vitalyk.insight.fragment

import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.TopsBean
import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.iex.toBean
import com.vitalyk.insight.ui.symbolfield
import com.vitalyk.insight.ui.toolbox
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.*

// TODO: the app won't shutdown because of some background thread activity
class WatchlistFragment(val watchlist: Watchlist) : Fragment() {

    var symbol = SimpleStringProperty("")
    private val newYorkTimeZone = TimeZone.getTimeZone("America/New_York")
    // "dd MMM HH:mm:ss zzz"
    private var lastTradeTimeFormatter = SimpleDateFormat("HH:mm:ss")
    private val priceFormat = "%.2f"

    // https://github.com/edvin/tornadofx/wiki/TableView-SmartResize
    val table = tableview(mutableListOf<TopsBean>().observable()) {
        val table = this

        multiSelect()

        column("Symbol", TopsBean::symbolProperty)
        column("Last Trade", TopsBean::lastSalePriceProperty)
        column("Trade Time", TopsBean::lastSaleTimeProperty).cellFormat {
            if (it != null) {
                text = lastTradeTimeFormatter.format(it)
            }
        }
        column("Trade Size", TopsBean::lastSaleSizeProperty)
//        val doubleComparator = Comparator<Double> { a, b ->
//            when {
//                a === null && b === null -> 0
//                a === null -> -1
//                b === null -> 1
//                else -> Math.signum(a - b).toInt()
//            }
//        }
//        val col = TableColumn<TopsBean, Double>("Bid1").apply {
//            setCellFactory {
//                FlashingTableCell(doubleComparator)
//            }
//        }
//        addColumnInternal(col)
//        column("Bid", TopsBean::bidPriceProperty) {
//            setCellFactory {
//                FlashingTableCell(Comparator { a, b ->
//                    when {
//                        a === null && b === null -> 0
//                        a === null -> -1
//                        b === null -> 1
//                        else -> Math.signum(a.toDouble() - b.toDouble()).toInt()
//                    }
//                })
//            }
//        }
        column("Bid", TopsBean::bidPriceProperty)
        column("Ask", TopsBean::askPriceProperty)
        column<TopsBean, String>("Spread") {
            val data = it.value
            SimpleObjectProperty(priceFormat.format(data.bidPrice - data.askPrice))
        }
        column("Volume", TopsBean::volumeProperty)
        column("Bid Size", TopsBean::bidSizeProperty)
        column("Ask Size", TopsBean::askSizeProperty)
        column("Sector", TopsBean::sectorProperty)

        contextmenu {
            item("Remove").action {
                // TODO: removed items sometimes reappear
                val symbols = table.selectionModel.selectedItems.map { it.symbol }
                watchlist.removeSymbols(symbols)
                table.items.removeAll(table.selectionModel.selectedItems)
            }
        }

        vgrow = Priority.ALWAYS
    }

    var isEasternTime: Boolean = false
        set(value) {
            field = value
            lastTradeTimeFormatter.timeZone = if (value)
                newYorkTimeZone
            else
                TimeZone.getDefault()
        }

    override val root = vbox {
        vgrow = Priority.ALWAYS

        toolbox(border = false) {
            symbolfield(symbol, {
                promptText = "Add Symbol(s)"
                minWidth = 120.0
            }) {
                addSymbol()
            }
            button("Add").action {
                addSymbol()
            }
            togglebutton("Eastern time") {
                isSelected = false
                action {
                    isEasternTime = isSelected
                    table.refresh()
                }
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
            table.items.add(Iex.Tops(symbol = it).toBean())
        }
    }

    private fun getSymbolIndex(symbol: String): Int {
        table.items.forEachIndexed { index, topsBean ->
            if (topsBean.symbol == symbol) return index
        }
        return -1
    }

    private fun replaceItem(tops: Iex.Tops): Int {
        val index = getSymbolIndex(tops.symbol)
        if (index >= 0) {
            table.items[index] = tops.toBean()
        }
        return index
    }

    private fun replaceOrAddItem(tops: Iex.Tops) {
        val replacedAt = replaceItem(tops)
        if (replacedAt < 0) {
            table.items.add(tops.toBean())
        }
    }

    private fun removeSymbol(symbol: String) {
        val index = getSymbolIndex(symbol)
        if (index >= 0) table.items.removeAt(index)
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
        table.items.setAll(watchlist.tops.map { it.toBean() })
    }

    init {
        watchlist.addListener { old, new ->
            if (new != null) {
                val selected = table.selectionModel.selectedIndices.toList()
                replaceOrAddItem(new)
                selected.forEach { table.selectionModel.select(it) }
            } else {
                removeSymbol(old!!.symbol)
            }
        }
        refresh()
    }
}
