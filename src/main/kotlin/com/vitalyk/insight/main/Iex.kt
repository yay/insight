package com.vitalyk.insight.main

import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

// The IEX API is currently open and does not require authentication to access its data.
// https://iextrading.com/developer/docs/

object IexApi1 {
    private val client = HttpClients.main
    private const val baseUrl = "https://api.iextrading.com/1.0"

    data class FastQuote(
        val symbol: String,
        val price: Double,
        val size: Int,
        val time: Long
    )

    enum class Info(val value: String) {
        Quote("quote"),
        News("news"),
        Chart("chart")
    }

    enum class Range(val value: String) {
        Y5("5y"),
        Y2("2y"),
        Y("1y"),
        YTD("ytd"),
        M6("6m"),
        M3("3m"),
        M("1m"),
        D("1d"),
//        Date("date"),
//        Auto("dynamic")
    }

    val allInfo = Info.values().toSet()

    private fun getStringResponse(requestUrl: String): String? {
        val request = Request.Builder()
            .url(requestUrl)
            .build()

        val response = client.newCall(request).execute()

        return getResponseString(response)
    }

    private fun getResponseString(response: Response): String? {
        response.use {
            return if (it.isSuccessful) {
                try {
                    it.body()?.string()
                } catch (e: IOException) { // string() can throw
                    e.printStackTrace()
                    getAppLogger().error("Request failed: ${e.message}")
                    null
                }
            } else {
                getAppLogger().error("Request failed: $it.")
                null
            }
        }
    }

    fun getChart(symbol: String, range: Range = Range.Y): String? {
        val url = "$baseUrl/stock/$symbol/chart/${range.value}"
        val httpUrl = HttpUrl.parse(url) ?: throw Error("Bad URL.")
        val requestUrl = httpUrl.newBuilder().build().toString()

        return getStringResponse(requestUrl)
    }

    // For example: getDayChart("AAPL", "20180131")
    fun getDayChart(symbol: String, date: String? = null): String? {
        val url = "$baseUrl/stock/$symbol/chart/date/$date"
        val httpUrl = HttpUrl.parse(url) ?: throw Error("Bad URL.")
        val requestUrl = httpUrl.newBuilder().build().toString()

        return getStringResponse(requestUrl)
    }

    fun getQuote(symbol: String, range: Range = Range.M, types: Set<Info> = allInfo): String? {
        val httpUrl = HttpUrl.parse("$baseUrl/stock/$symbol/batch") ?: throw Error("Bad URL.")
        val requestUrl = httpUrl.newBuilder().apply {
            addQueryParameter("types", types.joinToString(",") { it.value })
            addQueryParameter("range", range.value)
            addQueryParameter("last", "10")
        }.build().toString()

        return getStringResponse(requestUrl)
    }

    fun getQuotes(symbols: List<String>, range: Range = Range.M, types: Set<Info> = allInfo): String? {
        if (symbols.size > 100) {
            throw IllegalArgumentException("Up to 100 symbols allowed.")
        }
        val httpUrl = HttpUrl.parse("$baseUrl/stock/market/batch") ?: throw Error("Bad URL.")

        val requestUrl = httpUrl.newBuilder().apply {
            addQueryParameter("symbols", symbols.joinToString(","))
            addQueryParameter("types", types.joinToString(",") { it.value })
            addQueryParameter("range", range.value) // used to specify a chart range if 'chart' is used in 'types' parameter
            addQueryParameter("last", "5")
        }.build().toString()

        return getStringResponse(requestUrl)
    }

    // Last provides trade data for executions on IEX.
    // It is a near real time, intraday API that provides IEX last sale price, size and time.
    // If no symbols specified, will return all symbols (8K+).
    fun getLast(symbols: List<String> = emptyList()): List<FastQuote> {
        val httpUrl = HttpUrl.parse("$baseUrl/tops/last") ?: throw Error("Bad URL.")

        val requestUrl = httpUrl.newBuilder().apply {
            if (symbols.isNotEmpty()) {
                addQueryParameter("symbols", symbols.joinToString(","))
            }
        }.build().toString()


        return getStringResponse(requestUrl)?.toJsonNode()?.map {
            FastQuote(
                it.get("symbol").asText(),
                it.get("price").asDouble(),
                it.get("size").asInt(),
                it.get("time").asLong()
            )
        } ?: emptyList()
    }

    fun getLast(symbol: String): FastQuote? {
        val httpUrl = HttpUrl.parse("$baseUrl/tops/last") ?: throw Error("Bad URL.")
        val requestUrl = httpUrl.newBuilder().apply {
            addQueryParameter("symbols", symbol)
        }.build().toString()

        return getStringResponse(requestUrl)?.toJsonNode()?.map {
            FastQuote(
                it.get("symbol").asText(),
                it.get("price").asDouble(),
                it.get("size").asInt(),
                it.get("time").asLong()
            )
        }?.first()
    }

    fun getDeep(symbol: String) {
        // https://iextrading.com/developer/docs/#deep
    }

    fun getBook(symbol: String) {
        // https://iextrading.com/developer/docs/#book51
    }

    fun getTrades(symbol: String) {
        // https://iextrading.com/developer/docs/#trades
    }

    fun getDayStats(): String? {
        // https://iextrading.com/developer/docs/#intraday
        val httpUrl = HttpUrl.parse("$baseUrl/stats/intraday") ?: throw Error("Bad URL.")
        val requestUrl = httpUrl.newBuilder().build().toString()
        return getStringResponse(requestUrl)
    }

}