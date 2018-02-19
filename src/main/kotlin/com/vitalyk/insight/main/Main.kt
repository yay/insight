package com.vitalyk.insight.main

import com.vitalyk.insight.view.InsightApp
import javafx.application.Application
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

fun main(args: Array<String>) = runBlocking {

//    Settings.load(AppSettings)
//    Settings.saveOnShutdown(AppSettings)
//
//    // The app won't exit while the scheduler is running.
//    val appSchedulerFactory = StdSchedulerFactory()
//    val appScheduler = appSchedulerFactory.scheduler
//
//    appScheduler.start()
//
//    scheduleEndOfDayFetcher(appScheduler)


//    Application.launch(InsightApp::class.java, *args)
//    println(getYFinanceAuth())
//    println(fetchDailyData("GOOG"))
//    println(AlphaVantageApi.getQuotes(emptyArray()))

//    val str = IexApi1.getQuotes(arrayOf("AAPL", "AMZN"))
//    if (str != null) {
//        val json
//    }
//    println(str?.toJsonNode()?.get("AAPL"))
//    println(str?.toJsonNode()?.at("/APPL/quote")?.get("companyName"))

//    println(IexApi1.getLast().find { it.symbol == "AAPL" })
//    println(IexApi1.getLast("MSFT"))
//    println(IexApi1.getDayStats())
//    println(IexApi1.getQuotes(arrayOf("AAPL", "AMZN"))?.toPrettyJson())
//    val ran = 5.0..10.0
//    println(ran.start)
//    println(ran.endInclusive)
//    val scale = LinearScale().apply {
//        domain = 5.0 to 10.0
//        range = 100.0 to 200.0
//    }
    // date = "",
//    open = 0.0f,
//    high = 1.0f,
//    low = -1.0f,
//    close = 0.5f,
//    volume = 100,
//    unadjustedVolume = 99,
//    change = 0.25f,
//    changePercent = 25.0f,
//    vwap = 0.1f,
//    label = "hey",
//    changeOverTime = 5

//    val now = DateTime().withZone(DateTimeZone.forID("America/New_York"))
//    val then = now.minusYears(1)
//
//    val etDateTime = ZonedDateTime.now(ZoneId.of("America/New_York"))
//    val then2 = etDateTime.minusYears(1)
//
//    val m1 = then.millis
//    val m2 = then2.toInstant().toEpochMilli()
////    println(m1)
////    println(m2)
////    println(m2 - m1)
//
//    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
//    val parsed = dateFormat.parse("2018-01-31")
//    println(parsed)
//    println(LocalDate.parse("2018-01-31"))
//    println(LocalDateTime.parse("2018-01-31", DateTimeFormatter.ISO_DATE))
//    println(LocalDateTime.parse("1996-12-26"))
//    println(etDateTime.toString())
//    println(ZonedDateTime.now(ZoneId.of("America/New_York")).toString())
//    println(then.toString())
//    println(then2.toString())

//    println(IexApi1.getChart("AAPL").joinToString("\n"))

//    val list = listOf("AAPL", "MSFT", "AVGO", "C", "BAC", "MU", "NVDA")
//    println(IexApi1.getLast(list).joinToString("\n"))

//    println(IexApi1.getDayStats())

//    println(IexApi1.getQuote("AAPL"))
//    println(IexApi1.getQuote("AAPL", types = IexApi1.allTypes))
//    println(IexApi1.getCompany("AAPL"))
//    println(IexApi1.getDividends("AAPL"))
//    println(IexApi1.getEarnings("AAPL"))
//    println(IexApi1.getSpread("AAPL").joinToString("\n"))
//    println(IexApi1.getFinancials("SQ"))
//    println(IexApi1.getStats("SQ"))
//    println(IexApi1.getMostActive().joinToString("\n"))
//    println(IexApi1.getLogoData("MSFT"))
//    println(IexApi1.getPeers("MSFT"))
//    println(IexApi1.getSplits("AAPL"))
//    println(IexApi1.getVolumeByVenue("AAPL").joinToString("\n"))
    println(IexApi1.getSymbols().joinToString("\n"))

//
//    list.map {
//        async {
//            IexApi1.getFinancials("SQ").toString().writeToFile("./$it.txt")
//        }
//    }.forEach { it.join() }
//    println(IexApi1.getDayChart("AAPL", "20180131").joinToString("\n"))
}
