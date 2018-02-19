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
        String::class.java,
        Symbol::class.java,
        Quote::class.java,
        FastQuote::class.java,
        ChartUnit::class.java,
        DayChartUnit::class.java,
        NewsStory::class.java,
        Dividend::class.java,
        Spread::class.java,
        Split::class.java,
        VolumeData::class.java
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
        val qualified: DividendIncomeType,
        val indicated: Float?
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

    enum class DividendIncomeType {
        @JsonProperty("Q")
        QUALIFIED,
        @JsonProperty("N")
        UNQUALIFIED,
        @JsonProperty("P")
        PARTIALLY,
        @JsonEnumDefaultValue
        NA
    }

    data class RecentEarnings(
        val symbol: String,
        val earnings: List<Earnings>
    )

    data class Earnings(
        val actualEPS: Double,
        val consensusEPS: Double,
        val estimatedEPS: Double,
        val announceTime: String,
        val numberOfEstimates: Short,
        val EPSSurpriseDollar: Double,
        val EPSReportDate: Date,
        val fiscalPeriod: String,
        val fiscalEndDate: Date
    )

    data class RecentFinancials(
        val symbol: String,
        val financials: List<Financials>
    )

    data class Financials(
        val reportDate: Date,
        val grossProfit: Long,
        val costOfRevenue: Long,
        val operatingRevenue: Long,
        val totalRevenue: Long,
        val operatingIncome: Long,
        val netIncome: Long,
        val researchAndDevelopment: Long,
        val operatingExpense: Long,
        val currentAssets: Long,
        val totalAssets: Long,
        val totalLiabilities: Long,
        val currentCash: Long,
        val currentDebt: Long?,
        val totalCash: Long,
        val totalDebt: Long?,
        val shareholderEquity: Long,
        val cashChange: Long,
        val cashFlow: Long,
        val operatingGainsLosses: Long?
    )

    data class Stats(
        val companyName: String,
        val marketcap: Long,
        val beta: Double,
        val week52high: Double,
        val week52low: Double,
        val week52change: Double,
        val shortInterest: Long,
        val shortDate: Date,
        val dividendRate: Double,
        val dividendYield: Double,
        val exDividendDate: Date,
        val latestEPS: Double,          // Most recent quarter (MRQ)
        val latestEPSDate: Date,
        val sharesOutstanding: Long,
        val float: Long,
        val returnOnEquity: Double,     // Trailing twelve months (TTM)
        val consensusEPS: Double,       // MRQ
        val numberOfEstimates: Short,   // MRQ
        val EPSSurpriseDollar: Double?, // actual EPS vs consensus EPS, in dollars
        val EPSSurprisePercent: Double, // actual EPS vs consensus EPS, percent difference
        val symbol: String,
        val EBITDA: Long,               // TTM
        val revenue: Long,              // TTM
        val grossProfit: Long,          // TTM
        val cash: Long,                 // Total cash, TTM
        val debt: Long,                 // Total debt, TTM
        val ttmEPS: Double,             // TTM
        val revenuePerShare: Double,    // TTM
        val revenuePerEmployee: Double, // TTM
        val peRatioHigh: Double,
        val peRatioLow: Double,
        val returnOnAssets: Double,     // TTM
        val returnOnCapital: Double?,   // TTM
        val profitMargin: Double?,
        val priceToSales: Double?,
        val priceToBook: Double,
        val day200MovingAvg: Double,
        val day50MovingAvg: Double,
        val institutionPercent: Double, // Represents top 15 institutions
        val insiderPercent: Double?,
        val shortRatio: Double,
        val year5ChangePercent: Double,
        val year2ChangePercent: Double,
        val year1ChangePercent: Double,
        val ytdChangePercent: Double,
        val month6ChangePercent: Double,
        val month3ChangePercent: Double,
        val month1ChangePercent: Double,
        val day5ChangePercent: Double,
        val day30ChangePercent: Double
    )

    data class Spread(
        // Eligible shares used for calculating effectiveSpread and priceImprovement
        val volume: Long,
        // Market Identifier Code (MIC)
        val venue: String,
        // Readable MIC
        val venueName: String,
        // Measure marketable orders executed in relation to the market center’s quoted spread
        // and takes into account hidden and midpoint liquidity available at each market center in dollars.
        val effectiveSpread: Double,
        // A ratio calculated by dividing a market center’s effective spread by the NBBO quoted spread.
        val effectiveQuoted: Double,
        // The average amount of price improvement in dollars per eligible share executed.
        val priceImprovement: Double
    )

    // https://www.investopedia.com/terms/f/fractionalshare.asp
    data class Split(
        val exDate: Date,
        val declaredDate: Date,
        val recordDate: Date,
        val paymentDate: Date,
        // The split ratio is an inverse of the number of shares that a holder of the stock
        // would have after the split divided by the number of shares that the holder had before.
        // For example: Split ratio of .5 = 2 for 1 split.
        val ratio: Double,
        val toFactor: Double,
        val forFactor: Double
    )

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

    enum class CalculationPrice {
        @JsonProperty("tops")
        TOPS,
        @JsonProperty("sip")
        SIP,
        @JsonProperty("previousclose")
        PREV_CLOSE,
        @JsonProperty("close")
        CLOSE
    }

    data class Quote(
        val symbol: String,
        val companyName: String,
        val primaryExchange: String,
        val sector: String,
        val calculationPrice: CalculationPrice,
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
        val delayedPriceTime: Date,
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
    val allTypes = Type.values().toSet()

    enum class Range(val value: String) {
        Y5("5y"),
        Y2("2y"),
        Y("1y"),
        YTD("ytd"),
        M6("6m"),
        M3("3m"),
        M("1m")
    }

    data class LogoData(
        val url: String
    )

    data class VolumeData(
        val volume: Long,
        val venue: String,
        val venueName: String,
        val marketPercent: Double,
        val avgMarketPercent: Double,
        val date: Date?
    )

    data class Symbol(
        val symbol: String, // Symbol represented in Nasdaq Integrated symbology (INET).
        val name: String,   // The date the symbol reference data was generated.
        val date: Date,
        val isEnabled: Boolean, // Will be true if the symbol is enabled for trading on IEX.
        val type: IssueType,
        val iexId: String // Unique ID applied by IEX to track securities through symbol changes.
    )

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

    fun getStats(symbol: String): Stats {
        val url = "$baseUrl/stock/$symbol/stats"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), Stats::class.java)
    }

    private fun getQuotes(path: String): List<Quote> {
        val url = "$baseUrl$path"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[Quote::class.java])
    }

    fun getMostActive() = getQuotes("/stock/market/list/mostactive")
    fun getGainers() = getQuotes("/stock/market/list/gainers")
    fun getLosers() = getQuotes("/stock/market/list/losers")
    fun getIexVolume() = getQuotes("/stock/market/list/iexvolume")
    fun getIexPercent() = getQuotes("/stock/market/list/iexpercent")

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

    fun getEarnings(symbol: String): RecentEarnings {
        val url = "$baseUrl/stock/$symbol/earnings"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), RecentEarnings::class.java)
    }

    fun getPeers(symbol: String): List<String> {
        val url = "$baseUrl/stock/$symbol/peers"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[String::class.java])
    }

    fun getVolumeByVenue(symbol: String): List<VolumeData> {
        val url = "$baseUrl/stock/$symbol/volume-by-venue"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[VolumeData::class.java])
    }

    // This is a helper function, but the google APIs url is standardized.
    fun getLogoData(symbol: String): LogoData {
        val url = "$baseUrl/stock/$symbol/logo"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), LogoData::class.java)
    }

//    fun getLogo(symbol: String): Image {
//        val url = "https://storage.googleapis.com/iex/api/logos/$symbol.png"
//        return Image(url)
//    }

    fun getFinancials(symbol: String): RecentFinancials {
        val url = "$baseUrl/stock/$symbol/financials"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), RecentFinancials::class.java)
    }

    fun getSpread(symbol: String): List<Spread> {
        val url = "$baseUrl/stock/$symbol/effective-spread"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[Spread::class.java])
    }

    fun getSplits(symbol: String, range: Range = Range.Y5): List<Split> {
        val url = "$baseUrl/stock/$symbol/splits/${range.value}"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[Split::class.java])
    }

    fun getSymbols(): List<Symbol> {
        val url = "$baseUrl/ref-data/symbols"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[Symbol::class.java])
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