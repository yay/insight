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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.SendChannel
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


// The IEX API is currently open and does not require authentication to access its data.
// We throttle endpoints by IP, but you should be able to achieve over 100 requests per second.
// https://iextrading.com/developer/docs/

class Iex(private val httpClient: OkHttpClient) {
    companion object {
        private const val baseUrl = "https://api.iextrading.com/1.0"

        // Mapper instances are fully thread-safe provided that ALL configuration of the
        // instance occurs before ANY read or write calls.
        private val mapper = jacksonObjectMapper().apply {
            enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            registerModule(JavaTimeModule())
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

        private val previousDayMapType = mapper.typeFactory.constructMapType(
            Map::class.java,
            String::class.java, PreviousDay::class.java
        )

        fun parseTops(json: String): Tops = mapper.readValue(json, Tops::class.java)
        fun parseQuote(json: String): Quote = mapper.readValue(json, Quote::class.java)
    }

    private val logger by lazy { LoggerFactory.getLogger(this::class.simpleName) }

    private fun fetch(url: String, params: Map<String, String?> = emptyMap()): String? {
        val httpUrl = (HttpUrl.parse(url) ?: throw IllegalArgumentException("Bad URL: $url"))
            .newBuilder().apply {
                for ((key, value) in params) {
                    addQueryParameter(key, value)
                }
            }.build()

        val request = Request.Builder().url(httpUrl).build()
        val response = try {
            httpClient.newCall(request).execute()
        } catch (e: Exception) {
            logger.warn("${e.message}:\n$httpUrl")
            when (e) {
                is SocketTimeoutException -> null
                is UnknownHostException -> null
                is ConnectException -> null
                else -> throw e
            }
        }

        return response?.use {
            if (it.isSuccessful) {
                try {
                    it.body()?.string()
                } catch (e: IOException) { // string() can throw
                    logger.error("Request failed: $httpUrl\n${e.message}")
                    null
                }
            } else {
                logger.warn("Request failed: $httpUrl\n${it.message()}")
                null
            }
        }
    }

    private class LocalIsoDateDeserializer : StdDeserializer<LocalDate>(LocalDate::class.java) {
        override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDate {
            return LocalDate.parse(parser.readValueAs(String::class.java), DateTimeFormatter.BASIC_ISO_DATE)
        }
    }

    private class LocalDateDeserializer : StdDeserializer<LocalDate>(LocalDate::class.java) {
        override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDate? {
            val str = parser.readValueAs(String::class.java)
            return when (str) {
                "null", "0" -> null
                else -> LocalDate.parse(str, formatter)
            }
        }
        companion object {
            private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        }
    }

    /**
     * Deserializer for dates like "2018-05-11 00:00:00.0".
     */
    private class LocalDateDiscardTimeDeserializer : StdDeserializer<LocalDate>(LocalDate::class.java) {
        override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDate? {
            val str = parser.readValueAs(String::class.java)
            return when {
                str == "null" || str == "0" -> null
                str.length == 10 -> LocalDate.parse(str, dateFormatter)
                else -> LocalDate.parse(str, dateTimeFormatter)
            }
        }
        companion object {
            private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
            private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        }
    }

    private fun String.toJsonNode(): JsonNode? {
        return try {
            mapper.readTree(this)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
            logger.error("JSON parsing failed: ${e.message}")
            null
        }
    }

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
        val sector: String,
        val tags: List<String>
    )

