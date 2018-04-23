package com.vitalyk.insight.yahoo

import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.UserAgents
import com.vitalyk.insight.main.getAppLogger
import okhttp3.HttpUrl
import okhttp3.Request
import java.io.IOException

sealed class YahooGetResult
data class YahooGetSuccess(val data: String) : YahooGetResult()
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

fun yahooFetch(url: String, params: List<Pair<String, String>>? = null): String? {
    val httpUrl = HttpUrl.parse(url) ?: throw Error("Bad URL.")
    val urlBuilder = httpUrl.newBuilder()
    if (params != null) {
        for ((param, value) in params) {
            urlBuilder.addQueryParameter(param, value)
        }
    }
    val request = Request.Builder()
        .addHeader("User-Agent", UserAgents.chrome)
        .url(urlBuilder.build().toString())
        .build()
    val response = HttpClients.yahoo.newCall(request).execute()

    response.use {
        return if (it.isSuccessful) {
            try {
                it.body()?.string()
            } catch (e: IOException) {
                getAppLogger().error("${e.message}, $it")
                null
            }
        } else {
            getAppLogger().error(it.toString())
            null
        }
    }
}