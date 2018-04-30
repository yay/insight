package com.vitalyk.insight.yahoo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.vitalyk.insight.main.getAppLogger
import com.vitalyk.insight.main.objectMapper
import com.vitalyk.insight.main.toJsonNode
import java.sql.Time
import java.util.*

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

private val defaultSummaryParams = mapOf("modules" to summaryModules.joinToString(","))

@JsonIgnoreProperties(ignoreUnknown = true)
data class AssetProfile(
    val address1: String?,
    val city: String?,
    val state: String?,
    val zip: String?,
    val country: String?,
    val phone: String?,
    val website: String?,
    val industry: String?,
    val sector: String?,
    val longBusinessSummary: String,
    val fullTimeEmployees: Int?,
    val companyOfficers: List<CompanyOfficer>,
    val auditRisk: Int?,
    val boardRisk: Int?,
    val compensationRisk: Int?,
    val shareHolderRightsRisk: Int?,
    val overallRisk: Int?,
    val governanceEpochDate: Time?,
    val compensationAsOfEpochDate: Long?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FmtValue<T>(
    val raw: T,
    val fmt: String?,
    val longFmt: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CompanyOfficer(
    val name: String,
    val age: Int,
    val title: String,
    val yearBorn: Int,
    val fiscalYear: Int,
    val totalPay: FmtValue<Int>?,
    val exercisedValue: FmtValue<Int>,
    val unexercisedValue: FmtValue<Int>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CalendarEvents(
    val earnings: Earnings,
    val exDividendDate: FmtValue<Date>?,
    val dividendDate: FmtValue<Date>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Earnings(
    val earningsDate: List<FmtValue<Date>>?,
    val earningsAverage: FmtValue<Double>?,
    val earningsLow: FmtValue<Double>?,
    val earningsHigh: FmtValue<Double>?,
    val revenueAverage: FmtValue<Long>?,
    val revenueLow: FmtValue<Long>?,
    val revenueHigh: FmtValue<Long>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UpgradeDowngradeHistory(
    val history: List<UpgradeDowngrade>
)

data class UpgradeDowngrade(
    val epochGradeDate: Date,
    val firm: String,
    val toGrade: String,
    val fromGrade: String,
    val action: Action
) {
    companion object {
        enum class Action {
            @JsonProperty("init")
            INIT,
            @JsonProperty("up")
            UP,
            @JsonProperty("down")
            DOWN
        }

        private val upGrades = setOf("Buy", "Outperform", "Strong Buy")
    }

    fun isUp(): Boolean = action == Action.UP || toGrade in upGrades
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DefaultKeyStatistics(
    val enterpriseValue: FmtValue<Long>,
    val forwardPE: FmtValue<Double>,
    val profitMargins: FmtValue<Double>,
    val category: String?,
    val floatShares: FmtValue<Long>,
    val sharesOutstanding: FmtValue<Long>,
    val sharesShort: FmtValue<Long>,
    val sharesShortPriorMonth: FmtValue<Long>,
    val heldPercentInsiders: FmtValue<Double>,
    val heldPercentInstitutions: FmtValue<Double>,
    val shortRatio: FmtValue<Double>,
    val shortPercentOfFloat: FmtValue<Double>,
    val beta: FmtValue<Double>,
    val bookValue: FmtValue<Double>,
    val priceToBook: FmtValue<Double>,
    val lastFiscalYearEnd: FmtValue<Date>,
    val nextFiscalYearEnd: FmtValue<Date>,
    val mostRecentQuarter: FmtValue<Date>,
    val earningsQuarterlyGrowth: FmtValue<Double>,
    val revenueQuarterlyGrowth: FmtValue<Double>,
    val trailingEps: FmtValue<Double>,
    val forwardEps: FmtValue<Double>,
    val pegRatio: FmtValue<Double>,
    val lastSplitFactor: String,
    val lastSplitDate: FmtValue<Date>,
    val enterpriseToRevenue: FmtValue<Double>,
    val enterpriseToEbitda: FmtValue<Double>?,
    @JsonProperty("52WeekChange")
    val week52Change: FmtValue<Double>,
    @JsonProperty("SandP52WeekChange")
    val week52SPChange: FmtValue<Double>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class QuoteSummary(
    val assetProfile: AssetProfile?,
    val calendarEvents: CalendarEvents,
    val upgradeDowngradeHistory: UpgradeDowngradeHistory,
    val defaultKeyStatistics: DefaultKeyStatistics
)

fun getYahooSummary(symbol: String, params: Map<String, String> = defaultSummaryParams): String? {
    return try {
        yahooGet("https://query2.finance.yahoo.com/v10/finance/quoteSummary/$symbol", params)
    } catch (e: Exception) {
        getAppLogger().warn("getYahooSummary($symbol): ${e.message}")
        null
    }
}

fun getQuoteSummary(symbol: String): QuoteSummary? {
    val summary = getYahooSummary(symbol)?.let { it.replace("{}", "null") }
    val jsonNode = summary?.toJsonNode()
    val assetProfile = jsonNode
        ?.get("quoteSummary")
        ?.get("result")
        ?.first()

    return try {
        objectMapper.convertValue(assetProfile, QuoteSummary::class.java)
    } catch (e: Exception) {
        println("$symbol $summary")
        e.printStackTrace()
        null
    }
}

fun getAssetProfile(symbol: String): AssetProfile? {
    val summary = getYahooSummary(symbol)?.let { it.replace("{}", "null") }
    val jsonNode = summary?.toJsonNode()
    val assetProfile = jsonNode
        ?.get("quoteSummary")
        ?.get("result")
        ?.first()
        ?.get("assetProfile")

    return try {
        objectMapper.convertValue(assetProfile, AssetProfile::class.java)
    } catch (e: Exception) {
        println("$symbol $summary")
        e.printStackTrace()
        null
    }
}