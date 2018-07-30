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

fun calculateNewPrice(symbol: String, dollarBuybackAmount: Long) = runBlocking {
    val iex = Iex(HttpClients.main)
    val previousJob = async { iex.getPreviousDay(symbol) }
    val earningsJob = async { iex.getEarnings(symbol) }
    val statsJob = async { iex.getAssetStats(symbol) }

    val previous = previousJob.await()
    val earnings = earningsJob.await()
    val stats = statsJob.await()

    if (previous != null && earnings != null && stats != null) {
        // EPS = net income / outstanding shares
        val ttmEPS = earnings.earnings.sumByDouble { it.actualEps }
        val currentPE = previous.close / ttmEPS
        val shareBuybackAmount = dollarBuybackAmount / previous.close
        val sharesOutstanding = stats.sharesOutstanding
        val netIncome = ttmEPS * sharesOutstanding
        val sharesAfterBuyback = sharesOutstanding - shareBuybackAmount
        val newEPS = netIncome / sharesAfterBuyback
        val newPrice = newEPS * currentPE

        println("ttm EPS: $ttmEPS")
        println("Current P/E: $currentPE")
        println("Shares Outstanding: $sharesOutstanding")
        println("New EPS: $newEPS")
        println("New Price: $newPrice")
    }
}