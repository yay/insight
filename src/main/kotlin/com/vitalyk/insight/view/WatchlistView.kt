package com.vitalyk.insight.view

import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.TopsBean
import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.iex.toBean
import com.vitalyk.insight.ui.symbolfield
import com.vitalyk.insight.ui.toolbox
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.MapChangeListener
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.*

// TODO: the app won't shutdown because of some background thread activity
class WatchlistUI(val watchlist: Watchlist) : Fragment() {

    var symbol = SimpleStringProperty("")
    private val newYorkTimeZone = TimeZone.getTimeZone("America/New_York")
    // "dd MMM HH:mm:ss zzz"
    private var lastTradeFormatter = SimpleDateFormat("HH:mm:ss")

    // https://github.com/edvin/tornadofx/wiki/TableView-SmartResize
    val table = tableview(mutableListOf<TopsBean>().observable()) {
        val table = this

        multiSelect()

        column("Symbol", TopsBean::symbolProperty)
        column("Last Trade", TopsBean::lastSalePriceProperty)
        column("Trade Time", TopsBean::lastSaleTimeProperty).cellFormat {
            if (it != null) {
                text = lastTradeFormatter.format(it)
            }
        }
        column("Trade Size", TopsBean::lastSaleSizeProperty)
        column("Bid", TopsBean::bidPriceProperty)
        column("Ask", TopsBean::askPriceProperty)
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
            lastTradeFormatter.timeZone = if (value)
                newYorkTimeZone
            else
                TimeZone.getDefault()
        }

    override val root = vbox {
        vgrow = Priority.ALWAYS

        toolbox(border = false) {
            symbolfield(symbol, { addSymbol() }, {
                promptText = "Add Symbol(s)"
            })
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

    var listener: MapChangeListener<String, Iex.Tops>? = null

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
        listener = watchlist.addListener { change ->
            change.valueAdded?.let { new ->
                val selected = table.selectionModel.selectedIndices.toList()
                replaceOrAddItem(new)
                selected.forEach { table.selectionModel.select(it) }
                change.valueRemoved?.let { old ->
                    val delta = new.lastSalePrice - old.lastSalePrice
                    when {
                        delta > 0.0 -> {}
                        delta < 0.0 -> {}
                        else -> Unit
                    }
                }
                Unit
            } ?: removeSymbol(change.valueRemoved.symbol)
        }
        watchlist.symbols.forEach {
            table.items.add(Iex.Tops(symbol = it).toBean())
        }
    }
}

class WatchlistView : View("Watchlists") {

    val watchlist = WatchlistUI(Watchlist["Main"] ?: Watchlist("Main"))
    val newslist = NewsFragment()

    private val tabpane = tabpane {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        tab("Main") {
            this += watchlist
        }
    }

    override val root = vbox {
        toolbox {
            button("Back").action { replaceWith(SymbolTableView::class) }
        }
        splitpane(Orientation.VERTICAL) {
            vgrow = Priority.ALWAYS
            this += tabpane
            this += newslist.apply {
                toolbox.hide()
            }
            setDividerPositions(.6, .4)

            watchlist.table.onSelectionChange {
                if (Platform.isFxApplicationThread()) {
                    it?.let {
                        newslist.symbol.value = it.symbol
                    }
                }
            }
        }
    }
}
