package com.vitalyk.insight.main

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vitalyk.insight.iex.IexApi
import com.vitalyk.insight.view.InsightApp
import com.vitalyk.insight.iex.IexApi.Tops as Tops
import io.socket.client.IO
import io.socket.client.Socket.EVENT_CONNECT
import io.socket.client.Socket.EVENT_DISCONNECT
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