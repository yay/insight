package main

/*

    Example URL:

    https://query2.finance.yahoo.com/v10/finance/quoteSummary/AAPL
        ?formatted=true
        &lang=en-US
        &region=US
        &modules=summaryProfile%2CfinancialData%2CrecommendationTrend%2CupgradeDowngradeHistory%2Cearnings%2CdefaultKeyStatistics%2CcalendarEvents
        &corsDomain=finance.yahoo.com

    === Notes ===

    Starting on 2017-05-04 summaries are missing certain data:

    "defaultKeyStatistics": {
        enterpriseValue: {},
        earningsQuarterlyGrowth: {},
        netIncomeToCommon: {},
        enterpriseToRevenue: {},
        enterpriseToEbitda: {},

    "financialData": {
        totalCash: {},
        totalCashPerShare: {},
        ebitda: {},
        totalDebt: {},
        quickRatio: {},
        currentRatio: {},
        totalRevenue: {},
        debtToEquity: {},
        revenuePerShare: {},
        returnOnAssets: {},
        returnOnEquity: {},
        freeCashflow: {},
        operatingCashflow: {},
        earningsGrowth: {},
        revenueGrowth: {},

*/

fun getYahooSummary(symbol: String): String? {
    val params = mapOf("modules" to "defaultKeyStatistics,financialData,earnings,calendarEvents,recommendationTrend,upgradeDowngradeHistory,summaryProfile")
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