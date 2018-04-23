package com.vitalyk.insight.yahoo

import com.vitalyk.insight.main.getAppLogger

/**
 * This API has been discontinued on May 17, 2017.
 */
fun fetchIntradayData(symbol: String): String? {
    val url = "https://chartapi.finance.yahoo.com/instrument/1.0/$symbol/chartdata;type=quote;range=1d/json"
    val result = yahooGet(url)

    when (result) {
        is YahooGetSuccess -> {
            return result.data
        }
        is YahooGetFailure -> {
            getAppLogger().error("$url ${result.code}: ${result.message}")
        }
    }

    return null
}