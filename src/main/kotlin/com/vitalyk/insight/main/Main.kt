package com.vitalyk.insight.main

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.socket.client.IO
import io.socket.client.Socket.EVENT_CONNECT
import io.socket.client.Socket.EVENT_DISCONNECT


fun main(args: Array<String>) {

//    getTops()
    getDeep()

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
}

fun getTops() {
    val socket = IO.socket("https://ws-api.iextrading.com/1.0/tops")

    socket
        .on(EVENT_CONNECT, {
            socket.emit("subscribe", "firehose") // all symbols
//            socket.emit("subscribe", "snap,fb,aig+")
//            socket.emit("unsubscribe", "aig+")
            //        socket.disconnect()
        })
        .on("message", { params ->
            println(IexApi1.parseTops(params.first() as String))
        })
        .on(EVENT_DISCONNECT, { println("Disconnected.") })

    socket.connect()
}

fun getDeep() {
    val socket = IO.socket("https://ws-api.iextrading.com/1.0/deep")

    val mapper = jacksonObjectMapper()
    val value = mapper.writeValueAsString(object {
        val symbols = listOf("anet")
        val channels = listOf("deep")
    })

    socket
        .on(EVENT_CONNECT, {
            socket.emit("subscribe", value)
        })
        .on("message", { params ->
            println(params.first() as String)
        })
        .on(EVENT_DISCONNECT, { println("Disconnected.") })

    socket.connect()
}