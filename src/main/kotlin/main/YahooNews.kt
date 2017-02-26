package main

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.time.LocalDate
import java.util.logging.Logger

class NewsItem(val headline: String, val url: String) {
    var date: LocalDate = LocalDate.now()
    var content: String = ""
    var cite: String = ""
}

abstract class News {
    val items = mutableListOf<NewsItem>()

    val timeout: Int = 10000
    val log: Logger by lazy { Logger.getLogger(this::class.java.name) }

    abstract val baseUrl: String
    abstract var url: String

    protected lateinit var connection: Connection
    protected val mapper by lazy { jacksonObjectMapper() }

    fun fetch(): News {
        connection = Jsoup.connect(url).timeout(timeout)
//        connection.followRedirects(false)
        val doc = connection.get()
        val code = connection.response().statusCode()

        if (code == 200) {
            read(doc)
        } else {
            log.warning { "main.News request status code: $code\nURL: $url" }
        }

        return this
    }

    fun print() {
        print(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(items))
    }

    abstract fun read(doc: Document)
}

abstract class YahooNews : News() {
    override val baseUrl = "http://finance.yahoo.com"
}

class YahooIpoNews : YahooNews() {
    override var url = "$baseUrl/news/category-ipos/?bypass=true"

    override fun read(doc: Document) {
        val listItems = doc.select(".yom-top-story .yom-list li")

        for (listItem in listItems) {
            val a = listItem.select("a")
            val cite = a.next().text()
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
            )
            newsItem.cite = pubDate.text()
            items.add(newsItem)
        }
    }
}