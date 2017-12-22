package com.vitalyk.insight.main

import com.vitalyk.insight.main.fetchDailyData
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

    Application.launch(InsightApp::class.java, *args)

//    println(fetchDailyData("NVDA"))

}