package com.vitalyk.insight

import com.vitalyk.insight.fragment.AssetProfileFragment
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.IexSymbols
import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.main.AppSettings
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.Settings
import com.vitalyk.insight.style.Styles
import com.vitalyk.insight.view.SymbolTableView
import io.socket.client.IO
import io.socket.engineio.client.Socket
import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.Clipboard
import javafx.stage.Stage
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import tornadofx.*
import java.util.logging.Level
import java.util.logging.Logger

fun clipboardHook() {
    // These two approaches below don't work for some reason.
    // So we are just polling for clipboard changes every 500ms.
//    object : ClipboardAssistance(com.sun.glass.ui.Clipboard.SYSTEM) {
//        override fun contentChanged() {
//            val clipboard = Clipboard.getSystemClipboard()
//            if (clipboard.hasString()) {
//                println(clipboard.string)
//            }
//        }
//    }
//
//    Toolkit.getDefaultToolkit().systemClipboard.addFlavorListener {
//        println("${it.source} $it")
//    }

    object {
        val clipboard = Clipboard.getSystemClipboard()

        val symbolProperty = SimpleStringProperty().apply {
            addListener { _, _, symbol ->
                AssetProfileFragment.show(symbol)
            }
        }

        init {
            launch {
                while (isActive) {
                    delay(500)
                    runLater {
                        val string = clipboard.string
                        if (string in IexSymbols)
                            symbolProperty.value = string
                    }
                }
            }
        }
    }
}

class Insight : App(SymbolTableView::class, Styles::class) {

    override fun start(stage: Stage) {
//        Thread.setDefaultUncaughtExceptionHandler { _, e ->
//            System.err.println(e.message)
//        }

        Iex.setOkHttpClient(HttpClients.main)

        Settings.load(AppSettings) {
            Watchlist.restore(watchlists)
        }
        // Note: the shutdown hook won"t execute until the OkHttp threads are shut down.
        Settings.saveOnShutdown(AppSettings) {
            println("Saving settings...")
            AppSettings.watchlists = Watchlist.save()
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
        clipboardHook()

//        println(getYahooSummary("AAPL")?.toPrettyJson())
//        println(getAssetProfile("AAPL"))

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
            launch(Insight::class.java, *args)
        }
    }
}