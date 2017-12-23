package com.vitalyk.insight.main

import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

val httpStatusCodes = mapOf(
    200 to "OK",
    400 to "Bad Request",
    401 to "Unauthorized",
    403 to "Forbidden",
    404 to "Not Found",
    500 to "Internal Server Error",
    502 to "Bad Gateway",
    503 to "Service Unavailable",
    504 to "Gateway Timeout"
)

object UserAgents {
    val chrome =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/57.0.2987.133 Safari/537.36"
}

object HttpClients {
    val main: OkHttpClient = OkHttpClient.Builder()
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

sealed class HttpGetResult
data class HttpGetSuccess(val data: String) : HttpGetResult()
data class HttpGetError(val url: String, val code: Int, val message: String) : HttpGetResult()

fun httpGet(url: String, params: Map<String, String> = emptyMap()): HttpGetResult {
    val httpUrl = HttpUrl.parse(url) ?: throw Error("Invalid HttpUrl.")
    val urlBuilder = httpUrl.newBuilder()

    for ((param, value) in params) {
        urlBuilder.addQueryParameter(param, value)
    }

    val requestUrl = urlBuilder.build().toString()
    val request = Request.Builder()
        .addHeader("User-Agent", UserAgents.chrome)
        .url(requestUrl)
        .build()
    val response = HttpClients.main.newCall(request).execute()

    response.use {
        return if (it.isSuccessful) {
            try {
                val body = it.body()

                if (body == null) {
                    HttpGetError(requestUrl, it.code(), "Empty response body.")
                } else {
                    HttpGetSuccess(body.string())
                }
            } catch (e: IOException) {
                HttpGetError(requestUrl, it.code(), e.message ?: "")
            }
        } else {
            HttpGetError(requestUrl, it.code(), it.message())
        }
    }
}