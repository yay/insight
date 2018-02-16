package com.vitalyk.insight.main

import com.vitalyk.insight.view.InsightApp
import javafx.application.Application

fun main(args: Array<String>) {

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
    println(AlphaVantage.getQuotes(emptyArray()))

//    val ran = 5.0..10.0
//    println(ran.start)
//    println(ran.endInclusive)
//    val scale = LinearScale().apply {
//        domain = 5.0 to 10.0
//        range = 100.0 to 200.0
//    }
}
