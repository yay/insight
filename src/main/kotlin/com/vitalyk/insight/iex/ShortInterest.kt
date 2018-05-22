package com.vitalyk.insight.iex

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.module.kotlin.readValue
import com.vitalyk.insight.helpers.objectMapper
import com.vitalyk.insight.helpers.toPrettyJson
import com.vitalyk.insight.helpers.writeToFile
import com.vitalyk.insight.main.HttpClients
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import java.time.*

private const val filePath = "./data/short_interest.json"

fun main(args: Array<String>) {
    shortInterest()
    miniShortInterest()
}

fun miniShortInterest() {
    val list = objectMapper.readValue<List<Iex.AssetStats>>(File(filePath))

    list
        .map {
            val shortInterestPct = it.shortInterest.toDouble() / it.sharesOutstanding.toDouble()
            object {
                val shortInterest = "%.2f%%".format(shortInterestPct * 100)
                val symbol = it.symbol
                val name = it.companyName
                @JsonFormat(pattern = "MMM dd, yy")
                val date = it.shortDate
            }
        }
        .toPrettyJson()
        .writeToFile("./data/mini_short_interest.json")
}

fun shortInterest() {
    runBlocking {
        Iex.setOkHttpClient(HttpClients.main)

        val monthAgo = LocalDate.now().minusMonths(1)
        Iex.getSymbols()?.let {
            var count = 0
            val total = it.size
            val list = it.map {
                async {
                    Iex.getAssetStats(it.symbol).apply {
                        println("${count++} of $total")
                    }
                }
            }.mapNotNull {
                it.await()
            }.mapNotNull {
                val shortDate = it.shortDate
                if (it.shortInterest > 0 && it.sharesOutstanding > 0 &&
                    shortDate != null && shortDate.isAfter(monthAgo))
                    it
                else
                    null
            }.sortedByDescending {
                it.shortInterest.toDouble() / it.sharesOutstanding.toDouble()
            }

            list.toPrettyJson().writeToFile(filePath)
        }
    }
}