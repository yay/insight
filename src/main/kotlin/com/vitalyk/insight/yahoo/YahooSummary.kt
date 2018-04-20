package com.vitalyk.insight.yahoo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.vitalyk.insight.main.*
import java.sql.Time

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

@JsonIgnoreProperties(ignoreUnknown = true)
data class AssetProfile(
    val address1: String,
    val city: String,
    val state: String,
    val zip: Int,
    val country: String,
    val phone: String,
    val website: String,
    val industry: String,
    val industrySymbol: String,
    val sector: String,
    val longBusinessSummary: String,
    val fullTimeEmployees: Int,
    val companyOfficers: List<CompanyOfficer>,
    val auditRisk: Int,
    val boardRisk: Int,
    val compensationRisk: Int,
    val shareHolderRightsRisk: Int,
    val overallRisk: Int,
    val governanceEpochDate: Time,
    val compensationAsOfEpochDate: Int,
    val maxAge: Int
)

data class FmtValue<T>(
    val raw: T,
    val fmt: String?,
    val longFmt: String?
)

data class CompanyOfficer(
    val maxAge: Int,
    val name: String,
    val age: Int,
    val title: String,
    val yearBorn: Int,
    val fiscalYear: Int,
    val totalPay: FmtValue<Int>?,
    val exercisedValue: FmtValue<Int>,
    val unexercisedValue: FmtValue<Int>
)

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

fun getAssetProfile(symbol: String): AssetProfile? {
    val summary = getYahooSummary(symbol)
    println(summary)
    val jsonNode = summary?.toJsonNode()
    val assetProfile = jsonNode
        ?.get("quoteSummary")
        ?.get("result")
        ?.first()
        ?.get("assetProfile")
    println(assetProfile)
    return objectMapper.convertValue(assetProfile, AssetProfile::class.java)
}