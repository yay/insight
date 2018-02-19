package com.vitalyk.insight.main

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.*

// The IEX API is currently open and does not require authentication to access its data.
// https://iextrading.com/developer/docs/

object IexApi1 {
    private val client = HttpClients.main
    private const val baseUrl = "https://api.iextrading.com/1.0"
    private const val badUrlMsg = "Bad URL."

    // Mapper instances are fully thread-safe provided that ALL configuration of the
    // instance occurs before ANY read or write calls.
    private val mapper = jacksonObjectMapper().apply {
        enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
    }
    // http://www.baeldung.com/jackson-collection-array
    private val listTypes = listOf(
        ChartUnit::class.java,
        DayChartUnit::class.java,
        FastQuote::class.java,
        NewsStory::class.java,
        Dividend::class.java
    ).map { it to it.toListType() }.toMap()

    private fun Class<*>.toListType(): CollectionType =
        mapper.typeFactory.constructCollectionType(List::class.java, this)

    // Refers to the common issue type of the stock.
    enum class IssueType {
        @JsonProperty("ad")
        ADR,  // American Depository Receipt
        @JsonProperty("re")
        REIT, // Real Estate Investment Trust
        @JsonProperty("ce")
        CEF,  // Closed End Fund
        @JsonProperty("si")
        SI,   // Secondary Issue
        @JsonProperty("lp")
        LP,   // Limited Partnerships
        @JsonProperty("cs")
        CS,   // Common Stock
        @JsonProperty("et")
        ETF,  // Exchange Traded Fund
        @JsonEnumDefaultValue
        NA    // N/A: warrant, note, or (non-filling) CEF
    }

    data class Company(
        val symbol: String,
        val companyName: String,
        val exchange: String,
        val industry: String,
        val website: String,
        val description: String,
        val CEO: String,
        val issueType: IssueType,
        val sector: String
    )

    data class Dividend(
        val exDate: Date,
        val paymentDate: Date,
        val recordDate: Date,
        val declaredDate: Date,
        val amount: Double,
        val flag: DividendFlag,
        val type: String,
        val qualified: String,
        val indicated: String
    )

    enum class DividendFlag {
        @JsonProperty("FI") // Final dividend, dividend ends or instrument ends
        FINAL,
        @JsonProperty("LI") // Liquidation, instrument liquidates
        LIQUIDATION,
        @JsonProperty("PR") // Proceeds of a sale of rights or shares
        PROCEEDS,
        @JsonProperty("RE") // Redemption of rights
        REDEMPTION,
        @JsonProperty("AC") // Accrued dividend
        ACCRUED,
        @JsonProperty("AR") // Payment in arrears (owed money that should have been paid earlier)
        ARREARS,
        @JsonProperty("AD") // Additional payment
        ADDITIONAL,
        @JsonProperty("EX") // Extra payment
        EXTRA,
        @JsonProperty("SP") // Special dividend
        SPECIAL,
        @JsonProperty("YE") // Year end
        YEAR,
        @JsonProperty("UR") // Unknown rate
        UNKNOWN,
        @JsonProperty("SU") // Regular dividend is suspended
        SUSPENDED,
        @JsonEnumDefaultValue
        NA
    }

    // http://www.baeldung.com/jackson-serialize-dates
    data class LastValue<out T>(
        val value: T,
        val lastUpdated: Date
    )

    data class DayStats(
        val volume: LastValue<Long>,
        val symbolsTraded: LastValue<Long>,
        val routedVolume: LastValue<Long>,
        val notional: LastValue<Long>,
        val marketShare: LastValue<Double>
    )

    data class FastQuote(
        val symbol: String,
        val price: Double,
        val size: Int,
        val time: Long
    )

    // https://www.investopedia.com/terms/v/vwap.asp
    data class ChartUnit(
        val date: Date,
        val open: Double,
        val high: Double,
        val low: Double,
        val close: Double,
        val volume: Long,
        val unadjustedVolume: Long,
        val change: Double,
        val changePercent: Double,
        val vwap: Double, // volume weighted average price
        val label: String,
        val changeOverTime: Double // % change of each interval relative to first value,
                                   // useful for comparing multiple stocks
    )

    // TODO: check types
    data class DayChartUnit(
        val date: String,
        val minute: String,
        val label: String,
        val high: Double,
        val low: Double,
        val average: Double,
        val volume: Long,
        val notional: Double,
        val numberOfTrades: Int,
        val marketHigh: Double,
        val marketLow: Double,
        val marketAverage: Double,
        val marketVolume: Long,
        val marketNotional: Double,
        val marketNumberOfTrades: Int,
        val marketChangeOverTime: Double,
        val changeOverTime: Double
    )

    data class StockBatch(
        val quote: Quote?,
        val news: List<NewsStory>?,
        val chart: List<ChartUnit>?
    )

