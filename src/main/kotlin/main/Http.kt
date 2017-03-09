package main

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object HttpClients {
    val main: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .build()
}

sealed class GetResult
data class GetSuccess(val data: String) : GetResult()
data class GetError(val code: Int, val message: String) : GetResult()

fun httpGet(url: String, params: Map<String, String> = emptyMap()): GetResult {
    val urlBuilder = HttpUrl.parse(url).newBuilder()

    for ((param, value) in params) {
        urlBuilder.addQueryParameter(param, value)
    }

    val requestUrl = urlBuilder.build().toString()
    val request = Request.Builder().url(requestUrl).build()
    val response = HttpClients.main.newCall(request).execute()

    response.use {
        if (it.isSuccessful) {
            try {
                return GetSuccess(it.body().string())
            } catch (e: IOException) {
                return GetError(it.code(), e.message?: "")
            }
        } else {
            return GetError(it.code(), it.message())
        }
    }
}