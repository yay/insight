package main

typealias WatchlistName = String
typealias Watchlist = MutableList<String>

object AppSettings {
    var watchlists = mutableMapOf<WatchlistName, Watchlist>()

    var defaultLogger = "insight"

    object paths {
        var storage = "../insight_storage"
        var dailyData = "$storage/stock_daily"
        var intradayData = "$storage/stock_intraday"
        var summary = "$storage/stock_summary"
        var news = "$storage/stock_news"
    }
}