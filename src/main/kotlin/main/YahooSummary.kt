package main

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.map.ObjectMapper
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

// Java 9:
// New Logging API
// Money API
// Process API
// HTTP2 and WebSocket (currently use OkHttp)
// JSON API - currently use Jackson for JSON and XML (works very well)


class YahooSummary(val symbol: String) {

    /*

    Example URL:

    https://query2.finance.yahoo.com/v10/finance/quoteSummary/AVGO
        ?formatted=true
        &crumb=X8WUMkCCDSk
        &lang=en-US
        &region=US
        &modules=defaultKeyStatistics%2CfinancialData%2CcalendarEvents
        &corsDomain=finance.yahoo.com

    */

    private val version: Int = 10

    private val modules = arrayOf(
            "defaultKeyStatistics",
            "financialData",
            "calendarEvents"
    )
    private val modulesParam = "modules"
    private val moduleSeparator: String = "%2C"

    private val baseUrl: String = "https://query2.finance.yahoo.com/v$version/finance/quoteSummary/$symbol"
    private val urlParams = mapOf(
            "formatted" to "true",
            "corsDomain" to "finance.yahoo.com"
    )
    private val urlBuilder = HttpUrl.parse(baseUrl).newBuilder()

    val connectTimeout: Long = 10
    val readTimeout: Long = 30

    private val client by lazy {
        OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build()
    }
    private lateinit var request: Request
    private lateinit var response: Response

    private var data: String = ""
    private var tree: JsonNode? = null
    private val mapper by lazy { ObjectMapper() }
    private val log by lazy { Logger.getLogger(this::class.java.name) }

    fun execute(): YahooSummary {
//        for ((key, value) in urlParams) {
//            urlBuilder.addQueryParameter(key, value)
//        }
        urlBuilder.addEncodedQueryParameter(modulesParam, modules.joinToString(moduleSeparator))

        val url = urlBuilder.build().toString()

        log.info { "Sending summary request for $symbol:\n$url" }

        request = Request.Builder().url(url).build()
        response = client.newCall(request).execute()

        val code = response.code()
        val body = response.body().string()

        if (code == 200) {
            data = body
        } else {
            val tree = mapper.readTree(body)
            val error = tree.get("quoteSummary")?.get("error")

            if (error != null) {
                log.warning { "Request error: ${error["code"]}\n${error["description"]}" }
            }
        }

        return this
    }

    fun parse(): YahooSummary {
        if (data.isBlank()) {
//            throw Exception("No data to parse.")
            log.warning { "No data to parse for $symbol." }
        } else {
            tree = mapper.readTree(data)
        }

        return this
    }

    fun tree(): JsonNode? { return tree }

    fun data(): String { return data }

    fun prettyData(): String {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.tree())
    }

    fun print(): YahooSummary {
        print(prettyData())
        return this
    }

    fun call(f: YahooSummary.() -> Unit): YahooSummary {
        f()
        return this
    }

}