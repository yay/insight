package main

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

fun HttpGet(url: String): String? {
    var data: String? = null

    val urlBuilder = HttpUrl.parse(url).newBuilder()
    val requestUrl = urlBuilder.build().toString()
    val client = OkHttpClient()
    val request = Request.Builder().url(requestUrl).build()
    val response = client.newCall(request).execute()
    val code = response.code()

    if (code == 200) {
        data = response.body().string()
    }

    return data
}