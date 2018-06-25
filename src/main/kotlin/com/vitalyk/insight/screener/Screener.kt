package com.vitalyk.insight.screener

import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.main.HttpClients
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

// Change since previous day close
// Change since this day open
// 2, 3, 4, 5 day change - you get it

data class ChangeSinceClose(
    val symbol: String,
    val close: Double,  // previous close
    val price: Double,  // current price
    val change: Double, // price/close ratio
    val changePercent: Double
)

fun getChangeSinceClose(): List<ChangeSinceClose> {
    val prevCloses = Iex.getPreviousDay()
    val lastTrades = Iex.getLastTrade()?.map { it.symbol to it }?.toMap()

    val changes = mutableListOf<ChangeSinceClose>()
    if (prevCloses != null && lastTrades != null) {
        prevCloses.mapNotNull { prevClose ->
            val symbol = prevClose.key
            val lastTrade = lastTrades[symbol]
            lastTrade?.let {
                val price = it.price
                val close = prevClose.value.close
                if (price > 0.0 && close > 1.0) {
                    val change = price / close
                    val changePct = (change - 1.0) * 100.0
                    changes.add(ChangeSinceClose(symbol, close, price, change, changePct))
                }
            }
        }
    }

    return changes.sortedByDescending { it.change }
}

fun getChangeSinceOpen() {

}

fun getChange(dayCount: Int) {

}

fun getNewHighs() {

}

fun getNewLows() {

}

fun getDailyHighCount() {

}

fun main(args: Array<String>) = runBlocking {
    Iex.setOkHttpClient(HttpClients.main)

//    val symbolMap = Iex.getSymbols()?.let { it.map { it.symbol to it }.toMap() } ?: emptyMap()
//    val quotes = symbolMap.map { entry ->
//        async { Iex.getQuote(entry.key) }
//    }.mapNotNull { it.await() }.map { it.symbol to it }.toMap()

    getChangeSinceClose().forEach {
        println(it)
    }
}