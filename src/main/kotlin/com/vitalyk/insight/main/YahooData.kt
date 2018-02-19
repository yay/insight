package com.vitalyk.insight.main

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.StringReader
import java.net.MalformedURLException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

private val financeDownloadUrl = "https://query1.finance.yahoo.com/v7/finance/download/"

data class YFinanceAuth(
    val cookie: String,
    val crumb: String
)

fun getYFinanceAuth(symbol: String = "AAPL"): YFinanceAuth? {
    val url = "https://uk.finance.yahoo.com/quote/$symbol/history"
    val httpUrl = HttpUrl.parse(url) ?: throw MalformedURLException("Invalid HttpUrl.")
    val urlBuilder = httpUrl.newBuilder()
    val requestUrl = urlBuilder.build().toString()
    val request = Request.Builder()
        .addHeader("User-Agent", UserAgents.chrome)
        .url(requestUrl)
        .build()
    val response = HttpClients.yahoo.newCall(request).execute()
    val body = response.body()

    val cookieHeader = response.headers("set-cookie")

    if (cookieHeader.isNotEmpty()) {
        val cookie = response.headers("set-cookie").first().split(";").first()
        // Example: "CrumbStore":{"crumb":"l45fI\u002FklCHs"}
        // val crumbRegEx = Regex(""".*"CrumbStore":\{"crumb":"([^"]+)"}""", RegexOption.MULTILINE)
        // val crumb = crumbRegEx.find("body.string()")?.groupValues?.get(1) // takes ages

        if (body != null) {
            val text = body.string()
            val keyword = "CrumbStore\":{\"crumb\":\""
            val start = text.indexOf(keyword)
            val end = text.indexOf("\"}", start)
            val crumb = text.substring(start + keyword.length until end)
            return if (crumb.isNotBlank()) YFinanceAuth(cookie, crumb) else null
        }
    } else {
        throw Exception("No cookie found.")
    }

    return null
}

fun fetchDailyData(symbol: String, years: Long = 1): String {
    // See below for the 'crumb':
    // http://blog.bradlucas.com/posts/2017-06-02-new-yahoo-finance-quote-download-url/
    // https://github.com/dennislwy/YahooFinanceAPI

    val now = ZonedDateTime.now(ZoneId.of("America/New_York"))
    val then = now.minusYears(years)
    val crumb = "vjMESKwkGZA"
    val params =
        "?period1=${then.toInstant().toEpochMilli() / 1000}" +
        "&period2=${now.toInstant().toEpochMilli() / 1000}" +
        "&interval=1d" + // [1m, 2m, 5m, 15m, 30m, 60m, 90m, 1h, 1d, 5d, 1wk, 1mo, 3mo]
        "&events=history" +
        "&crumb=$crumb"  // required along with a cookie, changes with every login to Yahoo Finance
    val url = "$financeDownloadUrl$symbol$params"

    val result = yahooGet(url)
    when (result) {
        is YahooGetSuccess -> {
            return result.data
        }
        is YahooGetFailure -> {
            getAppLogger().error("Code: ${result.code}, Message: ${result.message}\n$url")
        }
    }

    return ""
}

fun String.parseYahooCSV(): Iterable<CSVRecord> =
    CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(StringReader(this))

fun Iterable<CSVRecord>.toStockList(): List<StockSymbol> {
    val header = yahooDataHeader
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")

    return this.map { it ->
        val date = try {
            dateFormat.parse(it.get(YahooDataColumns.date))
        } catch (e: ParseException) {
            YahooDataColumns.date
        }
        try {
            StockSymbol(
                dateFormat.parse(it.get(header[0])),
                it.get(header[1]).toFloat(),
                it.get(header[2]).toFloat(),
                it.get(header[3]).toFloat(),
                it.get(header[4]).toFloat(),
                it.get(header[5]).toInt(),
                it.get(header[6]).toFloat()
            )
        } catch (e: ParseException) {
            StockSymbol(
                dateFormat.parse(it.get(header[0])),
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                0,
                0.0f
            )
        } catch (e: NumberFormatException) {
            StockSymbol(
                dateFormat.parse(it.get(header[0])),
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                0,
                0.0f
            )
        }
    }
}




enum class DataFrequency {
    DAY, WEEK, MONTH
}

object YahooDataColumns {
    val date = "Date"
    val open = "Open"
    val high = "High"
    val low = "Low"
    val close = "Close"
    val adjClose = "Adj Close"
    val volume = "Volume"
}

