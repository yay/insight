package main

/*

    Example URL:

    https://query2.finance.yahoo.com/v10/finance/quoteSummary/AVGO
        ?formatted=true
        &crumb=X8WUMkCCDSk
        &lang=en-US
        &region=US
        &modules=defaultKeyStatistics%2CfinancialData%2CcalendarEvents
        &corsDomain=finance.yahoo.com

*/

fun getYahooSummary(symbol: String): String? {
    val params = mapOf("modules" to "defaultKeyStatistics,financialData,calendarEvents")
    val result = httpGet("https://query2.finance.yahoo.com/v10/finance/quoteSummary/$symbol", params)

    when (result) {
        is GetSuccess -> {
            return result.data // this JSON string is not "pretty"
        }
        is GetError -> {
            getAppLogger().warn("$symbol request status code ${result.code}: ${result.message}\n${result.url}")
        }
    }

    return null
}