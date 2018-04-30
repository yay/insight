package com.vitalyk.insight.yahoo

import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.UserAgents
import okhttp3.HttpUrl
import okhttp3.Request
import java.io.IOException

fun yahooGet(url: String, params: List<Pair<String, String>>? = null): String {
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
        throw IOException(e.message + "\nURL: $requestUrl")
    }

    response.use {
        return if (it.isSuccessful) {
            val body = it.body()
            if (body != null) {
                // body.string() seems to never return null,
                // but can return an empty string or throw IOException
                body.string()
            } else {
                throw IOException("Empty response body (${it.code()}).\nURL: $requestUrl")
            }
        } else {
            throw IOException("Request failed (${it.code()}). Message: ${it.message()}\nURL: $requestUrl")
        }
    }
}