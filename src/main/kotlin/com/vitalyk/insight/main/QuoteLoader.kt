package com.vitalyk.insight.main

import com.vitalyk.insight.yahoo.YahooDataColumns
import kotlinx.coroutines.CommonPool
import kotlinx.coroutines.async
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate

typealias ExchangeName = String
typealias Ticker = String

fun loadDailyQuotesForTicker(records: CSVParser, dateFormat: SimpleDateFormat): List<Quote> {
    val result = mutableListOf<Quote>()

    records.forEach {
        val quote = Quote(
            LocalDate.parse(it.get(YahooDataColumns.date)),
            BigDecimal(it.get(YahooDataColumns.open)),
            BigDecimal(it.get(YahooDataColumns.high)),
            BigDecimal(it.get(YahooDataColumns.low)),
            BigDecimal(it.get(YahooDataColumns.close)),
            BigDecimal(it.get(YahooDataColumns.adjClose)),
            it.get(YahooDataColumns.volume).toLong()
        )
        result.add(quote)
    }

    return result
}

/**
 * E.g. val map = loadAllDailyQuotes(listOf("nasdaq", "nyse", "amex"))
 * Note: the above data structure will require just short of 8 GB of RAM.
 * Use -Xmx8192m VM option.
 */
fun loadAllDailyQuotes(exchanges: List<String>): Map<ExchangeName, Map<Ticker, List<Quote>>> {

    val basePath = "${AppSettings.Paths.storage}/${AppSettings.Paths.dailyData}/"
    val exchangeMap = mutableMapOf<ExchangeName, Map<Ticker, List<Quote>>>()

    for (exchange in exchanges) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val path = basePath + exchange
        val walker = File(path).walk().maxDepth(1)
        val tickerMap = mutableMapOf<Ticker, List<Quote>>()

        exchangeMap[exchange] = tickerMap

        for (file in walker) {
            if (file.isFile()) {
                val symbol = file.nameWithoutExtension.trim()
                if (symbol != exchange) {
                    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(file.reader())

                    tickerMap[symbol] = loadDailyQuotesForTicker(records, dateFormat)
                }
            }
        }
    }

    return exchangeMap
}

suspend fun asyncLoadAllDailyQuotes(exchanges: List<String>): Map<ExchangeName, Map<Ticker, List<Quote>>> {

    val basePath = "${AppSettings.Paths.storage}/${AppSettings.Paths.dailyData}/"
    val exchangeMap = mutableMapOf<ExchangeName, Map<Ticker, List<Quote>>>()

    for (exchange in exchanges) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val path = basePath + exchange
        val walker = File(path).walk().maxDepth(1)
        val tickerMap = mutableMapOf<Ticker, List<Quote>>()

        exchangeMap[exchange] = tickerMap

        for (file in walker) {
            async(CommonPool) {
                if (file.isFile()) {
                    val symbol = file.nameWithoutExtension.trim()
                    if (symbol != exchange) {
                        val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(file.reader())

                        tickerMap[symbol] = loadDailyQuotesForTicker(records, dateFormat)
                    }
                }
            }.await()
        }
    }

    return exchangeMap
}