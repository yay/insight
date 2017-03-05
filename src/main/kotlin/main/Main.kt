package main

// https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md
// https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md
// https://github.com/Kotlin/kotlinx.coroutines

import javafx.application.Application
import org.skife.jdbi.v2.DBI
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

    val asyncTime = measureTimeMillis {
        USCompanies.asyncFetchSummary()
    }
    println("Asynchronous version completed in $asyncTime ms.")
}

fun fake_main(args: Array<String>) {
//    Settings.load()
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