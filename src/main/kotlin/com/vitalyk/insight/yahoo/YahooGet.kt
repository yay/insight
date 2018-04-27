package com.vitalyk.insight.yahoo

import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.UserAgents
import okhttp3.HttpUrl
import okhttp3.Request
import java.io.IOException

sealed class YahooGetResult
data class YahooGetSuccess(val value: String) : YahooGetResult()
data class YahooGetFailure(val url: String, val message: String, val code: Int) : YahooGetResult()

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
    val response = try {
        HttpClients.yahoo.newCall(request).execute()
    } catch (e: IOException) {
        return YahooGetFailure(requestUrl, e.message ?: "", 0)
    }

    response.use {
        return if (it.isSuccessful) {
            try {
                val body = it.body()
                if (body != null) {
                    // body.string() seems to never return null,
                    // but can return an empty string
                    YahooGetSuccess(body.string())
                } else {
                    YahooGetFailure(requestUrl, "Empty response body.", it.code())
                }
            } catch (e: IOException) {
                YahooGetFailure(requestUrl, e.message ?: "", it.code())
            }
        } else {
            YahooGetFailure(requestUrl, it.message(), it.code())
        }
    }
}