package com.vitalyk.insight

import com.teamdev.jxbrowser.chromium.Browser
import com.teamdev.jxbrowser.chromium.BrowserCore
import com.teamdev.jxbrowser.chromium.BrowserType
import com.teamdev.jxbrowser.chromium.internal.Environment
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.style.Styles
import com.vitalyk.insight.view.SymbolTableView
import javafx.stage.Stage
import tornadofx.*

class Insight : App(SymbolTableView::class, Styles::class) {

    override fun init() {
        // On Mac OS X Chromium engine must be initialized in non-UI thread.
        if (Environment.isMac()) {
            BrowserCore.initialize()
        }
    }

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

            if (Environment.isWindows()) {
                Thread {
                    browser.dispose()
                }.start()
            } else {
                browser.dispose()
            }
            BrowserCore.shutdown()
        }
    }

    companion object {
        val browser by lazy {
            Browser(BrowserType.HEAVYWEIGHT)
        }

        @JvmStatic
        fun main(vararg args: String) {
            launch(Insight::class.java, *args)
        }
    }
}