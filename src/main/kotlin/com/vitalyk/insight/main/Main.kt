package com.vitalyk.insight.main

import com.vitalyk.insight.main.fetchDailyData
import com.vitalyk.insight.view.InsightApp
import javafx.application.Application
import okhttp3.HttpUrl
import okhttp3.Request
import java.io.IOException

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
    println(fetchDailyData("GOOG"))

}