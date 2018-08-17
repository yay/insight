package com.vitalyk.insight.yahoo

import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.UserAgents
import com.vitalyk.insight.main.httpGet

fun yahooGet(url: String, params: Map<String, String?> = emptyMap()): String? {
    return httpGet(url,
        client = HttpClients.yahoo,
        withUrl = {
            for ((key, value) in params) {
                addQueryParameter(key, value)
            }
        },
        withRequest = {
            addHeader("User-Agent", UserAgents.chrome)
        }
    )
}