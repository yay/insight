package main

fun fetchIntradayData(symbol: String): String {
    var data = ""

    val result = httpGet("https://chartapi.finance.yahoo.com/instrument/1.0/$symbol/chartdata;type=quote;range=1d/json")

    when (result) {
        is GetSuccess -> {
            data = result.data
        }
        is GetError -> {
            println("$symbol request status code ${result.code}: ${result.message}")
        }
    }

    return data
}