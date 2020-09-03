package com.vitalyk.insight.main

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object UserAgents {
    const val chrome = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/69.0.3497.100 Safari/537.36"
}

data class YFinanceAuth(
        val cookie: String,
        val crumb: String
)

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
        readTimeout(10L, TimeUnit.SECONDS)
        // Yahoo Finance now requires a cookie to fetch historical and other data.
        // addInterceptor takes an Interceptor, which is an interface with a single
        // method that takes a Chain and returns a Response,
        // so we can just pass the implementation of that method.
        val auth = getYFinanceAuth()

        addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Cookie", auth.cookie)
                .build()

            chain.proceed(request)
        }
    }

    private fun createClient(block: OkHttpClient.Builder.() -> Unit): OkHttpClient {
        val builder = OkHttpClient.Builder()
        block(builder)
        val client = builder.build()
        clients.add(client)
        return client
    }

    private fun getYFinanceAuth(symbol: String = "AAPL"): YFinanceAuth {
        val url = "https://uk.finance.yahoo.com/quote/$symbol/history"
        val httpUrl = url.toHttpUrlOrNull() ?: throw MalformedURLException("Invalid HttpUrl.")
        val urlBuilder = httpUrl.newBuilder()
        val requestUrl = urlBuilder.build().toString()
        val request = Request.Builder()
                .addHeader("User-Agent", UserAgents.chrome)
                .url(requestUrl)
                .build()
        val response = HttpClients.main.newCall(request).execute()
        val body = response.body ?: throw Exception("The response has no body.")

        val cookieHeader = response.headers("set-cookie")

        if (cookieHeader.isNotEmpty()) {
            val cookie = response.headers("set-cookie").first().split(";").first()
            // Example: "CrumbStore":{"crumb":"l45fI\u002FklCHs"}
            // val crumbRegEx = Regex(""".*"CrumbStore":\{"crumb":"([^"]+)"}""", RegexOption.MULTILINE)
            // val crumb = crumbRegEx.find("body.string()")?.groupValues?.get(1) // takes ages
            val text = body.string()
            val keyword = "CrumbStore\":{\"crumb\":\""
            val start = text.indexOf(keyword)
            val end = text.indexOf("\"}", start)
            val crumb = text.substring(start + keyword.length until end)
            if (crumb.isBlank()) {
                throw Exception("Crumb is blank.")
            }
            return YFinanceAuth(cookie, crumb)
        } else {
            throw Exception("No cookie found.")
        }
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
            it.dispatcher.executorService.shutdown()
            it.connectionPool.evictAll()
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
    val httpUrl = (url.toHttpUrlOrNull() ?: throw IllegalArgumentException("Bad URL: $url"))
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
                it.body?.string()
            } catch (e: IOException) { // string() can throw
                appLogger.error("Request failed - ${e.message}\nURL: $httpUrl")
                null
            }
        } else {
            appLogger.warn("Request failed - ${it.code} - ${it.message}\nURL: $httpUrl")
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
