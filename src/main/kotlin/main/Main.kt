package main

// https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md
// https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md
// https://github.com/Kotlin/kotlinx.coroutines

import org.apache.commons.csv.CSVFormat
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.skife.jdbi.v2.DBI
import style.Styles
import tornadofx.App
import tornadofx.importStylesheet
import view.SymbolTableView
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat

class InsightApp : App(SymbolTableView::class) {

    init {
        importStylesheet(Styles::class)
    }

}

fun dailyQuotes_fromDiskToDb(db: DBI) {

    // TODO: https://www.google.nl/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#newwindow=1&q=ERROR:+numeric+field+overflow&*

    val exchangeToMarketMap = mapOf(
            "nasdaq" to "XNAS",
            "nyse" to "XNYS",
            "amex" to "XASE"
    )
    val basePath = "${AppSettings.paths.storage}/data/24-02-2017/"

//    transaction {  // will create a table "dailyquotes"
//        create(DailyQuotes)
//    }

    fun isGoodDecimal(d: BigDecimal): Boolean = d.precision() <= 14 && d.scale() <= 6

    transaction {

        // IMPORTANT:
        // https://www.postgresql.org/docs/9.1/static/populate.html

        for ((exchange, _market) in exchangeToMarketMap) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val path = basePath + exchange
            val walker = File(path).walk().maxDepth(1)

            for (file in walker) {
                val _symbol = file.nameWithoutExtension.trim()
                if (_symbol != exchange) {
//                println("$value:${file.nameWithoutExtension.trim()}")
                    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(file.reader())

                    records.forEach {
                        val rec = it
                        val date = DateTime(dateFormat.parse(rec.get(YahooDataColumns.date)))
                        val _open = BigDecimal(rec.get(YahooDataColumns.open))
                        val _high = BigDecimal(rec.get(YahooDataColumns.high))
                        val _low = BigDecimal(rec.get(YahooDataColumns.low))
                        val _close = BigDecimal(rec.get(YahooDataColumns.close))
                        val _adjClose = BigDecimal(rec.get(YahooDataColumns.adjClose))
                        val _volume = rec.get(YahooDataColumns.volume).toLong()

                        if (isGoodDecimal(_adjClose) &&
                            isGoodDecimal(_open) &&
                            isGoodDecimal(_high) &&
                            isGoodDecimal(_low) &&
                            isGoodDecimal(_close) &&
                            _symbol.length <= 6 &&
                            _market.length <= 4) {

                            try {
                                DailyQuotes.insert {
                                    it[quoteDate] = date
                                    it[symbol] = _symbol
                                    it[market] = _market
                                    it[open] = _open
                                    it[high] = _high
                                    it[low] = _low
                                    it[close] = _close
                                    it[adjClose] = _adjClose
                                    it[volume] = _volume
                                }
                            } catch (e: Exception) {
                                println("Exception on ($date, $_symbol, $_market): $e")
                            }

                        }
                    }
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    val db = DBI("jdbc:postgresql://localhost:5432/insight")
    val runner = MigrationRunner(db)

    dailyQuotes_fromDiskToDb(db)


//    println(BigDecimal("234.345678"))

//    println(GoogleIntradayData("AAPL").execute().data())

//    async(CommonPool) {
//        var map = mutableMapOf<String, MutableMap<String, String>>()
//        StockFetcherUS.forAll { exchange, companies ->
//            val symbolNames = mutableMapOf<String, String>()
//            companies.forEach { symbolNames[it.symbol] = it.name }
//            map[exchange] = symbolNames
//        }
//        Settings.save(map, "exchanges.json")
//    }

//    Settings.load(AppSettings)
//    Settings.saveOnShutdown(AppSettings)
//    Application.launch(InsightApp::class.java, *args)

//    fetchIntradayDataUsa()
//    fetchSummaryUsa()

}