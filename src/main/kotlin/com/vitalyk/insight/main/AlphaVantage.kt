package com.vitalyk.insight.main

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import okhttp3.HttpUrl
import java.net.URI

data class AlphaVantageQuote(
    val symbol: String,
    val price: Float,
    val volume: Int,
    val timestamp: String
)

object AlphaVantageApi {
    private val baseUrl = "https://www.alphavantage.co/query"

    fun getQuotes(tickers: Array<String>): String {
//        val httpUrl = HttpUrl.parseyahooBaseUrl) ?: throw Error("Bad URL.")
//        val urlBuilder = httpUrl.newBuilder()
//        urlBuilder.addQueryParameter(param, value)
        return "lol"
    }

//    fun getQuotesAsync(tickers: Array<String>): String = async {
//        return getQuotes(tickers)
//    }
}