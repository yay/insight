package main

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.csv.CSVFormat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.StringReader
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

/**
 * Fetches last day's intraday data for major exchanges.
 */
fun fetchIntradayData() {
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

suspend fun Exchange.asyncFetchIntradayData() {
    // Note: the data is most complete when fetched a few hours (3 hours or so) after the close.
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