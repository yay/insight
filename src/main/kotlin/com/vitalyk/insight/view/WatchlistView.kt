package com.vitalyk.insight.view

import com.vitalyk.insight.iex.IexApi
import com.vitalyk.insight.iex.TopsBean
import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.iex.toBean
import com.vitalyk.insight.ui.toolbox
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.MapChangeListener
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.TimeZone

// TODO: the app won't shutdown because of some background thread activity
class WatchlistUI : Fragment() {

    var symbol = SimpleStringProperty("")
    var watchlist = Watchlist()
    private val newYorkTimeZone = TimeZone.getTimeZone("America/New_York")
    private var lastTradeFormatter = SimpleDateFormat("dd MMM HH:mm:ss zzz")

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
        column("Bid Size", TopsBean::bidSizeProperty)
        column("Ask", TopsBean::askPriceProperty)
        column("Ask Size", TopsBean::askSizeProperty)
        column("Volume", TopsBean::volumeProperty)
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
            textfield(symbol) {
                promptText = "Add Symbol(s)"
                textProperty().onChange { value ->
                    this.text = value?.toUpperCase()
                }
                onKeyReleased = EventHandler { event ->
                    if (event.code == KeyCode.ENTER) {
                        addSymbol()
                    }
                }
            }
            button("Add").action {
                addSymbol()
            }
            togglebutton("Eastern time") {
                isSelected = false
                setOnAction {
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
        symbols.forEach {
            table.items.add(IexApi.Tops(symbol = it).toBean())
        }
        watchlist.addSymbols(symbols)
    }

    fun addSymbols(vararg symbols: String) {
        addSymbols(symbols.toList())
    }

    private fun getSymbolIndex(symbol: String): Int {
        table.items.forEachIndexed { index, topsBean ->
            if (topsBean.symbol == symbol) return index
        }
        return -1
    }

    private fun replaceItem(tops: IexApi.Tops): Int {
        val index = getSymbolIndex(tops.symbol)
        if (index >= 0) {
            table.items[index] = tops.toBean()
        }
        return index
    }

    private fun replaceOrAddItem(tops: IexApi.Tops) {
        val replacedAt = replaceItem(tops)
        if (replacedAt < 0) {
            table.items.add(tops.toBean())
        }
    }

    private fun removeSymbol(symbol: String) {
        val index = getSymbolIndex(symbol)
        if (index >= 0) table.items.removeAt(index)
    }

    var listener: MapChangeListener<String, IexApi.Tops>? = null

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
        addSymbols("ANET", "SQ", "AMD", "INTC", "AVGO", "MU", "NVDA", "SCHW", "NFLX", "AMAT", "SPOT")
    }
}

class WatchlistView : View("Watchlists") {

    val watchlist = WatchlistUI()
    val newslist = NewsList()

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
