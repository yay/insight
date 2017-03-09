package main

// https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md
// https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md
// https://github.com/Kotlin/kotlinx.coroutines

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

//    fetchIntradayDataUsa()
    fetchSummaryUsa()
}