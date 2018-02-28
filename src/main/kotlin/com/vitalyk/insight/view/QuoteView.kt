package com.vitalyk.insight.view

import com.vitalyk.insight.iex.IexApi1
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import tornadofx.*
import java.util.*
import kotlinx.coroutines.experimental.javafx.JavaFx as UI

open class StockQuote(
    latestUpdate: Date,
    symbol: String,
    companyName: String,
    latestPrice: Double,
    change: Double,
    changePercent: Double
) {
    var latestUpdate: Date by property(latestUpdate)
    fun latestUpdateProperty() = getProperty(StockQuote::latestUpdate)

    var symbol: String by property(symbol)
    fun symbolProperty() = getProperty(StockQuote::symbol)

    var companyName: String by property(companyName)
    fun companyNameProperty() = getProperty(StockQuote::companyName)

    var latestPrice: Double by property(latestPrice)
    fun latestPriceProperty() = getProperty(StockQuote::latestPrice)

    var change: Double by property(change)
    fun changeProperty() = getProperty(StockQuote::change)

    var changePercent: Double by property(changePercent)
    fun changePercentProperty() = getProperty(StockQuote::changePercent)
}

class QuoteViewStyles : Stylesheet() {
    init {
        label {
            and(selected) {
                textFill = Color.WHITE
            }
        }
    }
}

fun EventTarget.makeQuoteList(title: String, fn: () -> List<IexApi1.Quote>) = vbox {
    hgrow = Priority.ALWAYS
    label(title) {
        alignment = Pos.CENTER
        maxWidth = Double.MAX_VALUE
        minHeight = 30.0
        style {
            font = Font.font(null, FontWeight.BOLD, 15.0)
        }
    }
    listview<IexApi1.Quote> {
        val self = this
        val labelFont = Font.font(15.0)

        vgrow = Priority.ALWAYS

        cellCache {
            vbox {
                val changeColor = if (it.change > 0) Color.GREEN else Color.RED
                hbox {
                    label(it.symbol) {
                        textFill = Color.DODGERBLUE
                        font = Font.font("Verdana", FontWeight.BOLD, 15.0)
                        minWidth = 120.0
                    }
                    path {
                        moveTo(0.0, 0.0)
                        lineTo(12.0, 0.0)
                        lineTo(6.0, 10.0 * if (it.change > 0) 1.0 else -1.0)
                        closepath()
                        translateX = -8.0
                        translateY = 5.0
                        fill = changeColor
                        stroke = Color.WHITE
                        strokeWidth = 1.0
                    }
                    label("%.2f".format(it.change)) {
                        textFill = changeColor
                        font = labelFont
                    }
                    val changePercent = "%.2f".format(it.changePercent)
                    label(" ($changePercent%)") {
                        textFill = changeColor
                        font = labelFont
                    }
                    region {
                        hgrow = Priority.ALWAYS
                    }
                    label("${it.latestPrice}") {
                        font = labelFont
                        padding = Insets(0.0, 0.0, 0.0, 20.0)
                    }
                }
                label(it.companyName) {
                    textFill = Color.GRAY
                }
            }
        }

        launch(UI) {
            while (self.isVisible) {
                val mostActive = async {
                    fn()
                }
                val quotes = mostActive.await()

                val selectedSymbol = self.selectedItem?.symbol
                self.items.setAll(quotes)
                selectedSymbol?.let {
                    val index = self.items.indexOfFirst { item -> item.symbol == selectedSymbol }
                    self.selectionModel.select(index)
                }

                delay(1000L)
            }
        }
    }
}

class QuoteView : View("Quotes") {

