package com.vitalyk.insight.main

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vitalyk.insight.iex.IexApi
import com.vitalyk.insight.ui.getFxBeanDefinition
import com.vitalyk.insight.view.InsightApp
import io.socket.client.IO
import io.socket.client.Socket.EVENT_CONNECT
import io.socket.client.Socket.EVENT_DISCONNECT
import javafx.application.Application
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties

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

//    val indent = " ".repeat(4)
//    val klass = IexApi.DayChartPoint::class
//    val kclassProps = klass.declaredMemberProperties
//    val beanConstructor = kclassProps.map { prop ->
//        "$indent ${prop.name}: ${prop.returnType.toString().split(".")[1]}"
//    }
//    val beanProps = kclassProps.map { prop ->
//        "$indent var ${prop.name}: ${prop.returnType.toString().split(".")[1]}\n" +
//        "$indent fun ${prop.name}Property() = getProperty(${klass.simpleName}::${prop.name})\n"
//    }
//    val result = "open class ${klass.simpleName}(\n" +
//        beanConstructor.joinToString("\n") + "\n) {\n" +
//        beanProps.joinToString("\n") + "}"
//
//    println(result)

//    println(dataClassToTableView(IexApi.DayChartPoint::class))

//    println(makeBeanMaker(IexApi.DayChartPoint::class))
}

fun getTops() {
    val socket = IO.socket("https://ws-api.iextrading.com/1.0/tops")

    socket
        .on(EVENT_CONNECT) {
            socket.emit("subscribe", "firehose") // all symbols
//            socket.emit("subscribe", "snap,fb,aig+")
//            socket.emit("unsubscribe", "aig+")
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