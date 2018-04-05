package com.vitalyk.insight.yahoo

import com.vitalyk.insight.main.getAppLogger
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

// https://developer.yahoo.com/finance/company.html
private const val yahooBaseUrl = "http://finance.yahoo.com"
private const val companyNewsUrl = "$yahooBaseUrl/rss/headline?s="
private val newsDateParser = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")

fun fetchNews(symbol: String): MutableList<NewsItem> {
    val list = mutableListOf<NewsItem>()
    val url = companyNewsUrl + symbol
    val connection = Jsoup.connect(url).timeout(10000)

    val document = try { connection.get() } catch (e: IOException) {
        getAppLogger().error("Fetching news for $symbol failed: ${e.message}")
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
        getAppLogger().warn("$symbol request status code: $code\nURL: $url")
    }

    return list
}