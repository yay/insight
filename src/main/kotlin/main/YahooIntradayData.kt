package main

fun fetchIntradayData(symbol: String): String? {
    val result = httpGet("https://chartapi.finance.yahoo.com/instrument/1.0/$symbol/chartdata;type=quote;range=1d/json")

    when (result) {
        is GetSuccess -> {
            return result.data
        }
        is GetError -> {
            getAppLogger().error("$symbol request status code ${result.code}: ${result.message}")
        }
    }

    return null
}