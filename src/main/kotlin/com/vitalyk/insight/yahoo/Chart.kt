package com.vitalyk.insight.yahoo

import com.fasterxml.jackson.module.kotlin.readValue
import com.vitalyk.insight.helpers.objectMapper
import com.vitalyk.insight.helpers.toPrettyJson
import java.io.IOException
import java.util.*

data class ChartResponse(
    val chart: ChartResult
)

data class ChartResult(
    val result: List<Chart>,
    val error: String?
)

data class Chart(
    val meta: ChartMeta,
    val timestamp: List<Date>,
    val indicators: Indicators
)

data class Indicators(
    val quote: List<Quote>
)

data class Quote(
    val volume: List<Long>,
    val close: List<Double>,
    val high: List<Double>,
    val low: List<Double>,
    val open: List<Double>
)

data class ChartMeta(
    val currency: String,
    val symbol: String,
    val exchangeName: String,
    val instrumentType: String,
    val firstTradeDate: Date,
    val gmtoffset: Int,
    val timezone: String,
    val exchangeTimezoneName: String,
    val chartPreviousClose: Double,
    val previousClose: Double,
    val scale: Double,
    val currentTradingPeriod: CurrentTradingPeriod,
    val tradingPeriods: TradingPeriods,
    val dataGranularity: String,
    val validRanges: List<String>
)

data class TradingPeriod(
    val timezone: String,
    val start: Long,
    val end: Long,
    val gmtoffset: Int
)

data class CurrentTradingPeriod(
    val pre: TradingPeriod,
    val post: TradingPeriod,
    val regular: TradingPeriod
)

data class TradingPeriods(
    val pre: List<List<TradingPeriod>>,
    val post: List<List<TradingPeriod>>,
    val regular: List<List<TradingPeriod>>
)

enum class ChartEvents(val value: String) {
    DIV("div"),
    SPLIT("split"),
    EARN("earn");

    companion object {
        val all = values().joinToString("|") { it.value } // e.g. "div|split|earn"
    }
}

fun main(args: Array<String>) {
    getChart("MU")?.let {
        println(it.meta.toPrettyJson())
    }
}

private val baseUrl = "https://query1.finance.yahoo.com/v8/finance/chart/"

fun getChart(symbol: String, interval: String = "1m", includePrePost: Boolean = true): Chart? {
    val params = mapOf(
        "period1" to "1526478954",
        "period2" to "1526997354",
        "interval" to interval,
        "includePrePost" to includePrePost.toString(),
        "events" to ChartEvents.all
//        "lang" to "en-US",
//        "region" to "US",
//        "crumb" to "4zz3rv3vXd1",
//        "corsDomain" to "finance.yahoo.com"
    )
    return yahooGet(baseUrl + symbol, params)?.let {
        val chartResponse = objectMapper.readValue<ChartResponse>(it).chart
        if (chartResponse.error == null) {
            chartResponse.result.first()
        } else null
    }
}