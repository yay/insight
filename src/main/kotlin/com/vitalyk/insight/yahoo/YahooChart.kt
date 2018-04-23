package com.vitalyk.insight.yahoo

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.text.SimpleDateFormat
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