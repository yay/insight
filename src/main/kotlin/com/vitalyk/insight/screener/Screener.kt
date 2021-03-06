package com.vitalyk.insight.screener

import com.vitalyk.insight.helpers.objectMapper
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.IexSymbols
import com.vitalyk.insight.main.AppSettings
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
    val marketCap: Long,
    val industry: String = "",
    val sector: String = ""
)

fun getChangeSinceClose(
    iex: Iex,
    stats: Map<String, Iex.AssetStats>?,
    companies: Map<String, Iex.Company>?,
    minClose: Double,
    minCap: Long
): List<ChangeSinceClose> {
    val prevCloses = iex.getPreviousDay()
    val lastTrades = iex.getLastTrade()?.map { it.symbol to it }?.toMap()
    val blacklist = IexSymbols.blacklist

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
                if (isExpensiveEnough && isBigEnough && symbol !in blacklist) {
                    val change = price / close
                    val changePct = (change - 1.0) * 100.0
                    val company = companies?.get(symbol)
                    val changeSinceClose = ChangeSinceClose(
                        symbol,
                        close,
                        price,
                        change,
                        changePct,
                        cap!!,
                        company?.industry ?: "",
                        company?.sector ?: ""
                    )
                    changes.add(changeSinceClose)
                }
            }
        }
    }

    return changes.sortedByDescending { it.change }
}

// The number of new 52-week highs and lows today
data class HighsLows(
    val highs: List<String>,
    val lows: List<String>
)

fun getHighsLows(iex: Iex, stats: Map<String, Iex.AssetStats>?, minCap: Long): HighsLows? {
    val lastTrades = iex.getLastTrade()?.map { it.symbol to it }?.toMap()

    data class Company(
        val symbol: String,
        val cap: Long
    )

    return if (lastTrades != null && stats != null) {
        val highs = mutableListOf<Company>()
        val lows = mutableListOf<Company>()
        lastTrades.forEach {
            val symbol = it.key
            val trade = it.value
            stats[symbol]?.let { stat ->
                val cap = stat.marketCap
                if (cap >= minCap) {
                    if (trade.price > stat.week52high) highs.add(Company(symbol, cap))
                    if (trade.price < stat.week52low) lows.add(Company(symbol, cap))
                }
            }
        }
        highs.sortByDescending { it.cap }
        lows.sortByDescending { it.cap }
        HighsLows(highs.map { it.symbol }, lows.map { it.symbol })
    } else null
}

data class AdvancersDecliners(
    val advancerCount: Int,
    val declinerCount: Int
)

fun getAdvancersDecliners(iex: Iex, minPrice: Double = 0.0): AdvancersDecliners? {
    val prevCloses = iex.getPreviousDay()
    val lastTrades = iex.getLastTrade()?.map { it.symbol to it }?.toMap()

    var advancerCount = 0
    var declinerCount = 0

    return if (prevCloses != null && lastTrades != null) {
        lastTrades.forEach {
            val symbol = it.key
            val last = it.value
            prevCloses[symbol]?.let { prev ->
                val lastPrice = last.price
                if (lastPrice >= minPrice) {
                    if (lastPrice > prev.close) advancerCount++
                    if (lastPrice <= prev.close) declinerCount++
                }
            }
        }
        AdvancersDecliners(advancerCount, declinerCount)
    } else null
}

private val assetStatsMapType = objectMapper.typeFactory.constructMapType(
    Map::class.java, String::class.java, Iex.AssetStats::class.java
)

fun loadAssetStatsJson(): Map<String, Iex.AssetStats>? {
    val file = File(AppSettings.Paths.assetStats)
    if (!file.isFile) return null
    return objectMapper.readValue(file, assetStatsMapType)
}