    data class Quote(
        val symbol: String,
        val companyName: String,
        val primaryExchange: String,
        val sector: String,
        val calculationPrice: String,
        val open: Double,
        val openTime: Date,
        val close: Double,
        val closeTime: Date,
        val high: Double,
        val low: Double,
        val latestPrice: Double,
        val latestSource: String,
        val latestTime: String,
        val latestUpdate: Date,
        val latestVolume: Long,
        val iexRealtimePrice: Double?,
        val iexRealtimeSize: Long?,
        val iexLastUpdated: Date?,
        val delayedPrice: Double,
        val delayedPriceTime: Double,
        val previousClose: Double,
        val change: Double,
        val changePercent: Double,
        val iexMarketPercent: Double?,
        val iexVolume: Long?,
        val avgTotalVolume: Long,
        val iexBidPrice: Double?,
        val iexBidSize: Long?,
        val iexAskPrice: Double?,
        val iexAskSize: Long?,
        val marketCap: Long,
        val peRatio: Double,
        val week52High: Double,
        val week52Low: Double,
        val ytdChange: Double
    )

    data class NewsStory(
        val datetime: Date,
        val headline: String,
        val source: String,
        val url: String,
        val summary: String,
        val related: String
    )

    enum class Type(val value: String) {
        Quote("quote"),
        News("news"),
        Chart("chart")
    }

    enum class Range(val value: String) {
        Y5("5y"),
        Y2("2y"),
        Y("1y"),
        YTD("ytd"),
        M6("6m"),
        M3("3m"),
        M("1m"),
        D("1d"),
//        Date("date"),
//        Auto("dynamic")
    }

    val allTypes = Type.values().toSet()

    private fun getStringResponse(requestUrl: String): String? {
        val request = Request.Builder()
            .url(requestUrl)
            .build()

        val response = client.newCall(request).execute()

        return getResponseString(response)
    }

    private fun getResponseString(response: Response): String? {
        response.use {
            return if (it.isSuccessful) {
                try {
                    it.body()?.string()
                } catch (e: IOException) { // string() can throw
                    e.printStackTrace()
                    getAppLogger().error("Request failed: ${e.message}")
                    null
                }
            } else {
                getAppLogger().error("Request failed: $it.")
                null
            }
        }
    }

    fun getCompany(symbol: String): Company {
        val url = "$baseUrl/stock/$symbol/company"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), Company::class.java)
    }

    // https://iextrading.com/developer/docs/#chart
    // For example: IexApi1.getChart("AAPL").joinToString("\n")
    fun getChart(symbol: String, range: Range = Range.Y): List<ChartUnit> {
        val url = "$baseUrl/stock/$symbol/chart/${range.value}"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[ChartUnit::class.java])
    }

    // For example: getDayChart("AAPL", "20180131")
    fun getDayChart(symbol: String, date: String): List<DayChartUnit> {
        val url = "$baseUrl/stock/$symbol/chart/date/$date"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[DayChartUnit::class.java])
    }

    fun getDividends(symbol: String, range: Range = Range.Y): List<Dividend> {
        val url = "$baseUrl/stock/$symbol/dividends/${range.value}"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[Dividend::class.java])
    }

    // https://iextrading.com/developer/docs/#batch-requests
    fun getQuote(symbol: String, range: Range = Range.M, types: Set<Type> = allTypes): StockBatch {
        val httpUrl = HttpUrl.parse("$baseUrl/stock/$symbol/batch") ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().apply {
            addQueryParameter("types", types.joinToString(",") { it.value })
            addQueryParameter("range", range.value)
            addQueryParameter("last", "10")
        }.build().toString()

        return mapper.readValue(getStringResponse(requestUrl), StockBatch::class.java)
    }

    fun getQuotes(symbols: List<String>, range: Range = Range.M, types: Set<Type> = allTypes): String? {
        if (symbols.size > 100) {
            throw IllegalArgumentException("Up to 100 symbols allowed.")
        }
        val httpUrl = HttpUrl.parse("$baseUrl/stock/market/batch") ?: throw Error(badUrlMsg)

        val requestUrl = httpUrl.newBuilder().apply {
            addQueryParameter("symbols", symbols.joinToString(","))
            addQueryParameter("types", types.joinToString(",") { it.value })
            if (Type.Chart in types) {
                // used to specify a chart range if 'chart' is used in 'types' parameter
                addQueryParameter("range", range.value)
            }
            addQueryParameter("last", "5")
        }.build().toString()

        return getStringResponse(requestUrl)
    }

    // Last provides trade data for executions on IEX.
    // It is a near real time, intraday API that provides IEX last sale price, size and time.
    // If no symbols specified, will return all symbols (8K+).
    fun getLast(symbols: List<String>? = null): List<FastQuote> {
        val httpUrl = HttpUrl.parse("$baseUrl/tops/last") ?: throw Error(badUrlMsg)

        val requestUrl = httpUrl.newBuilder().apply {
            if (symbols != null && symbols.isNotEmpty()) {
                addQueryParameter("symbols", symbols.joinToString(","))
            }
        }.build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[FastQuote::class.java])
    }

    fun getDeep(symbol: String) {
        // https://iextrading.com/developer/docs/#deep
    }

    fun getBook(symbol: String) {
        // https://iextrading.com/developer/docs/#book51
    }

    fun getTrades(symbol: String) {
        // https://iextrading.com/developer/docs/#trades
    }

    // https://iextrading.com/developer/docs/#intraday
    fun getDayStats(): DayStats {
        val httpUrl = HttpUrl.parse("$baseUrl/stats/intraday") ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()
        return mapper.readValue(getStringResponse(requestUrl), DayStats::class.java)
    }

}