package main

import javafx.application.Application
import style.Styles
import tornadofx.App
import tornadofx.importStylesheet
import view.SymbolTableView


class InsightApp : App(SymbolTableView::class) {

    init {
        importStylesheet(Styles::class)
    }

}

fun main(args: Array<String>) {
    Settings.load()
    Settings.saveOnShutdown()
    Application.launch(InsightApp::class.java, *args)

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