// In Kotlin, unlike Java or C#, classes do not have static methods.
// In most cases, it's recommended to simply use package-level functions instead.
// Or a companion object. Note that, even though the members of companion objects
// look like static members in other languages, at runtime those are still instance
// members of real objects.
//val yahoo.getYahooDataHeader: Array<String> = arrayOf("Date", "Open", "High", "Low", "Close", "Volume", "Adj Close")
val yahooDataHeader: Array<String> = arrayOf(
    YahooDataColumns.date,
    YahooDataColumns.open,
    YahooDataColumns.high,
    YahooDataColumns.low,
    YahooDataColumns.close,
    YahooDataColumns.volume,
    YahooDataColumns.adjClose
)

class YahooData(var symbol: String, var frequency: DataFrequency = DataFrequency.DAY) {

    /*

    TODO: unit tests

    Example base URL params:

    ?s=AVGO &a=1 &b=10 &c=2016 &d=1 &e=10 &f=2017 &g=d &ignore=.csv
    ?s=AVGO &a=1 &b=10 &c=2016 &d=1 &e=10 &f=2017 &g=w &ignore=.csv
    ?s=AVGO &a=1 &b=10 &c=2016 &d=1 &e=10 &f=2017 &g=m &ignore=.csv

    */

    private val baseUrl: String = "http://chart.finance.yahoo.com/table.csv"
    private val urlBuilder: HttpUrl.Builder

    val connectTimeout: Long = 10
    val readTimeout: Long = 30

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .build()
    }

    private var data: String = ""
    private lateinit var records: Iterable<CSVRecord>
    private val logger by lazy { Logger.getLogger(this::class.java.name) }

    private val symbolParam = "s"

    private val format = ".csv"
    private val formatParam = "ignore"

    private val startYearParam = "c"
    private val endYearParam = "f"

    private val startMonthParam = "a"
    private val endMonthParam = "d"

    private val startDateParam = "b"
    private val endDateParam = "e"

    var endDate: LocalDate = LocalDate.now()
    var startDate: LocalDate = endDate.minusYears(1)

    private val frequencyParam = "g"
    private val frequencyMap = mapOf(
        DataFrequency.DAY to "d",
        DataFrequency.WEEK to "w",
        DataFrequency.MONTH to "m"
    )

    init {
        val httpUrl = HttpUrl.parse(baseUrl)

        if (httpUrl != null) {
            urlBuilder = httpUrl.newBuilder()
        } else {
            throw Error("Bad URL.")
        }
    }

    fun startDate(date: LocalDate): YahooData {
        urlBuilder
            .addQueryParameter(startYearParam, date.year.toString())
            .addQueryParameter(startMonthParam, date.monthValue.toString())
            .addQueryParameter(startDateParam, date.dayOfMonth.toString())

        return this
    }

    fun endDate(date: LocalDate): YahooData {
        urlBuilder
            .addQueryParameter(endYearParam, date.year.toString())
            .addQueryParameter(endMonthParam, date.monthValue.toString())
            .addQueryParameter(endDateParam, date.dayOfMonth.toString())

        return this
    }

    fun execute(): YahooData {
        urlBuilder.addQueryParameter(symbolParam, symbol)

        startDate(startDate)
        endDate(endDate)

        urlBuilder
            .addQueryParameter(frequencyParam, frequencyMap[frequency])
            .addQueryParameter(formatParam, format)

        val url = urlBuilder.build().toString()

        logger.info { "Sending historical data request for $symbol:\n$url" }

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        response.use {
            if (it.code() == 200) {
                val body = it.body()

                if (body != null) {
                    data = body.string()
                }
            }
        }

        return this
    }

    fun parse(): YahooData {
//        CSVFormat.DEFAULT.withHeader(*yahoo.getYahooDataHeader).parse(StringReader(data))
        records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(StringReader(data))

        return this
    }

    fun print(): YahooData {
        println(data)
        return this
    }

    fun data(): String {
        return data
    }

    fun records(): Iterable<CSVRecord> {
        return records
    }

    fun ohlc(): List<OHLC> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        return records.map { it ->
            OHLC(
                dateFormat.parse(it.get(YahooDataColumns.date)).toInstant().toEpochMilli(),
                it.get(YahooDataColumns.open).toDouble(),
                it.get(YahooDataColumns.high).toDouble(),
                it.get(YahooDataColumns.low).toDouble(),
                it.get(YahooDataColumns.close).toDouble(),
                it.get(YahooDataColumns.adjClose).toDouble(),
                it.get(YahooDataColumns.volume).toLong()
            )
        }
    }

    fun list(): List<StockSymbol> {
        val header = yahooDataHeader
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        return records.map { it ->
            StockSymbol(
                dateFormat.parse(it.get(header[0])),
                it.get(header[1]).toFloat(),
                it.get(header[2]).toFloat(),
                it.get(header[3]).toFloat(),
                it.get(header[4]).toFloat(),
                it.get(header[5]).toInt(),
                it.get(header[6]).toFloat()
            )
        }
    }

    fun call(f: YahooData.() -> Unit): YahooData {
        f()
        return this
    }

}
