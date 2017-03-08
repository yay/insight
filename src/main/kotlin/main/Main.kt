package main

// https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md
// https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md
// https://github.com/Kotlin/kotlinx.coroutines

import kotlinx.coroutines.experimental.runBlocking
import style.Styles
import tornadofx.App
import tornadofx.importStylesheet
import view.SymbolTableView
import kotlin.system.measureTimeMillis


class InsightApp : App(SymbolTableView::class) {

    init {
        importStylesheet(Styles::class)
    }

}

fun main(args: Array<String>) {
//    val db = DBI("jdbc:postgresql://localhost:5432/postgres")
//    val runner = MigrationRunner(db)

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

//    val asyncSummaryTime = measureTimeMillis {
//        StockFetcherUS.asyncFetchSummary()
//    }
//    println("Asynchronous summary fetching completed in $asyncSummaryTime ms.")

//    val asyncIntradayTime = measureTimeMillis {
//        StockFetcherUS.asyncFetchIntraday()
//    }
//    println("Asynchronous intraday data fetching completed in $asyncIntradayTime ms.")


//    runBlocking {
//        exchangeMap["nasdaq"]?.asyncFetchIntradayData()
//        exchangeMap["nyse"]?.asyncFetchIntradayData()
//        exchangeMap["amex"]?.asyncFetchIntradayData()
//    }

//    fetchIntradayDataUsa()
    fetchSummaryUsa()


//    val syncTime = measureTimeMillis {
//        StockFetcherUS.fetchSummary()
//    }
//    // Synchronous version is about 7 times slower.
//    println("Synchronous version completed in $syncTime ms.")

//    val syncTime = measureTimeMillis {
//        StockFetcherUS.fetchIntraday()
//    }
//    // Synchronous version is about 7 times slower.
//    println("Synchronous version completed in $syncTime ms.")
}

fun fake_main(args: Array<String>) {
//    Settings.load(AppSettings)
//    Settings.saveOnShutdown()
//    Application.launch(InsightApp::class.java, *args)


//    StockFetcherUS.fetchData()

//    val syncTime = measureTimeMillis {
//        StockFetcherUS.fetchSummary()
//    }
//    // Synchronous version is about 7 times slower.
//    println("Synchronous version completed in $syncTime ms.")





//    val asyncTime = measureTimeMillis {
//        StockFetcherUS.asyncFetchSummary()
//    }
//    println("Asynchronous version completed in $asyncTime ms.")

//    val asyncNewsTime = measureTimeMillis {
//        StockFetcherUS.asyncFetchNews()
//    }
//    println("Asynchronous version completed in $asyncNewsTime ms.")


//    YahooCompanyNews("AVGO").fetch().print()




//    YahooCompanyNews("AVGO").fetch().print()
//    YahooCompanyNews("NVDA").fetch().print()
//    YahooCompanyNews("MSFT").fetch().print()

//    main.YahooSummary("NVDA").execute().parse().print()
//    val list = main.YahooData("NVDA").execute().parse().ohlc()
//    main.saveToDb("NVDA", list)

//    val mapper = jacksonObjectMapper()
//    for (li in list) {
//        //println(mapper.writeValueAsString(li))
//        println(li)
//    }

//    main.connectToDb()
//    main.loadFromDb("NVDA")
}