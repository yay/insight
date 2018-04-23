package com.vitalyk.insight.yahoo

import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.UserAgents
import okhttp3.HttpUrl
import okhttp3.Request
import java.io.IOException

sealed class YahooGetResult
data class YahooGetSuccess(val value: String) : YahooGetResult()
data class YahooGetFailure(val url: String, val code: Int, val message: String) : YahooGetResult()

fun yahooGet(url: String, params: List<Pair<String, String>>? = null): YahooGetResult {
    val httpUrl = HttpUrl.parse(url) ?: throw Error("Bad URL.")
    val urlBuilder = httpUrl.newBuilder()
    if (params != null) {
        for ((param, value) in params) {
            urlBuilder.addQueryParameter(param, value)
        }
    }
    val requestUrl: String = urlBuilder.build().toString()

    val request = Request.Builder()
        .addHeader("User-Agent", UserAgents.chrome)
        .url(requestUrl)
        .build()
    val response = HttpClients.yahoo.newCall(request).execute()

    response.use {
        return if (it.isSuccessful) {
            try {
                val body = it.body()
                if (body != null) {
                    // body.string() seems to never return null,
                    // but can return an empty string
                    YahooGetSuccess(body.string())
                } else {
                    YahooGetFailure(requestUrl, it.code(), "Empty response body.")
                }
            } catch (e: IOException) {
                YahooGetFailure(requestUrl, it.code(), e.message ?: "")
            }
        } else {
            YahooGetFailure(requestUrl, it.code(), it.message())
        }
    }
}