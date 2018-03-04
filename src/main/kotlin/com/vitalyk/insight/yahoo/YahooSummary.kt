package com.vitalyk.insight.yahoo

import com.vitalyk.insight.main.YahooGetFailure
import com.vitalyk.insight.main.YahooGetSuccess
import com.vitalyk.insight.main.getAppLogger
import com.vitalyk.insight.main.yahooGet

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

private val summaryModules = listOf(
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

private val defaultSummaryParams = listOf("modules" to summaryModules.joinToString(","))

fun getYahooSummary(symbol: String, params: List<Pair<String, String>> = defaultSummaryParams): String? {
    val result = yahooGet("https://query2.finance.yahoo.com/v10/finance/quoteSummary/$symbol", params)

    when (result) {
        is YahooGetSuccess -> {
            return result.data // this JSON string is not "pretty"
        }
        is YahooGetFailure -> {
            getAppLogger().warn("$symbol request status code ${result.code}: ${result.message}\n${result.url}")
        }
    }

    return null
}