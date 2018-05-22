package com.vitalyk.insight.iex

import com.vitalyk.insight.main.HttpClients
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking


fun main(args: Array<String>) {
    calculateNewPrice("MU", 10_000_000_000)
}

fun calculateNewPrice(symbol: String, dollarBuybackAmount: Long) = runBlocking {
    Iex.setOkHttpClient(HttpClients.main)
    val previous = async { Iex.getPreviousDay(symbol) }.await()
    val earnings = async { Iex.getEarnings(symbol) }.await()
    val stats = async { Iex.getAssetStats(symbol) }.await()

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

        println("ttmEPS: $ttmEPS")
        println("Current P/E: $currentPE")
        println("Shares Outstanding: $sharesOutstanding")
        println("New Price: $newPrice")
    }
}