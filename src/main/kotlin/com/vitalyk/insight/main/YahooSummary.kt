package com.vitalyk.insight.main

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

private val summaryModules = listOf<String>(
    "defaultKeyStatistics",
    "financialData",
    "earnings",
    "calendarEvents",
    "recommendationTrend",
    "upgradeDowngradeHistory",
    "incomeStatementHistory",
    "cashflowStatementHistory",
    "balanceSheetHistory",
    "assetProfile"  // there is also "summaryProfile" which is the same, but less comprehensive
)

private val defaultSummaryParams = mapOf("modules" to summaryModules.joinToString(","))

fun getYahooSummary(symbol: String, params: Map<String, String> = defaultSummaryParams): String? {
    val result = httpGet("https://query2.finance.yahoo.com/v10/finance/quoteSummary/$symbol", params)

    when (result) {
        is HttpGetSuccess -> {
            return result.data // this JSON string is not "pretty"
        }
        is HttpGetError -> {
            getAppLogger().warn("$symbol request status code ${result.code}: ${result.message}\n${result.url}")
        }
    }

    return null
}