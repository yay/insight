package com.vitalyk.insight.yahoo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
//        val story: String = ""
)

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

// RSS 2.0
var rssPubDateParser = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
// Atom (ISO 8601)
var atomDateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz")

abstract class YahooNews : News() {
    override val baseUrl = "http://finance.yahoo.com"
}

class YahooIpoNews : YahooNews() {
    override var url = "$baseUrl/news/category-ipos/?bypass=true"

    override fun read(doc: Document) {
        val listItems = doc.select(".yom-top-story .yom-list li")

        for (listItem in listItems) {
            val a = listItem.select("a")
//            val cite = a.next().text()
            var href = a.attr("href")
            // If news item comes from Yahoo Finance itself, the base url
            // will be missing.
            if (href.indexOf("http") != 0) {
                href = baseUrl + href
            }
            val newsItem = NewsItem(
                headline = a.text(),
                url = href
            )
            items.add(newsItem)
        }
    }
}

class YahooCompanyNews(symbol: String) : YahooNews() {
    override var url = "$baseUrl/rss/headline?s=$symbol"

    override fun read(doc: Document) {
        val domItems = doc.select("item")
        for (domItem in domItems) {
            val title = domItem.select("title")
            val link = domItem.select("link")
            val pubDate = domItem.select("pubDate")
            val newsItem = NewsItem(
                headline = title.text(),
                url = link.text()
//                    date = rssPubDateParser.parse(pubDate.text())
//                    date = LocalDateTime.ofInstant(rssPubDateParser.parse(pubDate.text()).toInstant(), ZoneId.systemDefault())
            )
            items.add(newsItem)
        }
    }
}