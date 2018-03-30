package com.vitalyk.insight.yahoo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vitalyk.insight.main.getAppLogger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger

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

    val document = connection.get()
    val code = connection.data().response().statusCode()

    if (code == 200) {
        val items = document.select("item")
        for (item in items) {
            val title = item.select("title")
            val link = item.select("link")
            val pubDate = item.select("pubDate")
            val newsItem = NewsItem(
                headline = title.text(),
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

abstract class News {
    val items = mutableListOf<NewsItem>()

    val timeout: Int = 30000
    val log: Logger by lazy { Logger.getLogger(this::class.java.name) }

    abstract val baseUrl: String
    abstract var url: String

    protected val mapper by lazy { jacksonObjectMapper() }

    fun fetch(): News {
        val connection = Jsoup.connect(url).timeout(timeout)
//        connection.followRedirects(false)
        try {
            val doc = connection.get()
//            val code = connection.response().statusCode()
            read(doc)
        } catch (e: IOException) {
            log.warning { "News request failed: $e\nURL: $url" }
        }

        return this
    }

    fun json() = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(items)

    fun print() {
        print(json())
    }

    abstract fun read(doc: Document)
}