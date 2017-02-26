package main

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

typealias WatchlistName = String
typealias Watchlist = MutableList<String>

object Settings {

    val settingsFileName = "settings.json"

    private val mapper by lazy { jacksonObjectMapper() }
    private var isSaveOnShutdown = false

    fun load() {
        mapper.readValue<AppSettings>(File(settingsFileName))
    }

    fun save() {
        mapper.writerWithDefaultPrettyPrinter().writeValue(File(settingsFileName), AppSettings)
    }

    fun saveOnShutdown() {
        if (!isSaveOnShutdown) {
            Runtime.getRuntime().addShutdownHook(Thread {
                mapper.writerWithDefaultPrettyPrinter().writeValue(File(settingsFileName), AppSettings)
            })

            isSaveOnShutdown = true
        }
    }
}

//val AppSettings = mutableMapOf<Any, Any>(
//        "watchlists" to mutableMapOf<WatchlistName, Watchlist>()
//)

object AppSettings {
    var watchlists = mutableMapOf<WatchlistName, Watchlist>()

    object paths {
        var storage = "../insight_storage"
        var data = "$storage/stock_data"
        var summary = "$storage/stock_summary"
        var news = "$storage/stockNews"
    }
}