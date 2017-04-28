package main

fun fetchIntradayData(symbol: String): String? {
    val url = "https://chartapi.finance.yahoo.com/instrument/1.0/$symbol/chartdata;type=quote;range=1d/json"
    val result = httpGet(url)

    when (result) {
        is GetSuccess -> {
            return result.data
        }
        is GetError -> {
            getAppLogger().error("$url ${result.code}: ${result.message}")
        }
    }

    return null
}