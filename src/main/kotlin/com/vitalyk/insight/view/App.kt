package com.vitalyk.insight.view

import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.style.Styles
import javafx.stage.Stage
import tornadofx.*

class InsightApp : App(SymbolTableView::class, Styles::class) {
    override fun start(stage: Stage) {
        super.start(stage)

        stage.setOnCloseRequest {
            // OkHttp uses two thread pools that keep threads alive for 60 seconds after use.
            // The app will keep running unless the executor service is shut down
            // and connection pool is cleared.
            // See: https://square.github.io/okhttp/3.x/okhttp/okhttp3/OkHttpClient.html
            HttpClients.main.dispatcher().executorService().shutdown()
            HttpClients.main.connectionPool().evictAll()
        }
    }
}

// Application.launch(InsightApp::class.java, *args)