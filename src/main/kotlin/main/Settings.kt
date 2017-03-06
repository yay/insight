package main

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

typealias WatchlistName = String
typealias Watchlist = MutableList<String>

object Settings {

    private val mapper by lazy { jacksonObjectMapper() }

    private fun getFileName(obj: Any): String = "${obj::class.java.name.split(".").last()}.json"

    fun load(obj: Any) {
        mapper.readValue(File(getFileName(obj)), obj::class.java)
    }

    fun save(obj: Any) {
        mapper.writerWithDefaultPrettyPrinter().writeValue(File(getFileName(obj)), obj)
    }

    private var saveOnShutdownMap = mutableMapOf<Any, Boolean>()

    fun saveOnShutdown(obj: Any) {
        if (saveOnShutdownMap[obj] != true) {
            Runtime.getRuntime().addShutdownHook(Thread {
                mapper.writerWithDefaultPrettyPrinter().writeValue(File(getFileName(obj)), obj)
            })
            saveOnShutdownMap[obj] = true
        } else {
            println("${getFileName(obj)} is already set to save on shutdown.")
        }
    }
}

object AppSettings {
    var watchlists = mutableMapOf<WatchlistName, Watchlist>()

    object paths {
        var storage = "../insight_storage"
        var data = "$storage/stock_data"
        var summary = "$storage/stock_summary"
        var news = "$storage/stock_news"
    }
}