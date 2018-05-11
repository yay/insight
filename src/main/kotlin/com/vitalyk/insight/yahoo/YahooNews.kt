package com.vitalyk.insight.yahoo

import com.vitalyk.insight.main.appLogger
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NewsItem(
    val headline: String,
    val url: String,
    val date: Date = Date()
)

// API info: https://developer.yahoo.com/finance/company.html
private const val companyNewsUrl = "http://finance.yahoo.com/rss/headline?s="
private val newsDateParser = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")

fun fetchNews(symbol: String): MutableList<NewsItem> {
    // YFinance uses dashes for delimiters: RDS-B
    // IEX uses dots: RDS.B
    // Schwab and StockCharts use slashes: RDS/B
    @Suppress("NAME_SHADOWING")
    val symbol = symbol.replace(".", "-")
    val list = mutableListOf<NewsItem>()
    val url = companyNewsUrl + symbol
    val connection = Jsoup.connect(url).timeout(10000)

    val document = try { connection.get() } catch (e: IOException) {
        appLogger.error("Fetching news for $symbol failed: ${e.message}")
        null
    }
    val code = connection.data().response().statusCode()

    if (document != null && code == 200) {
        val items = document.select("item")
        for (item in items) {
            val title = item.select("title")
            val link = item.select("link")
            val pubDate = item.select("pubDate")
            val newsItem = NewsItem(
                headline = Parser.unescapeEntities(title.text(), true),
                url = link.text(),
                date = newsDateParser.parse(pubDate.text())
            )
            newsItem.date.toString()
            list.add(newsItem)
        }
    } else {
        appLogger.warn("$symbol request status code: $code\nURL: $url")
    }

    return list
}

object YahooNews {

    var updateInterval = 1 * 60 * 10_000

    // map of symbols to keywords we are listening too

    // show alert with the news and the keywords triggered

    // predefined set of keywords to choose from

    // keywords common to all symbols



    fun watch(symbol: String, keywords: Set<String>) {

    }

    init {
    }
}