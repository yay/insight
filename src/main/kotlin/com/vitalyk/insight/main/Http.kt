package com.vitalyk.insight.main

import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

object UserAgents {
    val chrome =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/57.0.2987.133 Safari/537.36"
}

object HttpClients {
    // OkHttp performs best when you create a single OkHttpClient instance and reuse it for
    // all of your HTTP calls.
    val main: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10L, TimeUnit.SECONDS)
        // If the server fails to send a byte <timeout> seconds after the last byte,
        // a read timeout error will be raised.
        .readTimeout(30L, TimeUnit.SECONDS)
        .build()

    val yahoo: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10L, TimeUnit.SECONDS)
        .readTimeout(30L, TimeUnit.SECONDS)
        // Yahoo Finance now requires a cookie to fetch historical and other data.
        // addInterceptor takes an Interceptor, which is an interface with a single
        // method that takes a Chain and returns a Response,
        // so we can just pass the implementation of that method.
        .addInterceptor { chain ->
            val original = chain.request()
            val authorized = original.newBuilder()
                .addHeader("Cookie", "B=avmvnm5d3qlf9&b=3&s=l4")
                .build()

            // http://stackoverflow.com/questions/44030983/yahoo-finance-url-not-working

            chain.proceed(authorized)
        }
        .build()
}

class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    // E.g. client.networkInterceptors().add(UserAgentInterceptor("user-agent-string"))
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .removeHeader(USER_AGENT_HEADER_NAME)
            .addHeader(USER_AGENT_HEADER_NAME, userAgent)
            .build()
        return chain.proceed(requestWithUserAgent)
    }

    companion object {
        private val USER_AGENT_HEADER_NAME = "User-Agent"
    }
}

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