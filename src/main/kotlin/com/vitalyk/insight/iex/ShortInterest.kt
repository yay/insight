package com.vitalyk.insight.iex

import com.fasterxml.jackson.annotation.JsonFormat
import com.vitalyk.insight.helpers.toPrettyJson
import com.vitalyk.insight.helpers.toReadableNumber
import com.vitalyk.insight.helpers.writeToFile
import com.vitalyk.insight.iex.Iex.AssetStats
import com.vitalyk.insight.iex.Iex.Quote
import com.vitalyk.insight.iex.Iex.Symbol
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.time.LocalDate

//fun main(args: Array<String>) {
//    Iex.setOkHttpClient(HttpClients.main)
//    getShortInterest()
//}

fun filterShortInterest(stats: List<AssetStats>, quotes: Map<String, Quote>) {
    stats
        .filter {
            it.marketCap > 1000_000_000
        }
        .mapNotNull {
            val quote = quotes[it.symbol]
            val shortInterest = it.shortInterest.toDouble()
            val shortInterestPct = shortInterest / it.sharesOutstanding.toDouble()
            if (quote != null)
                object {
                    val shortInterest = "%.2f%%".format(shortInterestPct * 100)
                    val shortDate = it.shortDate
                    val symbol = it.symbol
                    val name = it.companyName
                    @JsonFormat(pattern = "MMM dd, yy")
                    val marketCap = it.marketCap.toReadableNumber()
                    val daysToCover = shortInterest / quote.avgTotalVolume.toDouble()
                }
            else null
        }
        .toPrettyJson()
        .writeToFile("./data/filtered_short_interest.json")
}

fun getShortInterest(iex: Iex) = runBlocking {
    val monthAgo = LocalDate.now().minusMonths(1)
    iex.getSymbols()?.let {
        // Quote fetching will run concurrently with stats fetching,
        // and when both are fetched, the filtering function will be called.
        val quoteJob = async { getQuotes(iex, it) }

        val list = it.map {
            async {
                iex.getAssetStats(it.symbol)
            }
        }.mapNotNull {
            it.await()
        }.mapNotNull {
            val shortDate = it.shortDate
            if (it.shortInterest > 0 && it.sharesOutstanding > 0 &&
                shortDate != null && shortDate.isAfter(monthAgo))
                it
            else
                null
        }.sortedByDescending {
            it.shortInterest.toDouble() / it.sharesOutstanding.toDouble()
        }

        list.toPrettyJson().writeToFile("./data/short_interest.json")

        filterShortInterest(list, quoteJob.await())
    }
}

suspend fun getQuotes(iex: Iex, symbols: List<Symbol>): Map<String, Quote> {
    return symbols.map {
        async {
            iex.getQuote(it.symbol)
        }
    }.mapNotNull {
        it.await()
    }.map {
        it.symbol to it
    }.toMap()
}