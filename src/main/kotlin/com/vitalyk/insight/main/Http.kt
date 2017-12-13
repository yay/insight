package com.vitalyk.insight.main

import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

object HttpClients {
    val main: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10L, TimeUnit.SECONDS)
        .readTimeout(30L, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val authorized = original.newBuilder()
                .addHeader("Cookie", "B=abhu36lchrts9&b=3&s=l1")
                .build()

            // http://stackoverflow.com/questions/44030983/yahoo-finance-url-not-working

            chain.proceed(authorized)
        }
        .build()
}

sealed class GetResult
data class GetSuccess(val data: String) : GetResult()
data class GetError(val url: String, val code: Int, val message: String) : GetResult()

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

private val chromeUserAgent =
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/57.0.2987.133 Safari/537.36"

fun httpGet(url: String, params: Map<String, String> = emptyMap()): GetResult {
    val httpUrl = HttpUrl.parse(url) ?: throw Error("Invalid HttpUrl.")

    val urlBuilder = httpUrl.newBuilder()

    for ((param, value) in params) {
        urlBuilder.addQueryParameter(param, value)
    }

    val requestUrl = urlBuilder.build().toString()
    val request = Request.Builder()
        .addHeader("User-Agent", chromeUserAgent)
        .url(requestUrl)
        .build()
    val response = HttpClients.main.newCall(request).execute()

    response.use {
        return if (it.isSuccessful) {
            try {
                val body = it.body()

                if (body == null) {
                    GetError(requestUrl, it.code(), "Empty response body.")
                } else {
                    GetSuccess(body.string())
                }
            } catch (e: IOException) {
                GetError(requestUrl, it.code(), e.message ?: "")
            }
        } else {
            GetError(requestUrl, it.code(), it.message())
        }
    }
}