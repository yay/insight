package main

fun main(args: Array<String>) {

    Settings.load(AppSettings)
    Settings.saveOnShutdown(AppSettings)

    // The app won't exit while the scheduler is running.
    appScheduler.start()

    setupEndOfDayFetcher()

//    val db = DBI("jdbc:postgresql://localhost:5432/insight")

//    async(CommonPool) {
//        var map = mutableMapOf<String, MutableMap<String, String>>()
//        StockFetcherUS.forAll { exchange, companies ->
//            val symbolNames = mutableMapOf<String, String>()
//            companies.forEach { symbolNames[it.symbol] = it.name }
//            map[exchange] = symbolNames
//        }
//        Settings.save(map, "exchanges.json")
//    }

    // http://mailman.qos.ch/pipermail/logback-user/2007-June/000247.html
//    MarkerFactory.getMarker("flush_mail")

//    val ySteps = Nd4j.linspace(-100_000, 100_100, 101)

//    println(ySteps)

}