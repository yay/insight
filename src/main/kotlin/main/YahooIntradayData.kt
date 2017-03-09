package main

fun fetchIntradayData(symbol: String): String {
    val result = httpGet("https://chartapi.finance.yahoo.com/instrument/1.0/$symbol/chartdata;type=quote;range=1d/json")
    var data = ""

    when (result) {
        is GetSuccess -> {
            data = result.data
        }
        is GetError -> {
            println("$symbol request ${result.code} error: ${result.message}")
        }
    }

    return data
}