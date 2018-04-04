package com.vitalyk.insight.view

import com.vitalyk.insight.iex.IexApi
import com.vitalyk.insight.iex.TopsBean
import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.iex.toBean
import com.vitalyk.insight.ui.toolbox
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import tornadofx.*


class WatchlistUI : Fragment() {

    var symbol = SimpleStringProperty("")
    var watchlist = Watchlist()

    private val table = tableview(mutableListOf<TopsBean>().observable()) {
        val table = this

        multiSelect()

        column("Symbol", TopsBean::symbolProperty)
        column("Last Trade", TopsBean::lastSalePriceProperty)
        column("Bid", TopsBean::bidPriceProperty)
        column("Ask", TopsBean::askPriceProperty)
        column("Volume", TopsBean::volumeProperty)

        contextmenu {
            item("Remove").action {
                val symbols = table.selectionModel.selectedItems.map { it.symbol }
                watchlist.remove(symbols)
                table.items.removeAll(table.selectionModel.selectedItems)
            }
        }

        vgrow = Priority.ALWAYS
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
        watchlist.add(symbols)
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

    init {
        watchlist.addListener { change ->
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

    private val tabpane = tabpane {
        hgrow = Priority.ALWAYS

        tab("Main") {
            this += WatchlistUI()
        }
    }

    override val root = hbox {
        setMinSize(400.0, 300.0)

        this += tabpane
    }
}
