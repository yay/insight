package main

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

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

typealias YahooSummaryData = String

fun getYahooSummary(symbol: String): YahooSummaryData {
    var data = ""

    val params = mapOf("modules" to "defaultKeyStatistics,financialData,calendarEvents")
    val result = httpGet("https://query2.finance.yahoo.com/v10/finance/quoteSummary/$symbol", params)

    when (result) {
        is GetSuccess -> {
            data = result.data
        }
        is GetError -> {
            getAppLogger().error("$symbol request status code ${result.code}: ${result.message}\n${result.url}")
        }
    }

    return data
}

fun YahooSummaryData.toJson(): JsonNode {
    val mapper = ObjectMapper()

    try {
        return mapper.readTree(this)
    } catch (e: JsonProcessingException) {
        return mapper.readTree("{}")
    }
}

fun YahooSummaryData.toJsonString(): String =
        ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this.toJson())