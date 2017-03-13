package main

// https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md
// https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md
// https://github.com/Kotlin/kotlinx.coroutines

import org.apache.commons.csv.CSVFormat
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
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

fun dailyQuotes_fromDiskToDb(db: Database) {

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
//                    val _date = dateFormat.parse(it.get(YahooDataColumns.date)).toInstant().toEpochMilli()
                    val _date = DateTime(dateFormat.parse(it.get(YahooDataColumns.date)))
                    val _open = BigDecimal(it.get(YahooDataColumns.open))
                    val _high = BigDecimal(it.get(YahooDataColumns.high))
                    val _low = BigDecimal(it.get(YahooDataColumns.low))
                    val _close = BigDecimal(it.get(YahooDataColumns.close))
                    val _adjClose = BigDecimal(it.get(YahooDataColumns.adjClose))
                    val _volume = it.get(YahooDataColumns.volume).toLong()

                    transaction {
                        DailyQuotes.insert {
                            it[quoteDate] = _date
                            it[symbol] = _symbol
                            it[market] = _market
                            it[open] = _open
                            it[high] = _high
                            it[low] = _low
                            it[close] = _close
                            it[adjClose] = _adjClose
                            it[volume] = _volume
                        }
                    }
                }
                break
            }
        }
        break
    }
}

fun main(args: Array<String>) {
    val db = Database.connect("jdbc:postgresql://localhost:5432/insight",
            driver = "org.postgresql.Driver", user = "vitalykravchenko")

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