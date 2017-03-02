package main

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object HttpClients {
    val main = OkHttpClient.Builder()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .build()
}

fun httpGet(url: String, params: Map<String, String>): String? {
    var data: String? = null

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
                data = it.body().string()
            } catch (e: IOException) {
                println("111")
            }
        } else {
            throw IOException( "Unexpected code: " + it )
        }
    }

    return data
}