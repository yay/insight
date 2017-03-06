package main

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.logging.Logger

// Java 9:
// New Logging API
// Money API
// Process API
// HTTP2 and WebSocket (currently use OkHttp)
// JSON API - currently use Jackson for JSON and XML (works very well)


class YahooSummary(val symbol: String, val client: OkHttpClient = HttpClients.main) {

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
//    private val urlParams = mapOf(
//            "formatted" to "true",
//            "corsDomain" to "finance.yahoo.com"
//    )

    private var data: String = ""
    private var tree: JsonNode? = null
    private val mapper by lazy { ObjectMapper() }
    private val log by lazy { Logger.getLogger(this::class.java.name) }

    fun execute(): YahooSummary {
//        for ((key, value) in urlParams) {
//            urlBuilder.addQueryParameter(key, value)
//        }
        val urlBuilder = HttpUrl.parse(baseUrl).newBuilder()
                .addEncodedQueryParameter(modulesParam, modules.joinToString(moduleSeparator))

        val url = urlBuilder.build()

        log.info { "Sending summary request for $symbol:\n$url" }

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

//        if (response.isSuccessful) {
////            try {
//                data = response.body().string()
////            } catch (e: IOException) {
////            }
//        } else {
////                throw IOException("Unexpected code: " + response)
//            log.warning { "Unexpected code: " + response }
//        }
//        response.close()

//        if (response.isSuccessful) {
//            data = response.body().string()
//        } else {
//            log.warning { "Unexpected code: " + response }
//        }

        response.use {
            if (it.isSuccessful) {
                data = it.body().string()
            } else {
//                throw IOException( "Unexpected code: " + it )
                log.warning { "$symbol request. Unexpected code: " + it }
            }
        }

        return this
    }

    fun parse(): YahooSummary {
        if (data.isBlank()) {
            log.warning { "No data to parse for $symbol." }
        } else {
            try {
                tree = mapper.readTree(data)
            } catch (e: JsonProcessingException) {
                log.info { "Parsing exception: $e\n$data" }
            }
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