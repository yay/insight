package com.vitalyk.insight.main

import com.vitalyk.insight.view.InsightApp
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
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


//    val list = listOf("AAPL", "MSFT", "AVGO", "C", "BAC", "MU", "NVDA")
//    list.map {
//        async {
////            IexApi1.getFinancials("SQ").toString().writeToFile("./$it.txt")
//            println(IexApi1.getFinancials("SQ").toString())
//        }
//    }.forEach { it.join() }
    println(IexApi1.getSymbols().size)
}
