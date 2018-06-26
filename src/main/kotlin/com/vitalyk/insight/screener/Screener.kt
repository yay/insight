package com.vitalyk.insight.screener

import com.vitalyk.insight.helpers.objectMapper
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.main.AppSettings
import com.vitalyk.insight.main.HttpClients
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.io.File

// Change since previous day close
// Change since this day open
// 2, 3, 4, 5 day change - you get it

data class ChangeSinceClose(
    val symbol: String,
    val close: Double,  // previous close
    val price: Double,  // current price
    val change: Double, // price/close ratio
    val changePercent: Double,
    val marketCap: Long
)

fun getChangeSinceClose(minClose: Double = 2.0, minCap: Long = 500_000_000): List<ChangeSinceClose> {
    val prevCloses = Iex.getPreviousDay()
    val lastTrades = Iex.getLastTrade()?.map { it.symbol to it }?.toMap()
    val stats = loadAssetStatsJson()

    val changes = mutableListOf<ChangeSinceClose>()
    if (prevCloses != null && lastTrades != null && stats != null) {
        prevCloses.mapNotNull { prevClose ->
            val symbol = prevClose.key
            val lastTrade = lastTrades[symbol]
            lastTrade?.let {
                val price = it.price
                val close = prevClose.value.close
                val cap = stats[symbol]?.marketCap
                val isExpensiveEnough = price > 0.0 && close >= minClose
                val isBigEnough = cap != null && cap >= minCap
                if (isExpensiveEnough && isBigEnough) {
                    val change = price / close
                    val changePct = (change - 1.0) * 100.0
                    changes.add(ChangeSinceClose(symbol, close, price, change, changePct, cap!!))
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

suspend fun getAssetStats(progress: ((done: Int, total: Int) -> Unit)? = null): Map<String, Iex.AssetStats> {
    val symbolMap = Iex.getSymbols()?.let { it.map { it.symbol to it }.toMap() } ?: emptyMap()
    val total = symbolMap.size
    var done = 0
    return symbolMap.map { entry ->
        if (progress != null)
            async {
                val stat = Iex.getAssetStats(entry.key)
                progress(++done, total)
                stat
            }
        else
            async { Iex.getAssetStats(entry.key) }
    }.mapNotNull { it.await() }.map { it.symbol to it }.toMap()
}

private val assetStatsMapType = objectMapper.typeFactory.constructMapType(
    Map::class.java, String::class.java, Iex.AssetStats::class.java
)

fun loadAssetStatsJson(): Map<String, Iex.AssetStats>? {
    val file = File(AppSettings.Paths.assetStats)
    if (!file.isFile) return null
    return objectMapper.readValue(file, assetStatsMapType)
}

fun main(args: Array<String>) = runBlocking {
    Iex.setOkHttpClient(HttpClients.main)

    getChangeSinceClose().forEach {
        println(it)
    }
}