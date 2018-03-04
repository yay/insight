package com.vitalyk.insight.iex

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.getAppLogger
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


// The IEX API is currently open and does not require authentication to access its data.
// We throttle endpoints by IP, but you should be able to achieve over 100 requests per second.
// https://iextrading.com/developer/docs/

object IexApi {
    private val client = HttpClients.main
    private const val baseUrl = "https://api.iextrading.com/1.0"
    private const val badUrlMsg = "Bad URL."

    private class LocalDateDeserializer : StdDeserializer<LocalDate>(LocalDate::class.java) {

        override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDate {
            return LocalDate.parse(parser.readValueAs(String::class.java), DateTimeFormatter.BASIC_ISO_DATE)
        }
    }

    fun isWeekend(): Boolean {
        val day = LocalDate.now(ZoneId.of("America/New_York")).dayOfWeek
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
    }

    // Mapper instances are fully thread-safe provided that ALL configuration of the
    // instance occurs before ANY read or write calls.
    private val mapper = jacksonObjectMapper().apply {
        enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
    }

    private fun String.toJsonNode(): JsonNode? {
        return try {
            mapper.readTree(this)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
            getAppLogger().error("JSON parsing failed: ${e.message}")
            null
        }
    }

    // http://www.baeldung.com/jackson-collection-array
    private val listTypes = listOf(
        String::class.java,
        Symbol::class.java,
        Quote::class.java,
        Tops::class.java,
        LastTrade::class.java,
        DayChartPoint::class.java,
        MinuteChartPoint::class.java,
        NewsStory::class.java,
        Dividend::class.java,
        Spread::class.java,
        Split::class.java,
        VenueVolume::class.java
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

    data class Tops(
        val symbol: String,
        val marketPercent: Double,
        val bidSize: Int,
        val bidPrice: Double,
        val askSize: Int,
        val askPrice: Double,
        val volume: Long,
        val lastSalePrice: Double,
        val lastSaleSize: Int,
        val lastSaleTime: Date,
        val lastUpdated: Date,
        val sector: String,
        val securityType: String
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
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.S")
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

    data class Trade(
        val price: Double,
        val size: Int,
        val tradeId: Int,
        val isISO: Boolean,
        val isOddLot: Boolean,
        val isOutsideRegularHours: Boolean,
        val isSinglePriceCross: Boolean,
        val isTradeThroughExempt: Boolean,
        val timestamp: Date
    )

    data class Depth(
        val symbol: String,
        val marketPercent: Double,
        val volume: Long,
        val lastSalePrice: Double,
        val lastSaleSize: Int,
        val lastSaleTime: Date,
        val lastUpdated: Date,
        val bids: List<BidAsk>,
        val asks: List<BidAsk>,
        val systemEvent: SystemEventData,
        val tradingStatus: TradingStatusData,
        val opHaltStatus: OpHaltStatus,
        val ssrStatus: SsrStatus,
        val securityEvent: SecurityEventData,
        val trades: List<Trade>,
        val tradeBreaks: List<Trade>,
        @JsonIgnore
        val auction: Auction? = null,     // only for IEX listed securities
        @JsonIgnore
        val officialPrice: OfficialPrice? = null
    )

    data class BidAsk(
        val price: Double,
        val size: Int,
        val timestamp: Date
    )

    data class Book(
        val bids: List<BidAsk>,
        val asks: List<BidAsk>
    )

    data class SsrStatus(
        val isSSR: Boolean,
        val detail: String,
        val timestamp: Date
    )

    data class SystemEventData(
        val systemEvent: SystemEvent,
        val timestamp: Date
    )

    data class OfficialPrice(
        val priceType: OfficialPriceType,
        val price: Double,
        val timestamp: Date
    )

    enum class OfficialPriceType {
        @JsonProperty("Open")
        OPEN,
        @JsonProperty("Close")
        CLOSE
    }

    enum class SystemEvent {
        @JsonProperty("O")
        MESSAGES_START,
        @JsonProperty("S")
        SYSTEM_HOURS_START,
        @JsonProperty("R")
        MARKET_HOURS_START,
        @JsonProperty("M")
        MARKET_HOURS_END,
        @JsonProperty("E")
        SYSTEM_HOURS_END,
        @JsonProperty("C")
        MESSAGES_END
    }

    // https://iextrading.com/developer/docs/#trading-status
    data class TradingStatusData(
        val status: TradingStatus,
        val reason: TradingHaltReason,
        val timestamp: Date
    )

    enum class TradingStatus {
        @JsonProperty("H")
        HALTED,
        @JsonProperty("O")
        ORDER,
        @JsonProperty("P")
        PAUSED,
        @JsonProperty("T")
        TRADING
    }

    enum class TradingHaltReason {
        // Trading Halt Reasons
        @JsonProperty("T1")
        HALT_NEWS_PENDING,
        @JsonProperty("IPO1")
        IPO_NOT_YET_TRADING,
        @JsonProperty("IPOD")
        IPO_DEFERRED,
        @JsonProperty("MCB3")
        CIRCUIT_BREAKER_L3,
        @JsonEnumDefaultValue
        @JsonProperty("NA")
        NA,
        // Order Acceptance Period Reasons
        @JsonProperty("T2")
        HALT_NEWS_DISSEMINATION,
        @JsonProperty("IPO2")
        IPO_ORDER_ACCEPTANCE_PERIOD,
        @JsonProperty("IPO3")
        IPO_PRE_LAUNCH_PERIOD,
        @JsonProperty("MCB1")
        CIRCUIT_BREAKER_L1,
        @JsonProperty("MCB2")
        CIRCUIT_BREAKER_L2
    }

    data class OpHaltStatus(
        val isHalted: Boolean,
        val timestamp: Date
    )

    data class SecurityEventData(
        val securityEvent: SecurityEvent,
        val timestamp: Date
    )

    enum class SecurityEvent {
        @JsonProperty("MarketOpen")
        MARKET_OPEN,
        @JsonProperty("MarketClose")
        MARKET_CLOSE
    }

    // https://iextrading.com/developer/docs/#auction
    data class Auction(
        val auctionType: AuctionType,
        val pairedShares: Int,
        val imbalanceShares: Int,
        val referencePrice: Double,
        val indicativePrice: Double,
        val auctionBookPrice: Double,
        val collarReferencePrice: Double,
        val lowerCollarPrice: Double,
        val upperCollarPrice: Double,
        val extensionNumber: Int,
        val startTime: String,
        val lastUpdate: Date
    )

    enum class AuctionType {
        @JsonProperty("Open")
        OPEN,
        @JsonProperty("Close")
        CLOSE,
        @JsonProperty("Halt")
        HALT,
        @JsonProperty("Volatility")
        VOLATILITY,
        @JsonProperty("IPO")
        IPO
    }

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

    data class LastPrice(
        val price: Double,
        val time: Date
    )

    data class OHLC(
        val open: LastPrice,
        val close: LastPrice,
        val high: Double,
        val low: Double
    )

    // http://www.baeldung.com/jackson-serialize-dates
    data class IntradayStat<out T>(
        val value: T,
        val lastUpdated: Date
    )

    data class IntradayStats(
        val volume: IntradayStat<Long>,
        val symbolsTraded: IntradayStat<Long>,
        val routedVolume: IntradayStat<Long>,
        val notional: IntradayStat<Long>,
        val marketShare: IntradayStat<Double>
    )

    data class RecordsStat(
        val recordValue: Double,
        val recordDate: Date,
        val previousDayValue: Double,
        val avg30Value: Double
    )

    data class RecordsStats(
        val volume: RecordsStat,
        val symbolsTraded: RecordsStat,
        val routedVolume: RecordsStat,
        val notional: RecordsStat
    )

    data class LastTrade(
        val symbol: String,
        val price: Double,
        val size: Int,
        val time: Date
    )

    // https://www.investopedia.com/terms/v/vwap.asp
    data class DayChartPoint(
        val date: Date,
        val open: Double,
        val high: Double,
        val low: Double,
        val close: Double,
        val volume: Long,
        val unadjustedVolume: Long,
        val change: Double,         // compared to previous close
        val changePercent: Double,  // compared to previous close
        val vwap: Double,           // volume weighted average price
        val label: String,
        val changeOverTime: Double  // % change of each interval relative to first value,
                                    // useful for comparing multiple stocks
    )

    data class MinuteChartPoint(
        @JsonDeserialize(using = LocalDateDeserializer::class)
        val date: LocalDate,
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

    data class Batch(
        val quote: Quote?,
        val news: List<NewsStory>?,
        val chart: List<DayChartPoint>?
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

    // The names should match the individual endpoint names. Limited to 10 types.
    enum class BatchType(val value: String) {
        QUOTE("quote"),
        NEWS("news"),
        CHART("chart")
    }
    private val batchTypes = BatchType.values().toSet()

    enum class Range(val value: RangeValue) {
        Y5(RangeValue("5y", "5 Years")),
        Y2(RangeValue("2y", "2 Years")),
        Y(RangeValue("1y", "1 Year")),
        YTD(RangeValue("ytd", "YTD")),
        M6(RangeValue("6m", "6 Months")),
        M3(RangeValue("3m", "3 Months")),
        M(RangeValue("1m", "1 Month"))
    }

    data class RangeValue(
        val code: String,
        val name: String
    )

    data class LogoData(
        val url: String
    )

    data class VenueVolume(
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
        val url = "${baseUrl}/stock/$symbol/company"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), Company::class.java)
    }

    fun getStats(symbol: String): Stats {
        val url = "${baseUrl}/stock/$symbol/stats"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), Stats::class.java)
    }

