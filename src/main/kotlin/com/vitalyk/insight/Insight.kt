package com.vitalyk.insight

import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.style.Styles
import com.vitalyk.insight.view.SymbolTableView
import javafx.stage.Stage
import tornadofx.*

class Insight : App(SymbolTableView::class, Styles::class) {
    override fun start(stage: Stage) {
        super.start(stage)

        stage.setOnCloseRequest {
            // OkHttp uses two thread pools that keep threads alive for 60 seconds after use.
            // The app will keep running unless the executor service is shut down
            // and connection pool is cleared.
            // See: https://square.github.io/okhttp/3.x/okhttp/okhttp3/OkHttpClient.html
            // and PlatformImpl.exit() docs.
            HttpClients.main.dispatcher().executorService().shutdown()
            HttpClients.main.connectionPool().evictAll()
        }
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(Insight::class.java, *args)
        }
    }
}