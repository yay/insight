package main

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.csv.CSVFormat
import java.io.File
import java.io.StringReader
import java.time.LocalDate

// http://www.nasdaq.com/screening/company-list.aspx

// NASDAQ companies:
// http://www.nasdaq.com/screening/companies-by-name.aspx?letter=0&exchange=nasdaq&render=download

// NYSE companies:
// http://www.nasdaq.com/screening/companies-by-name.aspx?letter=0&exchange=nyse&render=download

// AMEX companies:
// http://www.nasdaq.com/screening/companies-by-name.aspx?letter=0&exchange=amex&render=download

data class Company(
        val symbol: String,
        val name: String,
        val sector: String = "",
        val industry: String = "",
        val cap: String = "",
        val ipo: String = ""
)

object USCompanies {
    val baseUrl = "http://www.nasdaq.com/screening/companies-by-name.aspx"

    val exchanges = listOf("nasdaq", "nyse", "amex")

    fun getCompanies(exchange: String): List<Company>? {
        var list: List<Company>? = null

        val params = mapOf(
                "letter" to "0",
                "render" to "download",
                "exchange" to exchange
        )
        if (exchange in exchanges) {
            val data = httpGet(baseUrl, params)

            if (data != null) {
                val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(StringReader(data))

                list = records.map { it -> Company(
                        it.get("Symbol"),
                        it.get("Name"),
                        it.get("Sector"),
                        it.get("industry"),
                        it.get("MarketCap"),
                        it.get("IPOyear")
                ) }

//                println(data)
            }
        }

        return list
    }

    fun forAll(f: (exchange: String, companies: List<Company>) -> Unit) {
        for (exchange in exchanges) {
            val companies = getCompanies(exchange)
            if (companies != null) {
                f(exchange, companies)
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

    fun fetchSummary() {
        val date: String = LocalDate.now().toString()
        forAll { exchange, companies ->
            for ((symbol) in companies) {
                val file = File("${AppSettings.paths.summary}/$date/$exchange/$symbol.json")
                file.parentFile.mkdirs()
                val data = YahooSummary(symbol, HttpClients.main)
                        .execute()
                        .parse()
                        .prettyData()

                file.writeText(data)
            }
        }
    }

    fun asyncFetchSummary() = runBlocking {
        val date: String = LocalDate.now().toString()

        for (exchange in exchanges) {
            val companies = getCompanies(exchange)

            if (companies != null) {
                for ((symbol) in companies) {
                    val job = async(CommonPool) {
                        val file = File("${AppSettings.paths.summary}/$date/$exchange/$symbol.json")
                        file.parentFile.mkdirs()
                        val data = YahooSummary(symbol, HttpClients.main)
                                .execute()
                                .parse()
                                .prettyData()

                        file.writeText(data)
                    }
                    job.await()
                }
            }
        }
    }
}