package com.vitalyk.insight.main

import com.vitalyk.insight.helpers.toPrettyJson
import com.vitalyk.insight.helpers.writeToFile
import com.vitalyk.insight.yahoo.YahooData
import com.vitalyk.insight.yahoo.fetchNews
import com.vitalyk.insight.yahoo.getYahooSummary
import com.vitalyk.insight.yahoo.yahooGet
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis


data class Security(
    val symbol: String,
    val name: String,
    val sector: String = "",
    val industry: String = ""
)

private fun Exchange.noSecurities() = emptyList<Security>()

class Exchange(
    val code: String,
    val url: String = "",
    val getSecurities: Exchange.() -> List<Security> = Exchange::noSecurities
) {
    val logger: Logger by lazy { LoggerFactory.getLogger(this::class.java.name) }
}

private val exchanges = listOf(
    Exchange(
        "nasdaq",
        "http://www.nasdaq.com/",
        Exchange::getExchangeSecuritiesFromNasdaq
    ),
    Exchange(
        "nyse",
        "https://www.nyse.com/index",
        Exchange::getExchangeSecuritiesFromNasdaq
    ),
    Exchange(
        "amex",
        "https://www.nyse.com/markets/nyse-mkt",
        Exchange::getExchangeSecuritiesFromNasdaq
    )

)

private var isFetchingEndOfDayData = false

fun fetchEndOfDayData() {
    if (!isFetchingEndOfDayData) {
        isFetchingEndOfDayData = true

        asyncMassFetchDailyData()
        asyncMassFetchSummary()

        isFetchingEndOfDayData = false
    } else {
        appLogger.warn("The end of day data fetching is already in progress.")
    }
}

// http://stackoverflow.com/questions/32935470/how-to-convert-list-to-map-in-kotlin
val exchangeMap = exchanges.map { it.code to it }.toMap()

fun asyncMassFetchDailyData() {
    appLogger.info("Fetching daily data ...")
    val time = measureTimeMillis {
        runBlocking {
            exchangeMap["nasdaq"]?.asyncFetchDailyData()
            exchangeMap["nyse"]?.asyncFetchDailyData()
            exchangeMap["amex"]?.asyncFetchDailyData()
        }
    }
    appLogger.info("Fetching daily data completed in $time ms.")
}

/**
 * Fetches last day's intraday data for major exchanges.
 */
fun asyncMassFetchIntradayData() {
    val name = "Fetch intraday data for all exchanges"
    appLogger.info("'$name' started.")
    val time = measureTimeMillis {
        runBlocking {
            exchangeMap["nasdaq"]?.asyncFetchIntradayData()
            exchangeMap["nyse"]?.asyncFetchIntradayData()
            exchangeMap["amex"]?.asyncFetchIntradayData()
        }
    }
    appLogger.info("'$name' completed in $time ms.")
}

fun syncFetchAllIntradayData() {
    val name = "Fetch intraday data for all exchanges"
    appLogger.info("'$name' started.")
    val time = measureTimeMillis {
        exchangeMap["nasdaq"]?.syncFetchIntradayData()
        exchangeMap["nyse"]?.syncFetchIntradayData()
        exchangeMap["amex"]?.syncFetchIntradayData()
    }
    appLogger.info("'$name' completed in $time ms.")
}

/**
 * Fetches last day's summary data for major exchanges.
 */
fun asyncMassFetchSummary() {
    appLogger.info("Fetching summary data ...")
    val time = measureTimeMillis {
        runBlocking {
            exchangeMap["nasdaq"]?.asyncFetchSummary()
            exchangeMap["nyse"]?.asyncFetchSummary()
            exchangeMap["amex"]?.asyncFetchSummary()
        }
    }
    appLogger.info("Fetching summaries completed in $time ms.")
}

