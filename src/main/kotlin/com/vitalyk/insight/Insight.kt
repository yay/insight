package com.vitalyk.insight

import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.style.Styles
import com.vitalyk.insight.view.SymbolTableView
import io.socket.client.Socket
import javafx.stage.Stage
import tornadofx.*
import okhttp3.OkHttpClient
import java.util.logging.Level
import java.util.logging.Logger


class Insight : App(SymbolTableView::class, Styles::class) {

    override fun start(stage: Stage) {
//        Thread.setDefaultUncaughtExceptionHandler { _, e ->
//            System.err.println(e.message)
//        }

        super.start(stage)

        stage.setOnCloseRequest {
            // OkHttp uses two thread pools that keep threads alive for 60 seconds after use.
            // The app will keep running unless the executor service is shut down
            // and connection pool is cleared.
            // See: https://square.github.io/okhttp/3.x/okhttp/okhttp3/OkHttpClient.html
            // and PlatformImpl.exit() docs.
            HttpClients.main.dispatcher().executorService().shutdown()
            HttpClients.main.connectionPool().evictAll()

            Watchlist.clearAll()
        }
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
            Logger.getLogger(Socket::class.java.name).level = Level.FINE

            launch(Insight::class.java, *args)
        }
    }
}