package com.vitalyk.insight.iex

import com.vitalyk.insight.main.HttpClients
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking


//fun main(args: Array<String>) {
//    // On May 21, 2018 showed that Micron can now be valued at $65/share.
//    calculateNewPrice("MU", 10_000_000_000)
//    // QCOM - no NXP purchase, target 90
//    // AVGO - no QCOM purchase, target 270
//}

data class PriceAfterBuyback(
    val newPrice: Double,
    val ttmEPS: Double,
    val newEPS: Double,
    val currentPE: Double,
    val sharesOutstanding: Long
)

fun calculateNewPrice(symbol: String, dollarBuybackAmount: Long): PriceAfterBuyback? = runBlocking {
    val iex = Iex(HttpClients.main)
    val previousJob = async { iex.getPreviousDay(symbol) }
    val earningsJob = async { iex.getEarnings(symbol) }
    val statsJob = async { iex.getAssetStats(symbol) }

    val previous = previousJob.await()
    val earnings = earningsJob.await()
    val stats = statsJob.await()

    var result: PriceAfterBuyback? = null

    if (previous != null && earnings != null && stats != null) {
        // EPS = net income / outstanding shares
        val ttmEPS = earnings.earnings.sumByDouble { it.actualEps }
        val currentPE = previous.close / ttmEPS
        val shareBuybackAmount = dollarBuybackAmount / previous.close
        val sharesOutstanding = stats.sharesOutstanding
        val netIncome = ttmEPS * sharesOutstanding
        val sharesAfterBuyback = sharesOutstanding - shareBuybackAmount
        val newEPS = netIncome / sharesAfterBuyback

        result = PriceAfterBuyback(
            newPrice = newEPS * currentPE,
            ttmEPS = ttmEPS,
            newEPS = newEPS,
            currentPE = currentPE,
            sharesOutstanding = sharesOutstanding
        )
    }
    result
}