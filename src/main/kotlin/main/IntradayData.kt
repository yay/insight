package main

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.SocketTimeoutException

// https://chartapi.finance.yahoo.com/instrument/1.0/GOOG/chartdata;type=quote;range=1d/json

class IntradayData(val symbol: String, val client: OkHttpClient = HttpClients.main) {
    private val baseUrl: String = "https://chartapi.finance.yahoo.com/instrument/1.0/$symbol/chartdata;type=quote;range=1d/json"
    private val urlBuilder = HttpUrl.parse(baseUrl).newBuilder()

    private var data: String = ""

    fun execute(): IntradayData {
        val url = urlBuilder.build()

        println(url)

        val request = Request.Builder().url(url).build()

        try {
            val response = client.newCall(request).execute()

            response.use {
                if (it.isSuccessful) {
                    data = it.body().string()
                } else {
                    println( "$symbol request. Unexpected code: " + it )
                }
            }
        } catch (e: IOException) {
            println( "$symbol request error: " + e )
        }

        return this
    }

    fun data(): String { return data }
}