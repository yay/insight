package com.vitalyk.insight.yahoo

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.UserAgents
import com.vitalyk.insight.main.getAppLogger
import okhttp3.HttpUrl
import okhttp3.Request
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

data class ChartPoint(
    @JsonProperty("Date")
    val date: Date,

    @JsonProperty("Open")
    val open: Double,

    @JsonProperty("High")
    val high: Double,

    @JsonProperty("Low")
    val low: Double,

    @JsonProperty("Close")
    val close: Double,

    @JsonProperty("Adj Close")
    val adjClose: Double,

    @JsonProperty("Volume")
    val volume: Long
)

enum class ChartInterval(val value: String) {
    DAY("1d"),
    DAY5("5d"),
    WEEK("1wk"),
    MONTH("1mo"),
    MONTH3("3mo")
}

fun isDistributionDay(prev: ChartPoint, curr: ChartPoint): Boolean {
    // Volume is higher than the previous day and decline is greater than 0.2%.
    return (curr.volume > prev.volume) && (curr.close / prev.close - 1.0 < -0.002)
}

fun getDistributionDays(symbol: String): Pair<MutableList<ChartPoint>?, MutableList<Int>> {
    val days = mutableListOf<Int>()
    val points = getChartPoints(symbol, 5, ChronoUnit.WEEKS)?.apply {
        this.reduceIndexed { index, prev, curr ->
            if (isDistributionDay(prev, curr)) {
                days.add(index)
            }
            curr
        }
    }
    return Pair(points, days)
}

fun getChartPoints(symbol: String, amount: Long = 1, unit: ChronoUnit = ChronoUnit.YEARS,
                   interval: ChartInterval = ChartInterval.DAY): MutableList<ChartPoint>? {
    // See below for the 'crumb':
    // http://blog.bradlucas.com/posts/2017-06-02-new-yahoo-finance-quote-download-url/
    // https://github.com/dennislwy/YahooFinanceAPI

    val now = ZonedDateTime.now(ZoneId.of("America/New_York"))
    val ago = now.minus(amount, unit)
    val crumb = "vjMESKwkGZA"
    val url = "https://query1.finance.yahoo.com/v7/finance/download/"

    val response = yahooFetch("$url$symbol", listOf(
        "period1" to "${ago.toInstant().toEpochMilli() / 1000}",
        "period2" to "${now.toInstant().toEpochMilli() / 1000}",
        "interval" to interval.value,
        "events" to "history",
        "crumb" to crumb // required along with a cookie, changes with every login to Yahoo Finance
    ))

    return response?.let {
        val mapper = CsvMapper()

        val schema = CsvSchema.builder()
            .addColumn("Date")
            .addColumn("Open")
            .addColumn("High")
            .addColumn("Low")
            .addColumn("Close")
            .addColumn("Adj Close")
            .addColumn("Volume")
            .build().withHeader()

        val reader = mapper.readerFor(ChartPoint::class.java).with(schema)

        reader.readValues<ChartPoint>(it).readAll()
    }
}

fun yahooFetch(url: String, params: List<Pair<String, String>>? = null): String? {
    val httpUrl = HttpUrl.parse(url) ?: throw Error("Bad URL.")
    val urlBuilder = httpUrl.newBuilder()
    if (params != null) {
        for ((param, value) in params) {
            urlBuilder.addQueryParameter(param, value)
        }
    }
    val request = Request.Builder()
        .addHeader("User-Agent", UserAgents.chrome)
        .url(urlBuilder.build().toString())
        .build()
    val response = HttpClients.yahoo.newCall(request).execute()

    response.use {
        return if (it.isSuccessful) {
            try {
                it.body()?.string()
            } catch (e: IOException) {
                getAppLogger().error("${e.message}, $it")
                null
            }
        } else {
            getAppLogger().error(it.toString())
            null
        }
    }
}