    // Generic method for fetching gainers, losers, etc.
    private fun getQuotes(path: String): List<Quote> {
        val url = "${baseUrl}$path"
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
    // For example: IexApi.getDayChart("AAPL").joinToString("\n")
    fun getDayChart(symbol: String, range: Range = Range.Y): List<DayChartPoint> {
        val url = "${baseUrl}/stock/$symbol/chart/${range.value.code}"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[DayChartPoint::class.java])
    }

    // For example: getMinuteChart("AAPL", "20180129")
    fun getMinuteChart(symbol: String, date: String): List<MinuteChartPoint> {
        val url = "${baseUrl}/stock/$symbol/chart/date/$date"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[MinuteChartPoint::class.java])
    }

    fun getDividends(symbol: String, range: Range = Range.Y): List<Dividend> {
        val url = "${baseUrl}/stock/$symbol/dividends/${range.value.code}"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[Dividend::class.java])
    }

    fun getEarnings(symbol: String): RecentEarnings {
        val url = "${baseUrl}/stock/$symbol/earnings"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), RecentEarnings::class.java)
    }

    fun getPeers(symbol: String): List<String> {
        val url = "${baseUrl}/stock/$symbol/peers"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[String::class.java])
    }

    fun getVolumeByVenue(symbol: String): List<VenueVolume> {
        val url = "${baseUrl}/stock/$symbol/volume-by-venue"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[VenueVolume::class.java])
    }

    // This is a helper function, but the google APIs url is standardized.
    fun getLogoData(symbol: String): LogoData {
        val url = "${baseUrl}/stock/$symbol/logo"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), LogoData::class.java)
    }

    fun getFinancials(symbol: String): RecentFinancials {
        val url = "${baseUrl}/stock/$symbol/financials"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), RecentFinancials::class.java)
    }

    fun getSpread(symbol: String): List<Spread> {
        val url = "${baseUrl}/stock/$symbol/effective-spread"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[Spread::class.java])
    }

    fun getOHLC(symbol: String): OHLC {
        val url = "${baseUrl}/stock/$symbol/ohlc"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), OHLC::class.java)
    }

    fun getSplits(symbol: String, range: Range = Range.Y5): List<Split> {
        val url = "${baseUrl}/stock/$symbol/splits/${range.value.code}"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[Split::class.java])
    }

    // List of all supported symbols.
    fun getSymbols(): List<Symbol> {
        val url = "${baseUrl}/ref-data/symbols"
        val httpUrl = HttpUrl.parse(url) ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[Symbol::class.java])
    }

    // 'range' refers to chart range, optional if chart is not in 'types'.
    // https://iextrading.com/developer/docs/#batch-requests
    fun getBatch(symbol: String, types: Set<BatchType> = batchTypes, range: Range = Range.M): Batch {
        val httpUrl = HttpUrl.parse("${baseUrl}/stock/$symbol/batch") ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().apply {
            addQueryParameter("types", types.joinToString(",") { it.value })
            addQueryParameter("range", range.value.code)
            // Parameters that are sent to individual endpoints can be specified in batch calls
            // and will be applied to each supporting endpoint.
            // For example, param below applies to news (only last 10 will be fetched).
            addQueryParameter("last", "10")
        }.build().toString()

        return mapper.readValue(getStringResponse(requestUrl), Batch::class.java)
    }

    // 'range' refers to chart range, optional if chart is not in 'types'.
    fun getBatch(symbols: List<String>, types: Set<BatchType> = batchTypes, range: Range = Range.M): List<Batch> {
        if (symbols.size > 100) {
            throw IllegalArgumentException("Up to 100 symbols allowed.")
        }
        val httpUrl = HttpUrl.parse("${baseUrl}/stock/market/batch") ?: throw Error(badUrlMsg)

        val requestUrl = httpUrl.newBuilder().apply {
            addQueryParameter("symbols", symbols.joinToString(","))
            addQueryParameter("types", types.joinToString(",") { it.value })
            if (BatchType.CHART in types) {
                // used to specify a chart range if 'chart' is used in 'types' parameter
                addQueryParameter("range", range.value.code)
            }
            addQueryParameter("last", "5")
        }.build().toString()

        return getStringResponse(requestUrl)?.toJsonNode()?.map {
            mapper.convertValue(it, Batch::class.java)
        } ?: emptyList()
    }

    // Near real time, intraday API that provides IEX last sale price, size and time.
    // If no symbols are specified, will return all symbols (8K+).
    fun getLastTrade(symbols: List<String>? = null): List<LastTrade> {
        val httpUrl = HttpUrl.parse("${baseUrl}/tops/last") ?: throw Error(badUrlMsg)

        val requestUrl = httpUrl.newBuilder().apply {
            if (symbols != null && symbols.isNotEmpty()) {
                addQueryParameter("symbols", symbols.joinToString(","))
            }
        }.build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[LastTrade::class.java])
    }

    fun getTops(symbols: List<String>? = null): List<Tops> {
        if (isWeekend()) return emptyList()

        val httpUrl = HttpUrl.parse("${baseUrl}/tops") ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().apply {
            if (symbols != null && symbols.isNotEmpty()) {
                addQueryParameter("symbols", symbols.joinToString(","))
            }
        }.build().toString()

        return mapper.readValue(getStringResponse(requestUrl), listTypes[Tops::class.java])
    }

    fun parseTops(json: String): Tops = mapper.readValue(json, Tops::class.java)
    fun parseQuote(json: String): Quote = mapper.readValue(json, Quote::class.java)

    // https://iextrading.com/developer/docs/#deep
    fun getDepth(symbol: String): Depth {
        val httpUrl = HttpUrl.parse("${baseUrl}/deep") ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().apply {
            addQueryParameter("symbols", symbol)
        }.build().toString()

        return mapper.readValue(getStringResponse(requestUrl), Depth::class.java)
    }

    // Shows IEX’s bids and asks for given symbols.
    // https://iextrading.com/developer/docs/#book51
    fun getBook(symbol: String): Book? {
        val httpUrl = HttpUrl.parse("${baseUrl}/deep/book") ?: throw Error(badUrlMsg)

        val requestUrl = httpUrl.newBuilder().apply {
            addQueryParameter("symbols", symbol)
        }.build().toString()

        return mapper.convertValue(
            getStringResponse(requestUrl)?.toJsonNode()?.get(symbol.toUpperCase()),
            Book::class.java
        )
    }

    // https://iextrading.com/developer/docs/#trades
    fun getTrades(symbol: String, last: Int = 20): List<Trade> {
        if (isWeekend()) return emptyList()

        val httpUrl = HttpUrl.parse("${baseUrl}/deep/trades") ?: throw Error(badUrlMsg)

        val requestUrl = httpUrl.newBuilder().apply {
            addQueryParameter("symbols", symbol)
            addQueryParameter("last", last.toString())
        }.build().toString()

        return getStringResponse(requestUrl)?.toJsonNode()?.get(symbol.toUpperCase())?.map {
            mapper.convertValue(it, Trade::class.java)
        } ?: emptyList()
    }

    // https://iextrading.com/developer/docs/#intraday
    fun getIntradayStats(): IntradayStats {
        val httpUrl = HttpUrl.parse("${baseUrl}/stats/intraday") ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()
        return mapper.readValue(getStringResponse(requestUrl), IntradayStats::class.java)
    }

    // https://iextrading.com/developer/docs/#records
    fun getRecordsStats(): RecordsStats {
        val httpUrl = HttpUrl.parse("${baseUrl}/stats/records") ?: throw Error(badUrlMsg)
        val requestUrl = httpUrl.newBuilder().build().toString()
        return mapper.readValue(getStringResponse(requestUrl), RecordsStats::class.java)
    }

}