    lateinit var symbolTable: TableView<StockQuote>
    lateinit var symbolList: ListView<IexApi1.Quote>

//    override val root = borderpane {
//
//    }
    override val root = hbox {
        /*
        vbox {
            //        symbolTable = tableview(listOf<StockQuote>().observable()) {
    //            column("Time", StockQuote::latestUpdateProperty)
    //            column("Symbol", StockQuote::symbolProperty)
    //            column("Company", StockQuote::companyNameProperty)
    //            column("Last Price", StockQuote::latestPriceProperty)
    //            column("Change", StockQuote::changeProperty)
    //            column("Change %", StockQuote::changePercentProperty)
    //
    //            vgrow = Priority.ALWAYS
    //        }
            label("Most Active") {
                alignment = Pos.CENTER
                maxWidth = Double.MAX_VALUE
                minHeight = 30.0
                style {
                    font = Font.font(null, FontWeight.BOLD, 15.0)
                }
            }
            symbolList = listview {
                val self = this
                val labelFont = Font.font(15.0)

                vgrow = Priority.ALWAYS

                cellCache {
                    vbox {
                        val changeColor = if (it.change > 0) Color.GREEN else Color.RED
                        hbox {
                            label(it.symbol) {
                                textFill = Color.DODGERBLUE
                                font = Font.font("Verdana", FontWeight.BOLD, 15.0)
                                minWidth = 120.0
                            }
                            path {
                                moveTo(0.0, 0.0)
                                lineTo(12.0, 0.0)
                                lineTo(6.0, 10.0 * if (it.change > 0) 1.0 else -1.0)
                                closepath()
                                translateX = -8.0
                                translateY = 5.0
                                fill = changeColor
                                stroke = Color.WHITE
                                strokeWidth = 1.0
                            }
                            label("%.2f".format(it.change)) {
                                textFill = changeColor
                                font = labelFont
                            }
                            val changePercent = "%.2f".format(it.changePercent)
                            label(" ($changePercent%)") {
                                textFill = changeColor
                                font = labelFont
                            }
                            region {
                                hgrow = Priority.ALWAYS
                            }
                            label("${it.latestPrice}") {
                                font = labelFont
                                padding = Insets(0.0, 0.0, 0.0, 20.0)
                            }
                        }
                        label(it.companyName) {
                            textFill = Color.GRAY
                        }
                    }
                }

                launch(UI) {
                    while (self.isVisible) {
                        val mostActive = async {
                            IexApi1.getMostActive()
                        }
                        val quotes = mostActive.await()

                        val selectedSymbol = self.selectedItem?.symbol
                        self.items.setAll(quotes)
                        selectedSymbol?.let {
                            val index = self.items.indexOfFirst { item -> item.symbol == selectedSymbol }
                            self.selectionModel.select(index)
                        }

                        delay(1000L)
                    }
                }
            }
        }
        */
        makeQuoteList("Most active", IexApi1::getMostActive)
        makeQuoteList("Gainers", IexApi1::getGainers)
        makeQuoteList("Losers", IexApi1::getLosers)
    }

    init {
//        launch(UI) {
//            while (true) {
//                val mostActive = async {
//                    IexApi1.getMostActive()
//                }
//
//                val quotes = mostActive.await()
//                symbolList.items.setAll(quotes)
//
////                val stockQuotes = quotes.map { quote ->
////                    StockQuote(
////                        quote.latestUpdate,
////                        quote.symbol,
////                        quote.companyName,
////                        quote.latestPrice,
////                        quote.change,
////                        quote.changePercent
////                    )
////                }
////                symbolTable.items = stockQuotes.observable()
//
//
////                symbolList.items = quotes.observable()
//
////                symbolList = listview {
////                    cellCache {
////                        vbox {
////                            label()
////                        }
////                    }
////                }
//
//                delay(1000L)
//            }
//        }

//        val socket = IO.socket("https://ws-api.iextrading.com/1.0/tops")
//
//        val tops = mutableListOf<IexApi1.Tops>()
//
//        socket
//            .on(Socket.EVENT_CONNECT, {
////                socket.emit("subscribe", "firehose") // all symbols
//            socket.emit("subscribe", "anet,snap,fb,c,bac")
////            socket.emit("unsubscribe", "aig+")
//                //        socket.disconnect()
//            })
//            .on("message", { params ->
////                val top = IexApi1.parseTops(params.first() as String)
////                val oldItem = symbolTable.items.find { item -> item.symbol === top.symbol }
////                if (oldItem != null) {
////                    oldItem.latestUpdate = top.lastUpdated
////                }
//                println(IexApi1.parseTops(params.first() as String))
//            })
//            .on(Socket.EVENT_DISCONNECT, { println("Disconnected.") })
//
//        socket.connect()
    }
}