    data class Tops(
        val symbol: String,
        val marketPercent: Double = 0.0,
        val bidSize: Int = 0,
        val bidPrice: Double = 0.0,
        val askSize: Int = 0,
        val askPrice: Double = 0.0,
        val volume: Long = 0,
        val lastSalePrice: Double = 0.0,
        val lastSaleSize: Int = 0,
        val lastSaleTime: Date = Date(),
        val lastUpdated: Date = Date(),
        val sector: String = "",
        val securityType: String = "",
        val seq: Int = 0
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

    // Earnings for the past 4 quarters.
    data class RecentEarnings(
        val symbol: String,
        val earnings: List<Earnings>
    )

    data class Earnings(
        @JsonProperty("actualEPS")
        val actualEps: Double,
        @JsonProperty("consensusEPS")
        val consensusEps: Double,
        @JsonProperty("estimatedEPS")
        val estimatedEps: Double?,
        val announceTime: String?,
        val numberOfEstimates: Int,
        @JsonProperty("EPSSurpriseDollar")
        val epsSurpriseDollar: Double,
        @JsonProperty("EPSReportDate")
        val epsReportDate: Date,
        val fiscalPeriod: String?,
        val fiscalEndDate: Date,
        val yearAgo: Double,
        val yearAgoChangePercent: Double,
        val estimatedChangePercent: Double,
        val symbolId: Int
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

    data class AssetStats(
        val companyName: String = "",
        @JsonProperty("marketcap")
        val marketCap: Long = 0L,
        val beta: Double = 0.0,
        val week52high: Double = 0.0,
        val week52low: Double = 0.0,
        val week52change: Double = 0.0,
        val shortInterest: Long = 0L,
        @JsonDeserialize(using = LocalDateDeserializer::class)
        @JsonFormat(pattern = "yyyy-MM-dd")
        val shortDate: LocalDate? = null,
        val dividendRate: Double = 0.0,
        val dividendYield: Double = 0.0,
        @JsonDeserialize(using = LocalDateDiscardTimeDeserializer::class)
        @JsonFormat(pattern = "yyyy-MM-dd")
        val exDividendDate: LocalDate? = null,
        @JsonProperty("latestEPS")
        val latestEps: Double = 0.0, // Most recent quarter (MRQ)
        @JsonProperty("latestEPSDate")
        @JsonDeserialize(using = LocalDateDeserializer::class)
        @JsonFormat(pattern = "yyyy-MM-dd")
        val latestEpsDate: LocalDate? = null,
        val sharesOutstanding: Long = 0L,
        val float: Long = 0L,
        val returnOnEquity: Double = 0.0, // Trailing twelve months (TTM)
        @JsonProperty("consensusEPS")
        val consensusEps: Double = 0.0, // MRQ
        val numberOfEstimates: Int = 0, // MRQ
        @JsonProperty("EPSSurpriseDollar")
        val epsSurpriseDollar: Double = 0.0, // actual EPS vs consensus EPS, in dollars
        @JsonProperty("EPSSurprisePercent")
        val epsSurprisePercent: Double = 0.0, // actual EPS vs consensus EPS, percent difference
        val symbol: String = "",
        @JsonProperty("EBITDA")
        val ebitda: Long = 0L, // TTM
        val revenue: Long = 0L,  // TTM
        val grossProfit: Long = 0L, // TTM
        val cash: Long = 0L, // Total cash, TTM
        val debt: Long = 0L, // Total debt, TTM
        @JsonProperty("ttmEPS")
        val ttmEps: Double = 0.0, // TTM
        val revenuePerShare: Int = 0, // TTM
        val revenuePerEmployee: Int = 0, // TTM
        val peRatioHigh: Double = 0.0,
        val peRatioLow: Double = 0.0,
        val returnOnAssets: Double = 0.0, // TTM
        val returnOnCapital: Double = 0.0, // TTM
        val profitMargin: Double = 0.0,
        val priceToSales: Double = 0.0,
        val priceToBook: Double = 0.0,
        val day200MovingAvg: Double = 0.0,
        val day50MovingAvg: Double = 0.0,
        val institutionPercent: Double = 0.0, // Represents top 15 institutions
        val insiderPercent: Double = 0.0,
        val shortRatio: Double = 0.0,
        val year5ChangePercent: Double = 0.0,
        val year2ChangePercent: Double = 0.0,
        val year1ChangePercent: Double = 0.0,
        val ytdChangePercent: Double = 0.0,
        val month6ChangePercent: Double = 0.0,
        val month3ChangePercent: Double = 0.0,
        val month1ChangePercent: Double = 0.0,
        val day5ChangePercent: Double = 0.0,
        val day30ChangePercent: Double = 0.0
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
        // https://iextrading.com/developer/docs/#trades
        val trades: List<Trade>,
        // https://iextrading.com/developer/docs/#trade-break
        val tradeBreaks: List<Trade>,
        @JsonIgnore
        val auction: Auction? = null,     // only for Iex listed securities
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

    // https://iextrading.com/developer/docs/#previous
    data class PreviousDay(
        val symbol: String,
        val date: Date,
        val open: Double,
        val high: Double,
        val low: Double,
        val close: Double,
        val volume: Long,
        val unadjustedVolume: Long,
        val change: Double,
        val changePercent: Double,
        val vwap: Double
    )

    interface ChartPoint {
        val open: Double
        val high: Double
        val low: Double
        val close: Double
    }

    interface VolumeChartPoint : ChartPoint {
        val volume: Long
    }

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
        @JsonDeserialize(using = LocalIsoDateDeserializer::class)
        val date: LocalDate, // "20180518"
        val minute: String,  // "09:30"
        val label: String,   // "09:30 AM"
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
        val open: Double,
        val close: Double,
        val marketOpen: Double,
        val marketClose: Double,
        val changeOverTime: Double,
        val marketChangeOverTime: Double  // 15-min delayed
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

    // https://iextrading.com/developer/docs/#quote
    data class Quote(
        val symbol: String,
        val companyName: String,
        val primaryExchange: String,
        val sector: String,
        val calculationPrice: CalculationPrice,
        val open: Double = 0.0,
        val openTime: Date?,
        val close: Double = 0.0,
        val closeTime: Date?,
        val high: Double = 0.0,
        val low: Double = 0.0,
        val latestPrice: Double = 0.0,
        val latestSource: String,
        val latestTime: String,
        val latestUpdate: Date?,
        val latestVolume: Long = 0,
        val iexRealtimePrice: Double = 0.0,
        val iexRealtimeSize: Long = 0,
        val iexLastUpdated: Date?,
        val delayedPrice: Double = 0.0,
        val delayedPriceTime: Date?,
        val extendedPrice: Double = 0.0,
        val extendedPriceTime: Date?,
        val previousClose: Double = 0.0,
        val change: Double = 0.0,
        val changePercent: Double = 0.0,
        val extendedChange: Double = 0.0,
        val extendedChangePercent: Double = 0.0,
        val iexMarketPercent: Double = 0.0, // IEX’s percentage of the market in the stock
        val iexVolume: Long = 0,            // Shares traded in the stock on IEX
        val avgTotalVolume: Long = 0,       // 30 day average volume on all markets
        val iexBidPrice: Double = 0.0,
        val iexBidSize: Long = 0,
        val iexAskPrice: Double = 0.0,
        val iexAskSize: Long = 0,
        val marketCap: Long = 0,
        val peRatio: Double = 0.0,
        val week52High: Double = 0.0,
        val week52Low: Double = 0.0,
        val ytdChange: Double = 0.0
    )

    data class NewsStory(
        val datetime: Date,
        val headline: String,
        val source: String,
        val url: String,
        val summary: String,
        val related: String,
        val image: String
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
        M(RangeValue("1m", "1 Month"));

        override fun toString(): String = value.name
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
        val isEnabled: Boolean, // Will be true if the symbol is enabled for trading on Iex.
        val type: IssueType,
        val iexId: String // Unique ID applied by Iex to track securities through symbol changes.
    )

    /**
     * Asynchronously fetches stats for all symbols and sends the total number of requests
     * to the `counter` channel each time a request completes. For example:
     *
     *     val counterActor = actor<Int>(UI) {
     *         var counter = 0
     *         for (total in channel) {
     *             progressLabel.text = "${++counter} / $total"
     *         }
     *     }
     *     val companies = iex.mapSymbolsWithProgress(iex::getCompany, counterActor)
     */
    suspend fun <T : Any> mapSymbolsWithProgress(fn: (String) -> T?, counter: SendChannel<Int>): Map<String, T> {
        val symbolMap = getSymbols()?.let { it.map { it.symbol to it }.toMap() } ?: emptyMap()
        val total = symbolMap.size
        return symbolMap
            .map { (symbol, _) ->
                async {
                    val data = fn(symbol)
                    if (data != null) Pair(symbol, data) else null
                }
            }
            .mapNotNull { it.await().also { counter.send(total) } }
            .map { it.first to it.second }
            .toMap()
    }

    // TODO: implement https://iextrading.com/developer/docs/#market

    fun getCompany(symbol: String): Company? {
        return fetch("$baseUrl/stock/$symbol/company")?.let {
            try {
                mapper.readValue(it, Company::class.java)
            } catch (e: MissingKotlinParameterException) {
                // For some symbols company name, sector, etc. will be null.
                logger.warn(symbol + ": " + e.msg)
                null
            }
        }
    }

    suspend fun getCompaniesAsync(): Map<String, Company> {
        val symbolMap = getSymbols()?.let { it.map { it.symbol to it }.toMap() } ?: emptyMap()
        return symbolMap.map { async { getCompany(it.key) } }
            .mapNotNull { it.await() }
            .map { it.symbol to it }
            .toMap()
    }

    /**
     * Fetches stats for the specified symbol.
     */
    fun getAssetStats(symbol: String): AssetStats? {
        return fetch("$baseUrl/stock/$symbol/stats")?.let {
            // The returned JSON here can use `"NaN"` for values:
            // https://github.com/iexg/IEX-API/issues/29
            mapper.readValue(it.replace("\"NaN\"", "0"), AssetStats::class.java)
        }
    }

    /**
     * Asynchronously fetches stats for all symbols.
     */
    suspend fun getAssetStatsAsync(): Map<String, AssetStats> {
        val symbolMap = getSymbols()?.let { it.map { it.symbol to it }.toMap() } ?: emptyMap()
        return symbolMap.map { async { getAssetStats(it.key) } }
            .mapNotNull { it.await() }
            .map { it.symbol to it }
            .toMap()
    }

    /**
     * Asynchronously fetches stats for all symbols and sends the total number of requests
     * to the `counter` channel each time a request completes. For example:
     *
     *     val counterActor = actor<Int>(UI) {
     *         var counter = 0
     *         for (total in channel) {
     *             progressLabel.text = "${++counter} / $total"
     *         }
     *     }
     */
    suspend fun getAssetStatsWithProgress(counter: SendChannel<Int>): Map<String, AssetStats> {
        val symbolMap = getSymbols()?.let { it.map { it.symbol to it }.toMap() } ?: emptyMap()
        val total = symbolMap.size
        return symbolMap.map { async { getAssetStats(it.key) } }
            .mapNotNull { it.await().also { counter.send(total) } }
            .map { it.symbol to it }
            .toMap()
    }

    fun getQuote(symbol: String): Quote? {
        return fetch("$baseUrl/stock/$symbol/quote")?.let {
            mapper.readValue(it, Quote::class.java)
        }
    }

    // Generic method for fetching gainers, losers, etc.
    private fun getQuotes(path: String): List<Quote>? {
        return fetch("$baseUrl$path")?.let {
            mapper.readValue(it, listTypes[Quote::class.java])
        }
    }

    fun getMostActive() = getQuotes("/stock/market/list/mostactive")
    fun getGainers() = getQuotes("/stock/market/list/gainers")
    fun getLosers() = getQuotes("/stock/market/list/losers")
    fun getIexVolume() = getQuotes("/stock/market/list/iexvolume")
    fun getIexPercent() = getQuotes("/stock/market/list/iexpercent")

    /**
     * Returns previous day price data for the given symbol.
     */
    fun getPreviousDay(symbol: String): PreviousDay? {
        return fetch("$baseUrl/stock/$symbol/previous")?.let {
            mapper.readValue(it, PreviousDay::class.java)
        }
    }

    /**
     * Returns previous day price data for the whole market.
     */
    fun getPreviousDay(): Map<String, PreviousDay>? {
        return fetch("$baseUrl/stock/market/previous")?.let {
            mapper.readValue(it, previousDayMapType)
        }
    }

    // https://iextrading.com/developer/docs/#chart
    // For example: Iex.getDayChart("AAPL").joinToString("\n")
    fun getDayChart(symbol: String, range: Range = Range.Y): List<DayChartPoint>? {
        return fetch("$baseUrl/stock/$symbol/chart/${range.value.code}")?.let {
            mapper.readValue(it, listTypes[DayChartPoint::class.java])
        }
    }

    fun getDayChartWithQuote(symbol: String, range: Range = Range.Y): MutableList<DayChartPoint>? {
        return getDayChart(symbol, range)?.let {
            val result = it.toMutableList()
            getQuote(symbol)?.let {
                result.add(DayChartPoint(
                    date = it.latestUpdate ?: Date(),
                    open = it.open,
                    high = it.high,
                    low = it.low,
                    close = it.latestPrice,
                    volume = it.latestVolume,
                    unadjustedVolume = it.latestVolume,
                    change = it.change,
                    changePercent = it.changePercent,
                    vwap = 0.0,
                    label = "Last",
                    changeOverTime = 0.0
                ))
            }
            result
        }
    }

    // For example: getMinuteChart("AAPL", "20180129")
    fun getMinuteChart(symbol: String, date: String): List<MinuteChartPoint>? {
        return fetch("$baseUrl/stock/$symbol/chart/date/$date")?.let {
            mapper.readValue(it, listTypes[MinuteChartPoint::class.java])
        }
    }

    fun getDividends(symbol: String, range: Range = Range.Y): List<Dividend>? {
        return fetch("$baseUrl/stock/$symbol/dividends/${range.value.code}")?.let {
            mapper.readValue(it, listTypes[Dividend::class.java])
        }
    }

    fun getEarnings(symbol: String): RecentEarnings? {
        return fetch("$baseUrl/stock/$symbol/earnings")?.let {
            try {
                mapper.readValue(it, RecentEarnings::class.java)
            } catch (e: MissingKotlinParameterException) {
                // Some assets have no earnings.
                // In this case an empty JSON object is returned by the server.
                logger.warn("$symbol earnings are not available. ${e.message}")
                null
            }
        }
    }

    fun getPeers(symbol: String): List<String>? {
        return fetch("$baseUrl/stock/$symbol/peers")?.let {
            mapper.readValue(it, listTypes[String::class.java])
        }
    }

    fun getVolumeByVenue(symbol: String): List<VenueVolume>? {
        return fetch("$baseUrl/stock/$symbol/volume-by-venue")?.let {
            mapper.readValue(it, listTypes[VenueVolume::class.java])
        }
    }

    // This is a helper function, but the google APIs url is standardized.
    fun getLogoData(symbol: String): LogoData? {
        return fetch("$baseUrl/stock/$symbol/logo")?.let {
            mapper.readValue(it, LogoData::class.java)
        }
    }

    fun getFinancials(symbol: String): RecentFinancials? {
        return fetch("$baseUrl/stock/$symbol/financials")?.let {
            mapper.readValue(it, RecentFinancials::class.java)
        }
    }

    fun getSpread(symbol: String): List<Spread>? {
        return fetch("$baseUrl/stock/$symbol/effective-spread")?.let {
            mapper.readValue(it, listTypes[Spread::class.java])
        }
    }

    fun getOHLC(symbol: String): OHLC? {
        return fetch("$baseUrl/stock/$symbol/ohlc")?.let {
            mapper.readValue(it, OHLC::class.java)
        }
    }

    fun getSplits(symbol: String, range: Range = Range.Y5): List<Split>? {
        return fetch("$baseUrl/stock/$symbol/splits/${range.value.code}")?.let {
            mapper.readValue(it, listTypes[Split::class.java])
        }
    }

    // List of all supported symbols.
    fun getSymbols(): List<Symbol>? {
        return fetch("$baseUrl/ref-data/symbols")?.let {
            mapper.readValue(it, listTypes[Symbol::class.java])
        }
    }

    // 'range' refers to chart range, optional if chart is not in 'types'.
    // https://iextrading.com/developer/docs/#batch-requests
    fun getBatch(symbol: String, types: Set<BatchType> = batchTypes, range: Range = Range.M): Batch? {
        val params = mapOf(
            "types" to types.joinToString(",") { it.value },
            "range" to range.value.code,
            // Parameters that are sent to individual endpoints can be specified in batch calls
            // and will be applied to each supporting endpoint.
            // For example, param below applies to news (only last 10 will be fetched).
            "last" to "10"
        )

        return fetch("$baseUrl/stock/$symbol/batch", params)?.let {
            mapper.readValue(it, Batch::class.java)
        }
    }

    // 'range' refers to chart range, optional if chart is not in 'types'.
    fun getBatch(symbols: List<String>, types: Set<BatchType> = batchTypes, range: Range = Range.M): List<Batch>? {
        if (symbols.size > 100) {
            throw IllegalArgumentException("Up to 100 symbols allowed.")
        }

        val params = mutableMapOf(
            "symbols" to symbols.joinToString(","),
            "types" to types.joinToString(",") { it.value },
            "last" to "5"
        )
        if (BatchType.CHART in types) {
            // used to specify a chart range if 'chart' is used in 'types' parameter
            params["range"] = range.value.code
        }

        return fetch("$baseUrl/stock/market/batch", params)?.toJsonNode()?.map {
            mapper.convertValue(it, Batch::class.java)
        }
    }

    // Near real time, intraday API that provides Iex last sale price, size and time.
    // If no symbols are specified, will return data for all symbols.
    fun getLastTrade(symbols: List<String>? = null): List<LastTrade>? {
        val params = if (symbols != null && symbols.isNotEmpty()) {
            mapOf("symbols" to symbols.joinToString(","))
        } else emptyMap()

        return fetch("$baseUrl/tops/last", params)?.let {
            // https://github.com/iexg/IEX-API/issues/304
            mapper.readValue(it.replace("{},", ""), listTypes[LastTrade::class.java])
        }
    }

    // If no symbols are specified, will return data for all symbols.
    // The data is ~2MB uncompressed and is throttled at one request per second.
    fun getTops(symbols: List<String>? = null): List<Tops>? {
        val params = if (symbols != null && symbols.isNotEmpty()) {
            mapOf("symbols" to symbols.joinToString(","))
        } else emptyMap()

        return fetch("$baseUrl/tops", params)?.let {
            mapper.readValue(it, listTypes[Tops::class.java])
        }
    }

    // https://iextrading.com/developer/docs/#deep
    fun getDepth(symbol: String): Depth? {
        return fetch("$baseUrl/deep", mapOf("symbols" to symbol))?.let {
            mapper.readValue(it, Depth::class.java)
        }
    }

    // Shows Iex’s bids and asks for given symbols.
    // https://iextrading.com/developer/docs/#book51
    fun getBook(symbol: String): Book? {
        return mapper.convertValue(
            fetch("$baseUrl/deep/book", mapOf("symbols" to symbol))
                ?.toJsonNode()?.get(symbol.toUpperCase()),
            Book::class.java
        )
    }

    // https://iextrading.com/developer/docs/#trades
    fun getTrades(symbol: String, last: Int = 20): List<Trade>? {
        val params = mapOf(
            "symbols" to symbol,
            "last" to last.toString()
        )

        return fetch("$baseUrl/deep/trades", params)?.toJsonNode()?.get(symbol.toUpperCase())?.map {
            mapper.convertValue(it, Trade::class.java)
        }
    }

    // https://iextrading.com/developer/docs/#intraday
    fun getIntradayStats(): IntradayStats? {
        return fetch("$baseUrl/stats/intraday")?.let {
            mapper.readValue(it, IntradayStats::class.java)
        }
    }

    // https://iextrading.com/developer/docs/#records
    fun getRecordsStats(): RecordsStats? {
        return fetch("$baseUrl/stats/records")?.let {
            mapper.readValue(it, RecordsStats::class.java)
        }
    }

}