fun massFetchSummary() {
    appLogger.info("Fetching summary data (sync) ...")
    val time = measureTimeMillis {
        exchangeMap["nasdaq"]?.fetchSummary()
        exchangeMap["nyse"]?.fetchSummary()
        exchangeMap["amex"]?.fetchSummary()
    }
    appLogger.info("Fetching summaries completed in $time ms.")
}

fun Exchange.fetchSummary() {
    val exchange = this
    // No matter what time and date it is locally, we are interested in what date it is in New York.
    val nyDateTime = ZonedDateTime.now(ZoneId.of("America/New_York"))
    val nyIsoDate: String = nyDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val securities = exchange.getSecurities()

    securities.map { (symbol) ->
        val data = getYahooSummary(symbol)
        if (data != null) {
            try {
                val path = "${AppSettings.Paths.summary}/$nyIsoDate/${exchange.code}/$symbol.json"
                data.toPrettyJson().writeToFile(path)
            } catch (e: Error) {
                exchange.logger.error(e.message)
            }
        }
    }
}


// In the future we may find a better way to fetch the list of securities
// (from exchanges directly?), where each exchange has it unique fetching logic.
// For now, Nasdaq has a list of securities traded on Nasdaq, NYSE and AMEX, so we use that:
// http://www.nasdaq.com/screening/company-list.aspx
fun Exchange.getExchangeSecuritiesFromNasdaq(): List<Security> {

    return try {
        yahooGet("http://www.nasdaq.com/screening/companies-by-name.aspx", mapOf(
            "letter" to "0",
            "render" to "download",
            "exchange" to this.code
        ))
    } catch (e: IOException) {
        logger.error(e.message)
        null
    }?.let {
        val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(it.reader())

        // Convert CSVRecord's to instances of the Security data class.
        return records.map {
            Security(
                it.get("Symbol").trim(),
                it.get("Name"),
                it.get("Sector"),
                it.get("industry")
            )
        }
    } ?: emptyList()
}

/**
 * There is quite a delay between the market close and the time that day's EOD data becomes available
 * (more than 3 hours).
 */
