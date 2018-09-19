package com.vitalyk.insight.main

import okhttp3.*
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object UserAgents {
    val chrome =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/57.0.2987.133 Safari/537.36"
}

object HttpClients {
    private val clients = mutableListOf<OkHttpClient>()

    // OkHttp performs best when you create a single OkHttpClient instance and reuse it for
    // all of your HTTP calls.
    val main: OkHttpClient = createClient {
        connectTimeout(10L, TimeUnit.SECONDS)
        // If the server fails to send a byte <timeout> seconds after the last byte,
        // a read timeout error will be raised.
        readTimeout(30L, TimeUnit.SECONDS)
    }

    val yahoo = createClient {
        connectTimeout(10L, TimeUnit.SECONDS)
        readTimeout(30L, TimeUnit.SECONDS)
        // Yahoo Finance now requires a cookie to fetch historical and other data.
        // addInterceptor takes an Interceptor, which is an interface with a single
        // method that takes a Chain and returns a Response,
        // so we can just pass the implementation of that method.
        addInterceptor { chain ->
            val original = chain.request()
            val authorized = original.newBuilder()
                .addHeader("Cookie", "B=avmvnm5d3qlf9&b=3&s=l4")
                .build()

            // http://stackoverflow.com/questions/44030983/yahoo-finance-url-not-working

            chain.proceed(authorized)
        }
    }

    private fun createClient(block: OkHttpClient.Builder.() -> Unit): OkHttpClient {
        val builder = OkHttpClient.Builder()
        block(builder)
        val client = builder.build()
        clients.add(client)
        return client
    }

    fun shutdown() {
        clients.forEach {
            // OkHttp uses two thread pools that keep threads alive for 60 seconds after use.
            // The app will keep running unless the executor service is shut down
            // and connection pool is cleared.
            // See: https://square.github.io/okhttp/3.x/okhttp/okhttp3/OkHttpClient.html
            // and PlatformImpl.exit() docs.
            // Also: https://publicobject.com/2015/01/02/okio-watchdog/
            //       https://github.com/square/okhttp/issues/3957
            it.dispatcher().executorService().shutdown()
            it.connectionPool().evictAll()
        }
    }
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

fun httpGet(
    url: String,
    client: OkHttpClient,
    withUrl: HttpUrl.Builder.() -> Unit = {},
    withRequest: Request.Builder.() -> Unit = {}
): String? {
    val httpUrl = (HttpUrl.parse(url) ?: throw IllegalArgumentException("Bad URL: $url"))
        .newBuilder().apply(withUrl).build()

    val request = Request.Builder().url(httpUrl).apply(withRequest).build()

    val response = try {
        client.newCall(request).execute()
    } catch (e: Exception) {
        appLogger.warn("${e.message}:\n$httpUrl")
//        e.printStackTrace()
        when (e) {
            is SocketTimeoutException -> null
            is UnknownHostException -> null
            is ConnectException -> null
            else -> throw e
        }
    }

    return response?.use {
        return if (it.isSuccessful) {
            try {
                it.body()?.string()
            } catch (e: IOException) { // string() can throw
                appLogger.error("Request failed: $httpUrl\n${e.message}")
                null
            }
        } else {
            appLogger.warn("Request failed: $httpUrl\n${it.message()}")
            null
        }
    }
}

fun httpGet(url: String, params: Map<String, String?> = emptyMap()): String? {
    return httpGet(url,
        client = HttpClients.main,
        withUrl = {
            for ((key, value) in params) {
                addQueryParameter(key, value)
            }
        }
    )
}
