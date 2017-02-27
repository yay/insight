package main

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

fun httpGet(url: String, params: Map<String, String>): String? {
    var data: String? = null

    val urlBuilder = HttpUrl.parse(url).newBuilder()

    for ((param, value) in params) {
        urlBuilder.addQueryParameter(param, value)
    }

    val requestUrl = urlBuilder.build().toString()
    val client = OkHttpClient()
    val request = Request.Builder().url(requestUrl).build()
    val response = client.newCall(request).execute()
    val code = response.code()

    if (code == 200) {
        data = response.body().string()
    }

    response.close()

    return data
}