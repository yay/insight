package com.vitalyk.insight.main

import jdk.incubator.http.HttpClient
import jdk.incubator.http.HttpRequest
import jdk.incubator.http.HttpResponse
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.net.URI

data class AlphaVantageQuote(
    val symbol: String,
    val price: Float,
    val volume: Int,
    val timestamp: String
)

object AlphaVantage {
    private val client = HttpClient.newHttpClient()
    private val baseUri = URI("https://www.alphavantage.co/query")

    fun getQuotes(tickers: Array<String>): String {
        val request = HttpRequest.newBuilder().uri(URI("https://www.google.com/")).GET().build()
        val response = client.send(request, HttpResponse.BodyHandler.asString())
        return response.body()
    }

//    fun getQuotesAsync(tickers: Array<String>): String = async {
//        return getQuotes(tickers)
//    }
}