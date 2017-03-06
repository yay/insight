package main

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.StringReader
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

enum class DataFrequency {
    DAY, WEEK, MONTH
}

object YahooDataColumns {
    val time     = "Date"
    val open     = "Open"
    val high     = "High"
    val low      = "Low"
    val close    = "Close"
    val adjClose = "Adj Close"
    val volume   = "Volume"
}
// In Kotlin, unlike Java or C#, classes do not have static methods.
// In most cases, it's recommended to simply use package-level functions instead.
// Or a companion object. Note that, even though the members of companion objects
// look like static members in other languages, at runtime those are still instance
// members of real objects.
//val main.getYahooDataHeader: Array<String> = arrayOf("Date", "Open", "High", "Low", "Close", "Volume", "Adj Close")
val YahooDataHeader: Array<String> = arrayOf(
        YahooDataColumns.time,
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
    private val urlBuilder = HttpUrl.parse(baseUrl).newBuilder()

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
    private val log by lazy { Logger.getLogger(this::class.java.name) }

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
    private val frequencyMap = mapOf<DataFrequency, String>(
            DataFrequency.DAY to "d",
            DataFrequency.WEEK to "w",
            DataFrequency.MONTH to "m"
    )

    init {
        endDate.year
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

        log.info { "Sending historical data request for $symbol:\n$url" }

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        response.use {
            if (it.code() == 200) {
                data = it.body().string()
            }
        }

        return this
    }

    fun parse(): YahooData {
//        CSVFormat.DEFAULT.withHeader(*main.getYahooDataHeader).parse(StringReader(data))
        records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(StringReader(data))

        return this
    }

    fun print(): YahooData {
        println(data)
        return this
    }

    fun data(): String { return data }

    fun records(): Iterable<CSVRecord> { return records }

    fun ohlc(): List<OHLC> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        return records.map { it -> OHLC(
                dateFormat.parse(it.get(YahooDataColumns.time)).toInstant().toEpochMilli(),
                it.get(YahooDataColumns.open).toDouble(),
                it.get(YahooDataColumns.high).toDouble(),
                it.get(YahooDataColumns.low).toDouble(),
                it.get(YahooDataColumns.close).toDouble(),
                it.get(YahooDataColumns.adjClose).toDouble(),
                it.get(YahooDataColumns.volume).toLong()
        ) }
    }

    fun list(): List<StockSymbol> {
        val header = YahooDataHeader
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        return records.map { it -> StockSymbol(
                dateFormat.parse(it.get(header[0])),
                it.get(header[1]).toFloat(),
                it.get(header[2]).toFloat(),
                it.get(header[3]).toFloat(),
                it.get(header[4]).toFloat(),
                it.get(header[5]).toInt(),
                it.get(header[6]).toFloat()
        ) }
    }

    fun call(f: YahooData.() -> Unit): YahooData {
        f()
        return this
    }

}