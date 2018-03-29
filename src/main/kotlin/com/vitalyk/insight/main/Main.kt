package com.vitalyk.insight.main

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vitalyk.insight.iex.IexApi
import com.vitalyk.insight.view.InsightApp
import com.vitalyk.insight.yahoo.ChartInterval
import com.vitalyk.insight.yahoo.ChartPoint
import com.vitalyk.insight.iex.IexApi.Tops as Tops
import com.vitalyk.insight.yahoo.getChartPoints
import com.vitalyk.insight.yahoo.getDistributionDays
import io.socket.client.IO
import io.socket.client.Socket.EVENT_CONNECT
import io.socket.client.Socket.EVENT_DISCONNECT
import javafx.application.Application
import java.time.temporal.ChronoUnit

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
//    getChartPoints("^RUT", 5, ChronoUnit.WEEKS)?.forEach { println(it) }

//    val distributionDays = mutableListOf<Int>()
//    getChartPoints("^RUT", 5, ChronoUnit.WEEKS)?.reduceIndexed { index, prev, curr ->
////        println("$index${Pair(prev, curr)}")
//        if (isDistributionDay(prev, curr)) {
//            distributionDays.add(index)
//        }
//        curr
//    }
//    println("${distributionDays.size} distribution days")

//    println(toBeanMaker(IexApi.Tops::class))
//    getTops()
//    getLast()

//    val list = observableList<String>()
//    list.onChange { change ->
//        change.next()
//        println(change.removed)
//        println(change.addedSubList)
//        println(change.list)
//    }
////    list.add("FB")
////    list.addAll("GOOG", "BAC")
////    list.addAll("GOOG")
//    val text = "BAC, GOOG,   FB, C, MSFT"

//    map["FB"] = Tops(symbol = "FB")
//
//    text.split(",").forEach {
//        map[it] = Tops(symbol = it)
//    }

//    map.addListener(MapChangeListener { change ->
//        println(change.valueAdded)
//        println(change.valueRemoved)
//    })
//
//    map.keys.add("ANET")
//    map.keys.toList()
//
//    println(map.values)

//    val wl = Watchlist()
////
//    wl.add("BAC", "C")
//    wl.add("MSFT")
//    wl.add("MSFT", "ANET")

//    launch(JavaFx) {
//        Thread.sleep(10000)
//        wl.remove("BAC", "C")
//        println("removed BAC C")
//    }
//    launch(JavaFx) {
//        Thread.sleep(20000)
//        wl.remove("MSFT", "ANET")
//        println("removed MSFT ANET")
//    }

//    async {
//        Thread.sleep(7000)
//        println("!!! ${wl["BAC"]}")
//    }
}

fun getTops() {
    val socket = IO.socket("https://ws-api.iextrading.com/1.0/tops")

    val symbolList = mutableListOf("orcl", "fb")
//    val topsList = observableList<Tops>()

    socket
        .on(EVENT_CONNECT) {
//            socket.emit("subscribe", "firehose") // all symbols
            socket.emit("subscribe", symbolList.joinToString(","))
//            socket.emit("unsubscribe", "fb")
            //        socket.disconnect()
        }
        .on("message") { params ->
            println(IexApi.parseTops(params.first() as String))
        }
        .on(EVENT_DISCONNECT) {
            println("Disconnected.")
        }

    socket.connect()
}

fun getLast() {
    val socket = IO.socket("https://ws-api.iextrading.com/1.0/last")

    socket
        .on(EVENT_CONNECT) {
            socket.emit("subscribe", "orcl,fb")
        }
        .on("message") { params ->
            println(params.first() as String)
        }

    socket.connect()
}

fun getDepth() {
    val socket = IO.socket("https://ws-api.iextrading.com/1.0/deep")

    val mapper = jacksonObjectMapper()
    val value = mapper.writeValueAsString(object {
        val symbols = listOf("ORCL")
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