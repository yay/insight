package com.vitalyk.insight

import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.main.HttpClients
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
            Watchlist.clearAll()
        }
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(Insight::class.java, *args)
        }
    }
}