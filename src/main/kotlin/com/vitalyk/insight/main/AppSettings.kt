package com.vitalyk.insight.main

import com.vitalyk.insight.iex.Watchlist

object AppSettings {
    var watchlists = listOf<Watchlist.Settings>()

    object paths {
        var storage = "./storage"
        var dailyData = "$storage/stock_daily"
        var intradayData = "$storage/stock_intraday"
        var summary = "$storage/stock_summary"
        var news = "$storage/stock_news"
    }
}