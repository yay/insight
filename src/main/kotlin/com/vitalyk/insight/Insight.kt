package com.vitalyk.insight

import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.IexSymbols
import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.main.AppSettings
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.Settings
import com.vitalyk.insight.reuters.ReutersWire
import com.vitalyk.insight.style.Styles
import com.vitalyk.insight.view.MainView
import io.socket.client.IO
import io.socket.engineio.client.Socket
import javafx.scene.control.Alert
import javafx.stage.Stage
import okhttp3.OkHttpClient
import tornadofx.*
import java.util.logging.Level
import java.util.logging.Logger

class Insight : App(MainView::class, Styles::class) {

    override fun start(stage: Stage) {

        stage.minWidth = 900.0
        stage.minHeight = 600.0
//        Thread.setDefaultUncaughtExceptionHandler { _, e ->
//            System.err.println(e.message)
//        }

        Iex.setOkHttpClient(HttpClients.main)

        if (!Settings.load(AppSettings) {
            Watchlist.restore(watchlists)
            ReutersWire.loadState(reutersWire)
        }) alert(Alert.AlertType.ERROR, "Application settings failed to load (missing or corrupted).")

        // Note: the shutdown hook won"t execute until the OkHttp threads are shut down.
        Settings.saveOnShutdown(AppSettings) {
            println("Saving settings...")
            AppSettings.watchlists = Watchlist.save()
            AppSettings.reutersWire = ReutersWire.saveState()
        }

        //
        //    // The app won"t exit while the scheduler is running.
        //    val appSchedulerFactory = StdSchedulerFactory()
        //    val appScheduler = appSchedulerFactory.scheduler
        //
        //    appScheduler.start()
        //
        //    scheduleEndOfDayFetcher(appScheduler)

        IexSymbols.update()

        IO.setDefaultOkHttpWebSocketFactory(HttpClients.main)

        Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
        Logger.getLogger(Socket::class.java.name).level = Level.FINE

        super.start(stage)

        stage.setOnCloseRequest {
            Watchlist.disconnect()
            HttpClients.shutdown()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Class.forName("org.h2.Driver")
            Settings.parentDir = "/Users/vitalykravchenko/insight/"
            launch(Insight::class.java, *args)
        }
    }
}