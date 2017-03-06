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

fun main(args: Array<String>) = runBlocking {
//    val db = DBI("jdbc:postgresql://localhost:5432/postgres")
//    val runner = MigrationRunner(db)

//    println(IntradayData("AAPL").execute().data())

//    async(CommonPool) {
//        var map = mutableMapOf<String, MutableMap<String, String>>()
//        USCompanies.forAll { exchange, companies ->
//            val symbolNames = mutableMapOf<String, String>()
//            companies.forEach { symbolNames[it.symbol] = it.name }
//            map[exchange] = symbolNames
//        }
//        Settings.save(map, "exchanges.json")
//    }

//    Settings.load(AppSettings)
//    Settings.saveOnShutdown(AppSettings)
//    Application.launch(InsightApp::class.java, *args)

//    val asyncTime = measureTimeMillis {
//        USCompanies.asyncFetchSummary()
//    }
//    println("Asynchronous version completed in $asyncTime ms.")


//    val syncTime = measureTimeMillis {
//        USCompanies.fetchSummary()
//    }
//    // Synchronous version is about 7 times slower.
//    println("Synchronous version completed in $syncTime ms.")

    val syncTime = measureTimeMillis {
        USCompanies.fetchIntraday()
    }
    // Synchronous version is about 7 times slower.
    println("Synchronous version completed in $syncTime ms.")
}

fun fake_main(args: Array<String>) {
//    Settings.load(AppSettings)
//    Settings.saveOnShutdown()
//    Application.launch(InsightApp::class.java, *args)


//    USCompanies.fetchData()

//    val syncTime = measureTimeMillis {
//        USCompanies.fetchSummary()
//    }
//    // Synchronous version is about 7 times slower.
//    println("Synchronous version completed in $syncTime ms.")





//    val asyncTime = measureTimeMillis {
//        USCompanies.asyncFetchSummary()
//    }
//    println("Asynchronous version completed in $asyncTime ms.")

//    val asyncNewsTime = measureTimeMillis {
//        USCompanies.asyncFetchNews()
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