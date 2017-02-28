package main

import kotlinx.coroutines.experimental.*
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
//    Settings.load()
//    Settings.saveOnShutdown()
//    Application.launch(InsightApp::class.java, *args)


//    USCompanies.fetchData()

//    val syncTime = measureTimeMillis {
//        USCompanies.fetchSummary()
//    }
//    println("Synchronous version completed in $syncTime ms.")





//    val asyncTime = measureTimeMillis {
//        USCompanies.asyncFetchSummary()
//    }
//    println("Asynchronous version completed in $asyncTime ms.")






//    main.YahooCompanyNews("AVGO").fetch().print()
//    main.YahooCompanyNews("NVDA").fetch().print()
//    main.YahooCompanyNews("MSFT").fetch().print()

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