suspend fun Exchange.asyncFetchDailyData() {
    val exchange = this
    // No matter what time and date it is locally, we are interested in what date it is in New York.
    val now = ZonedDateTime.now(ZoneId.of("America/New_York"))
    var then = now.minusYears(1)

    val baseUrl = "https://query1.finance.yahoo.com/v7/finance/download"
    val crumb = "Al6fqK3.l4h"

    val securities = async { exchange.getSecurities() }.await()

    securities.map { (symbol) -> async {
        val filename = "${AppSettings.Paths.dailyData}/${exchange.code}/$symbol.csv"
        val file = File(filename)
        val fileExists = file.exists()

        var existingRecords: MutableList<CSVRecord> = mutableListOf()

        if (fileExists) {
            val existingData = file.readText()
            val existingRecordsParser = CSVFormat.DEFAULT.parse(existingData.reader())
            existingRecords = existingRecordsParser.records
        }

        val newDataOnly = existingRecords.size < 2 // no existing records or just a header

        // Quotes for a new listing won't go back 70 years,
        // but an old company might change its ticker.
        if (newDataOnly) {
            then = now.minusYears(70)
        }

        // The periods are the number of seconds between epoch and the start of the day in UTC.
        val params = "?period1=${then.toInstant().toEpochMilli() / 1000}" +
            "&period2=${now.toInstant().toEpochMilli() / 1000}" +
            "&interval=1d" +
            "&events=history" +
            "&crumb=$crumb"
        val requestUrl = "$baseUrl/$symbol$params"

        try {
            yahooGet(requestUrl)
        } catch (e: IOException) {
            exchange.logger.error(e.message)
            null
        }?.let {
            if (!newDataOnly) {
                try {
                    val fetchedRecordsParser = CSVFormat.DEFAULT.parse(it.reader())
                    val fetchedRecords = fetchedRecordsParser.records

                    if (fetchedRecords.size <= 1) {
                        exchange.logger.warn("${exchange.code}:$symbol - no data or header only." +
                            " Probably no longer traded.")
                    }
                    // if headers match
                    else if (existingRecords.first().toList() == fetchedRecords.first().toList()) {
                        // Note: the data we get will have the CSV header as the first line
                        // 'Date,Open,High,Low,Close,Adj Close,Volume', and the subsequent lines will
                        // represent each day's data in chronological order. Previous Yahoo Finance API version
                        // returned the data in reverse order, which we'll continue to use here, as it is handy
                        // to fetch the first n records, knowing they'll represent n most recent trading days
                        // (except for stocks that are no longer trading).
                        val headlessRecords = fetchedRecords.subList(1, fetchedRecords.size).asReversed()
                        val fileWriter = FileWriter(file, false)
                        val csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT)

                        // Sample CSV file:

                        // Date,Open,High,Low,Close,Volume,Adj Close
                        //
                        // --- NEW RECORDS GO HERE ---
                        //
                        // 2017-02-24,135.910004,136.660004,135.279999,136.660004,21690900,136.660004
                        // 2017-02-23,137.380005,137.479996,136.300003,136.529999,20704100,136.529999
                        // ...

                        val lastFetchedDate = existingRecords[1].get(0)

                        // write new records, starting with header
                        csvPrinter.printRecord(fetchedRecords[0])
                        var i = 0
                        while (i < headlessRecords.size && headlessRecords[i].get(0) != lastFetchedDate) {
                            csvPrinter.printRecord(headlessRecords[i])
                            i++
                        }

                        // write existing records, skip header
                        i = 1
                        while (i < existingRecords.size) {
                            csvPrinter.printRecord(existingRecords[i])
                            i++
                        }

                        csvPrinter.flush()
                        csvPrinter.close()
                    } else {
                        exchange.logger.error("$symbol: CSV headers don't match.\nRequest URL: $requestUrl\n" +
                            "Expected '${fetchedRecords.first().toList()}' to be '${existingRecords.first().toList()}'")
                    }
                } catch (e: Error) {
                    exchange.logger.error("Updating existing symbol ($symbol) data failed: ${e.message}")
                }
            } else {
                try {
                    file.parentFile.mkdirs()
                    file.writeText(it)
                } catch (e: Error) {
                    exchange.logger.error("Writing new symbol ($symbol) date failed: ${e.message}")
                }
            }
        }

    } }.forEach { it.await() }
}

suspend fun Exchange.asyncFetchIntradayData() {
    val exchange = this
    // No matter what time and date it is locally, we are interested in what date it is in New York.
    val nyDateTime = ZonedDateTime.now(ZoneId.of("America/New_York"))
    val nyIsoDate: String = nyDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val securities = async { exchange.getSecurities() }.await()

    securities.map { (symbol) ->
        async {
            val data = fetchIntradayData(symbol)
            if (data != null) {
                try {
                    data.writeToFile("${AppSettings.Paths.intradayData}/$nyIsoDate/${exchange.code}/$symbol.json")
                } catch (e: Error) {
                    exchange.logger.error(e.message)
                }
            }
        }
    }.forEach { it.await() }
}

fun Exchange.syncFetchIntradayData() {
    val exchange = this
    // No matter what time and date it is locally, we are interested in what date it is in New York.
    val nyDateTime = ZonedDateTime.now(ZoneId.of("America/New_York"))
    val nyIsoDate: String = nyDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val securities = exchange.getSecurities()
    val maxRequestAttempts = 10

    securities.map { (symbol) ->
        var count = 0
        var data: String?

        do {
            count++
            data = fetchIntradayData(symbol)
            if (data != null) {
                try {
                    data.writeToFile("${AppSettings.Paths.intradayData}/$nyIsoDate/${exchange.code}/$symbol.json")
                } catch (e: Error) {
                    exchange.logger.error(e.message)
                }
            }
        } while (data == null && count < maxRequestAttempts)

        if (data == null) {
            println("Failed fetching $symbol after $maxRequestAttempts attempts.")
        }
    }
}

