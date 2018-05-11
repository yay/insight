package com.vitalyk.insight.main

import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.reuters.ReutersWire

object AppSettings {
    var watchlists = listOf<Watchlist.Settings>()
    var reutersWire: ReutersWire.State? = null

    object Paths {
        var storage = "./storage"
        var dailyData = "$storage/stock_daily"
        var intradayData = "$storage/stock_intraday"
        var summary = "$storage/stock_summary"
        var news = "$storage/stock_news"
    }
}