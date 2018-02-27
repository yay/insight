package com.vitalyk.insight.main

import io.socket.client.IO


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


//    val list = listOf("AAPL", "MSFT", "AVGO", "C", "BAC", "MU", "NVDA")
//    list.map {
//        async {
////            IexApi1.getFinancials("SQ").toString().writeToFile("./$it.txt")
//            println(IexApi1.getFinancials("SQ").toString())
//        }
//    }.forEach { it.join() }
//    println(IexApi1.getTops("ANET", "NVDA").joinToString("\n"))
//    println(IexApi1.getBatch(listOf("ANET", "NVDA"), setOf(IexApi1.BatchType.QUOTE)).joinToString("\n"))

    val socket = IO.socket("https://ws-api.iextrading.com/1.0/tops")

    socket
        .on("connect", {
            socket.emit("subscribe", "snap,fb,aig+")
            socket.emit("unsubscribe", "aig+")
    //        socket.disconnect()
        })
        .on("message", { params -> println(params[0] as String) })
        .on("disconnect", { println("Disconnected.") })

    socket.connect()
}