// Yahoo API https://chartapi.finance.yahoo.com/instrument/1.0/<symbol>/chartdata;type=quote;range=1d/json
// was discontinued on May 17, 2017. This is just a stub.
fun fetchIntradayData(symbol: String): String? = null

suspend fun Exchange.asyncFetchSummary() {
    val exchange = this
    // No matter what time and date it is locally, we are interested in what date it is in New York.
    val nyDateTime = ZonedDateTime.now(ZoneId.of("America/New_York"))
    val nyIsoDate: String = nyDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val path = "${AppSettings.Paths.summary}/$nyIsoDate/${exchange.code}"

    if (File(path).exists()) {
        exchange.logger.warn("'$path' already exists. The data won't be fetched.")
        return
    }

    val securities = async { exchange.getSecurities() }.await()

    securities.map { (symbol) ->
        async {
            val data = getYahooSummary(symbol)
            if (data != null) {
                try {
                    val filePath = "$path/$symbol.json"
                    data.toPrettyJson().writeToFile(filePath)
                } catch (e: Error) {
                    exchange.logger.error(e.message)
                }
            }
        }
    }.forEach { it.await() }
}

// Creates a map of ticker symbols to company names for all exchanges.
fun createTickerToNameJson() {
    async {
        var map = mutableMapOf<String, MutableMap<String, String>>()
        StockFetcherUS.forAll { exchange, companies ->
            val symbolNames = mutableMapOf<String, String>()
            companies.forEach { symbolNames[it.symbol] = it.name }
            map[exchange] = symbolNames
        }
        Settings.save(map, "exchanges.json")
    }
}

object StockFetcherUS {
    val baseUrl = "http://www.nasdaq.com/screening/companies-by-name.aspx"

    val exchanges = listOf("nasdaq", "nyse", "amex")

    val logger: Logger by lazy { LoggerFactory.getLogger(this::class.java.name) }

    fun getStocks(exchange: String): List<Security>? {
        if (exchange in exchanges) {
            return try {
                yahooGet(baseUrl, mapOf(
                    "letter" to "0",
                    "render" to "download",
                    "exchange" to exchange
                ))
            } catch (e: IOException) {
                logger.error(e.message)
                null
            }?.let {
                val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(it.reader())

                records.map {
                    Security(
                        it.get("Symbol"),
                        it.get("Name"),
                        it.get("Sector"),
                        it.get("industry")
                    )
                }
            }
        }
        return null
    }

    fun forAll(f: (exchange: String, securities: List<Security>) -> Unit) {
        for (exchange in exchanges) {
            val stocks = getStocks(exchange)
            if (stocks != null) {
                f(exchange, stocks)
            }
        }
    }

    fun fetchData() {
        forAll { exchange, companies ->
            for ((symbol) in companies) {
                val file = File("${AppSettings.Paths.dailyData}/$exchange/$symbol.csv")
                file.parentFile.mkdirs()
                val data = YahooData(symbol)
                    .startDate(LocalDate.now().minusYears(70))
                    .execute()
                    .data()

                file.writeText(data)
            }
        }
    }

    fun asyncFetchNews() = runBlocking {
        val date: String = LocalDate.now().toString()
        val jobs = arrayListOf<Deferred<Unit>>()

        for (exchange in exchanges) {
            val companies = getStocks(exchange)

            companies?.map { (symbol) ->
                async {
                    val json = fetchNews(symbol).toPrettyJson()

                    val file = File("${AppSettings.Paths.news}/$date/$exchange/$symbol.json")
                    file.parentFile.mkdirs()
                    file.writeText(json)
                }
            }?.forEach { it.await() }
        }

        jobs.forEach { it.await() }
    }

}