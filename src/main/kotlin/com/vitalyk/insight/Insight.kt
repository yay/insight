package com.vitalyk.insight

import com.vitalyk.insight.fragment.AssetProfileFragment
import com.vitalyk.insight.iex.IexSymbols
import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.main.AppSettings
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.Settings
import com.vitalyk.insight.style.Styles
import com.vitalyk.insight.view.SymbolTableView
import com.vitalyk.insight.yahoo.AssetProfile
import com.vitalyk.insight.yahoo.getAssetProfile
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

    val clipboardMonitor = object {
        var fragment: AssetProfileFragment? = null
        val clipboard = Clipboard.getSystemClipboard()

        val symbolProperty = SimpleStringProperty().apply {
            addListener { _, _, symbol ->
                if (symbol in IexSymbols) {
                    runAsync {
                        getAssetProfile(symbol)
                    } ui {
                        it?.let {
                            show(symbol, it)
                        }
                    }
                }
            }
        }

        init {
            launch {
                while (isActive) {
                    delay(500)
                    runLater {
                        symbolProperty.value = clipboard.string
                    }
                }
            }
        }

        fun show(symbol: String, profile: AssetProfile) {
            fragment = fragment ?: tornadofx.find(AssetProfileFragment::class)
            fragment?.apply {
                openWindow()
                titleProperty.value = symbol
                this.profile.value = profile
            }
        }
    }
}

class Insight : App(SymbolTableView::class, Styles::class) {

    override fun start(stage: Stage) {
//        Thread.setDefaultUncaughtExceptionHandler { _, e ->
//            System.err.println(e.message)
//        }

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
            IexSymbols.update()
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

            launch(Insight::class.java, *args)
        }
    }
}

fun parse(str: String) {
    // object field condition value repeat
    // object must be a recognized object
    // field must be recognized field, etc.
//    parse("ANET price increases to 50.25 playSound() sendEmail("vitalyx@gmail.com") once")
//    parse("INTC bid gains 20%")
    // if (ANET.price >= 50.25) then

    // if (symbols["ANET"].price >= 50.25) {
    //     playSound("adsfsadf")
    //     sendEmail("asdfasdf")
    //     removeAlert()
    // }
}