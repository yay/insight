package main

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.time.LocalDate
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
        val name: String,
        val location: String,
        val url: String = "",
        val getSecurities: Exchange.() -> List<Security> = Exchange::noSecurities
) {
    val logger: Logger by lazy { LoggerFactory.getLogger(this::class.java.name) }
}

private val exchanges = listOf(
        Exchange("nasdaq", "NASDAQ", "New York City", "http://www.nasdaq.com/", // founded 1971
                Exchange::getExchangeSecuritiesFromNasdaq
        ),
        Exchange("nyse", "New York Security Exchange", "New York City", "https://www.nyse.com/index", // founded 1817
                Exchange::getExchangeSecuritiesFromNasdaq
        ),
        Exchange("amex", "NYSE MKT", "New York City", "https://www.nyse.com/markets/nyse-mkt", // founded 1908
                Exchange::getExchangeSecuritiesFromNasdaq
        )

)

// http://stackoverflow.com/questions/32935470/how-to-convert-list-to-map-in-kotlin
val exchangeMap = exchanges.map { it.code to it }.toMap()

fun fetchDailyData() {
    getAppLogger().debug("Fetching daily data ...")
    val time = measureTimeMillis {
        runBlocking {
            exchangeMap["nasdaq"]?.asyncFetchDailyData()
            exchangeMap["nyse"]?.asyncFetchDailyData()
            exchangeMap["amex"]?.asyncFetchDailyData()
        }
    }
    getAppLogger().debug("Fetching daily data completed in $time ms.")
}

/**
 * Fetches last day's intraday data for major exchanges.
 */
fun fetchIntradayData() {
    getAppLogger().debug("Fetching intraday data ...")
    val time = measureTimeMillis {
        runBlocking {
            exchangeMap["nasdaq"]?.asyncFetchIntradayData()
            exchangeMap["nyse"]?.asyncFetchIntradayData()
            exchangeMap["amex"]?.asyncFetchIntradayData()
        }
    }
    getAppLogger().debug("Fetching intraday data completed in $time ms.")
}

/**
 * Fetches last day's summary data for major exchanges.
 */
fun fetchSummary() {
    getAppLogger().debug("Fetching summary data ...")
    val time = measureTimeMillis {
        runBlocking {
            exchangeMap["nasdaq"]?.asyncFetchSummary()
            exchangeMap["nyse"]?.asyncFetchSummary()
            exchangeMap["amex"]?.asyncFetchSummary()
        }
    }
    getAppLogger().debug("Fetching summaries completed in $time ms.")
}


// In the future we may find a better way to fetch the list of securities
// (from exchanges directly?), where each exchange has it unique fetching logic.
// For now, Nasdaq has a list of securities traded on Nasdaq, NYSE and AMEX, so we use that:
// http://www.nasdaq.com/screening/company-list.aspx
fun Exchange.getExchangeSecuritiesFromNasdaq(): List<Security> {

    val result = httpGet("http://www.nasdaq.com/screening/companies-by-name.aspx", mapOf(
            "letter" to "0",
            "render" to "download",
            "exchange" to this.code
    ))

    when (result) {
        is GetSuccess -> {
            val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(StringReader(result.data))

            return records.map { it -> Security(
                    it.get("Symbol"),
                    it.get("Name"),
                    it.get("Sector"),
                    it.get("industry")
            ) }

        }
        is GetError -> {
            logger.error(result.message)
        }
    }

    return emptyList<Security>()
}

/**
 * There is quite a delay between the market close and the time that day's EOD data becomes available
 * (more then 3 hours).
 */
