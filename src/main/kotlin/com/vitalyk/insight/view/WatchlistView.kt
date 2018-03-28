package com.vitalyk.insight.view

import com.vitalyk.insight.iex.DayChartPointBean
import com.vitalyk.insight.iex.TopsBean
import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.iex.toBean
import javafx.beans.property.SimpleStringProperty
import javafx.collections.MapChangeListener
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import tornadofx.*


class WatchlistUI : Fragment() {

    var symbol = SimpleStringProperty("")
    var watchlist = Watchlist()

    val symbolTable = tableview(mutableListOf<TopsBean>().observable()) {
        column("Symbol", TopsBean::symbolProperty)
        column("Last Trade", TopsBean::lastSalePriceProperty)
        column("Bid", TopsBean::bidPriceProperty)
        column("Ask", TopsBean::askPriceProperty)
        column("Volume", TopsBean::volumeProperty)

        vgrow = Priority.ALWAYS
    }

    override val root = vbox {
        vgrow = Priority.ALWAYS

        hbox {
            spacing = 10.0
            padding = Insets(10.0)
            alignment = Pos.CENTER_LEFT

            textfield(symbol) {
                promptText = "Add Symbol(s)"
                textProperty().onChange { value ->
                    this.text = value?.toUpperCase()
                }
                onKeyReleased = EventHandler { key ->
                    if (key.code == KeyCode.ENTER) {
                        watchlist.add(symbol.value)
                        symbol.value = ""
//                        updateSymbolTable()
                    }
                }
            }
            button("Add") {
                setOnAction {

                }
            }
        }

        this += symbolTable
    }

    init {
        watchlist.addListener(MapChangeListener { change ->
            val items = symbolTable.items
            val old = items.find { it.symbol == change.valueAdded.symbol }
            val oldIdx = items.indexOf(old)
            if (oldIdx >= 0) {
//                symbolTable.row
                items[oldIdx] = change.valueAdded.toBean()
            } else {
                items.add(change.valueAdded.toBean())
            }
        })
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
