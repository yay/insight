package com.vitalyk.insight.fragment

import com.vitalyk.insight.helpers.newYorkTimeZone
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.TopsBean
import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.iex.toBean
import com.vitalyk.insight.ui.HighlightTableCell
import com.vitalyk.insight.ui.symbolfield
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.util.Callback
import tornadofx.*
import tornadofx.Stylesheet.Companion.filled
import java.text.SimpleDateFormat
import java.util.*

// TODO: the app won't shutdown because of some background thread activity
class WatchlistFragment(val watchlist: Watchlist) : Fragment() {

    var symbol = SimpleStringProperty("")
    private var lastTradeTimeFormatter = SimpleDateFormat("HH:mm:ss").apply {
        timeZone = newYorkTimeZone
    }
    private val priceFormat = "%.2f"

    val tableItems = FXCollections.observableArrayList<TopsBean>()

    // https://github.com/edvin/tornadofx/wiki/TableView-SmartResize
    val table = tableview(tableItems) {
        val table = this

        multiSelect()

        column("Symbol", TopsBean::symbolProperty)
        TableColumn<TopsBean, Double>("Last Trade").apply {
            setCellValueFactory { it.value.lastSalePriceProperty().asObject() }
            setCellFactory {
                HighlightTableCell<TopsBean, Double>(
                    nullsLast<Double>() as Comparator<Double>,
                    { "%.2f".format(it) }
                )
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
            setCellValueFactory { it.value.bidPriceProperty().asObject() }
            setCellFactory {
                HighlightTableCell<TopsBean, Double>(
                    nullsLast<Double>() as Comparator<Double>,
                    { "%.2f".format(it) }
                )
            }
            table.columns.add(this)
        }
        TableColumn<TopsBean, Double>("Ask").apply {
            setCellValueFactory { it.value.askPriceProperty().asObject() }
            setCellFactory {
                HighlightTableCell<TopsBean, Double>(
                    nullsLast<Double>() as Comparator<Double>,
                    { "%.2f".format(it) }
                )
            }
            table.columns.add(this)
        }
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
                tableItems.removeAll(table.selectionModel.selectedItems)
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