suspend fun Exchange.asyncFetchDailyData() {
    val exchange = this
    // No matter what time and date it is locally, we are interested in what date it is in New York.
    val now = DateTime().withZone(DateTimeZone.forID("America/New_York"))
    var then = now.minusYears(1)

    val baseUrl = "http://chart.finance.yahoo.com/table.csv"

    val securities = async(CommonPool) { exchange.getSecurities() }.await()

    securities.map { (symbol) ->
        async(CommonPool) {
            val filename = "${AppSettings.paths.dailyData}/${exchange.code}/$symbol.csv"
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

            val params = "&a=${then.monthOfYear}&b=${then.dayOfMonth}&c=${then.year}" +
                    "&d=${now.monthOfYear}&e=${now.dayOfMonth}&f=${now.year}" +
                    "&g=d" +
                    "&ignore=.csv"

            val requestUrl = "$baseUrl?s=$symbol$params"
            val result = httpGet(requestUrl)

            when (result) {
                is GetSuccess -> {
                    if (!newDataOnly) {
                        try {
                            val fetchedRecordsParser = CSVFormat.DEFAULT.parse(result.data.reader())
                            val fetchedRecords = fetchedRecordsParser.records

                            // if headers match
                            if (existingRecords.first().toList() == fetchedRecords.first().toList()) {

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
                                var i = 1
                                while (i < fetchedRecords.size && fetchedRecords[i].get(0) != lastFetchedDate) {
                                    csvPrinter.printRecord(fetchedRecords[i])
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
                                exchange.logger.error("$symbol: CSV headers don't match.\nRequest URL: $requestUrl")
                            }
                        } catch (e: Error) {
                            exchange.logger.error("Updating existing symbol ($symbol) data failed: ${e.message}")
                        }
                    } else {
                        try {
                            file.parentFile.mkdirs()
                            file.writeText(result.data)
                        } catch (e: Error) {
                            exchange.logger.error("Writing new symbol ($symbol) date failed: ${e.message}")
                        }
                    }
                }
                is GetError -> {
                    getAppLogger().warn("Daily data request: $requestUrl - ${result.code} - ${result.message}")
                }
            }
        }
    }.forEach { it.await() }
}

suspend fun Exchange.asyncFetchIntradayData() {
    val exchange = this
    // No matter what time and date it is locally, we are interested in what date it is in New York.
    val dateTime = DateTime().withZone(DateTimeZone.forID("America/New_York"))
    val dateString: String = dateTime.toString("yyyy-MM-dd")
    val securities = async(CommonPool) { exchange.getSecurities() }.await()

    securities.map { (symbol) ->
        async(CommonPool) {
            val data = fetchIntradayData(symbol)
            if (data != null) {
                try {
                    data.writeToFile("${AppSettings.paths.intradayData}/$dateString/${exchange.code}/$symbol.json")
                } catch (e: Error) {
                    exchange.logger.error(e.message)
                }
            }
        }
    }.forEach { it.await() }
}

suspend fun Exchange.asyncFetchSummary() {
    val exchange = this
    // No matter what time and date it is locally, we are interested in what date it is in New York.
    val dateTime = DateTime().withZone(DateTimeZone.forID("America/New_York"))
    val dateString: String = dateTime.toString("yyyy-MM-dd")
    val securities = async(CommonPool) { exchange.getSecurities() }.await()

    securities.map { (symbol) ->
        async(CommonPool) {
            val data = getYahooSummary(symbol)
            if (data != null) {
                try {
                    val path = "${AppSettings.paths.summary}/$dateString/${exchange.code}/$symbol.json"
                    data.toPrettyJson().writeToFile(path)
                } catch (e: Error) {
                    exchange.logger.error(e.message)
                }
            }
        }
    }.forEach { it.await() }
}

// Creates a map of ticker symbols to company names for all exchanges.
fun createTickerToNameJson() {
    async(CommonPool) {
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
        var list: List<Security>? = null

        val params = mapOf(
                "letter" to "0",
                "render" to "download",
                "exchange" to exchange
        )
        if (exchange in exchanges) {
            val result = httpGet(baseUrl, params)

            when (result) {
                is GetSuccess -> {
                    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(StringReader(result.data))

                    list = records.map { it -> Security(
                            it.get("Symbol"),
                            it.get("Name"),
                            it.get("Sector"),
                            it.get("industry")
                    ) }

                }
                is GetError -> {
                    logger.error(result.message)
                }
            }
        }

        return list
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
                val file = File("${AppSettings.paths.dailyData}/$exchange/$symbol.csv")
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
                async(CommonPool) {
                    val data = YahooCompanyNews(symbol)
                            .fetch()
                            .json()

                    val file = File("${AppSettings.paths.news}/$date/$exchange/$symbol.json")
                    file.parentFile.mkdirs()
                    file.writeText(data)
                }
            }?.forEach { it.await() }
        }

        jobs.forEach { it.await() }
    }

}