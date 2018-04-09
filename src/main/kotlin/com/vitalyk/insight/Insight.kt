package com.vitalyk.insight

import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.main.AppSettings
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.Settings
import com.vitalyk.insight.style.Styles
import com.vitalyk.insight.view.SymbolTableView
import io.socket.client.IO
import io.socket.engineio.client.Socket
import javafx.stage.Stage
import okhttp3.OkHttpClient
import tornadofx.*
import java.util.logging.Level
import java.util.logging.Logger


class Insight : App(SymbolTableView::class, Styles::class) {

    override fun start(stage: Stage) {
//        Thread.setDefaultUncaughtExceptionHandler { _, e ->
//            System.err.println(e.message)
//        }

        IO.setDefaultOkHttpWebSocketFactory(HttpClients.main)

        Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
        Logger.getLogger(Socket::class.java.name).level = Level.FINE

        super.start(stage)

        stage.setOnCloseRequest {
            HttpClients.killAll()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Settings.load(AppSettings) {
                Watchlist.restore(watchlists)
            }
            Settings.saveOnShutdown(AppSettings) {
                watchlists = Watchlist.save()
                Watchlist.clearAll()
            }
            //
            //    // The app won't exit while the scheduler is running.
            //    val appSchedulerFactory = StdSchedulerFactory()
            //    val appScheduler = appSchedulerFactory.scheduler
            //
            //    appScheduler.start()
            //
            //    scheduleEndOfDayFetcher(appScheduler)

            launch(Insight::class.java, *args)
        }
    }
}

fun parse(str: String) {
    // object field condition value repeat
    // object must be a recognized object
    // field must be recognized field, etc.
//    parse("ANET price increases to 50.25 playSound() sendEmail('vitalyx@gmail.com') once")
//    parse("INTC bid gains 20%")
    // if (ANET.price >= 50.25) then

    // if (symbols["ANET"].price >= 50.25) {
    //     playSound("adsfsadf")
    //     sendEmail("asdfasdf")
    //     removeAlert()
    // }
}