package main

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.csv.CSVFormat
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
        val founded: Int = 0,
        val url: String = "",
        val getSecurities: Exchange.() -> List<Security> = Exchange::noSecurities
)

private val exchanges = listOf(
        Exchange("nasdaq", "NASDAQ", "New York City", 1971, "http://www.nasdaq.com/",
                Exchange::getExchangeSecuritiesFromNasdaq
        ),
        Exchange("nyse", "New York Security Exchange", "New York City", 1817, "https://www.nyse.com/index",
                Exchange::getExchangeSecuritiesFromNasdaq
        ),
        Exchange("amex", "NYSE MKT", "New York City", 1908, "https://www.nyse.com/markets/nyse-mkt",
                Exchange::getExchangeSecuritiesFromNasdaq
        )

)

// http://stackoverflow.com/questions/32935470/how-to-convert-list-to-map-in-kotlin
val exchangeMap = exchanges.map { it.code to it }.toMap()


fun fetchIntradayDataUsa() {
    val time = measureTimeMillis {
        runBlocking {
            exchangeMap["nasdaq"]?.asyncFetchIntradayData()
            exchangeMap["nyse"]?.asyncFetchIntradayData()
            exchangeMap["amex"]?.asyncFetchIntradayData()
        }
    }
    println("Fetching intraday data completed in $time ms.")
}

fun fetchSummaryUsa() {
    val time = measureTimeMillis {
        runBlocking {
            exchangeMap["nasdaq"]?.asyncFetchSummary()
            exchangeMap["nyse"]?.asyncFetchSummary()
            exchangeMap["amex"]?.asyncFetchSummary()
        }
    }
    println("Fetching stock summaries completed in $time ms.")
}


// In the future we may find a better way to fetch the list of securities
// (from exchanges directly?), where each exchange has it unique fetching logic.
// For now, Nasdaq has a list of securities traded on Nasdaq, NYSE and AMEX, so we use that.
fun Exchange.getExchangeSecuritiesFromNasdaq(): List<Security> {

    // http://www.nasdaq.com/screening/company-list.aspx
    // NASDAQ companies:
    // http://www.nasdaq.com/screening/companies-by-name.aspx?letter=0&exchange=nasdaq&render=download
    // NYSE companies:
    // http://www.nasdaq.com/screening/companies-by-name.aspx?letter=0&exchange=nyse&render=download
    // AMEX companies:
    // http://www.nasdaq.com/screening/companies-by-name.aspx?letter=0&exchange=amex&render=download

    val result = httpGet(StockFetcherUS.baseUrl, mapOf(
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
            println(result.message)
        }
    }

    return emptyList<Security>()
}

suspend fun Exchange.asyncFetchIntradayData() {
    // Note: the data is most complete when fetched a few hours (3 hours or so) after the close.
    val exchange = this
    val date: String = LocalDate.now().toString()
    val securities = async(CommonPool) { exchange.getSecurities() }.await()

    securities.map { (symbol) ->
        async(CommonPool) {
            val data = fetchIntradayData(symbol)
            if (data.isNotBlank()) {
                val file = File("${AppSettings.paths.intradayData}/$date/${exchange.code}/$symbol.json")
                file.parentFile.mkdirs()
                file.writeText(data)
            }
        }
    }.forEach { it.await() }
}

suspend fun Exchange.asyncFetchSummary() {
    val exchange = this
    val date: String = LocalDate.now().toString()
    val securities = async(CommonPool) { exchange.getSecurities() }.await()

    securities.map { (symbol) ->
        async(CommonPool) {
            val data = getYahooSummary(symbol).toJsonString()
            val file = File("${AppSettings.paths.summary}/$date/${exchange.code}/$symbol.json")
            file.parentFile.mkdirs()
            file.writeText(data)
        }
    }.forEach { it.await() }
}

object StockFetcherUS {
    val baseUrl = "http://www.nasdaq.com/screening/companies-by-name.aspx"

    val exchanges = listOf("nasdaq", "nyse", "amex")

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
                    println(result.message)
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
                val file = File("${AppSettings.paths.data}/$exchange/$symbol.csv")
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