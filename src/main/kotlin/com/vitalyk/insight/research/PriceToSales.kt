package com.vitalyk.insight.research

import com.vitalyk.insight.helpers.toPrettyJson
import com.vitalyk.insight.helpers.toReadableNumber
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.main.HttpClients
import javafx.application.Application
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.experimental.async
import tornadofx.*

data class Entry(
    val symbol: String,
    val priceToSales: Double,
    val marketCap: Long,
    val price: Double
)

suspend fun topPriceToSales() {
    val iex = Iex(HttpClients.main)
    val prevDayMap = iex.getPreviousDay() ?: return
    val stats = iex.getAssetStatsAsync()

    val priceToSales = stats.values
        .filter { it.marketCap > 500_000_000 && it.priceToSales > 100.0 }
        .sortedByDescending { it.priceToSales }

    val entries = priceToSales.map {
        Entry(it.symbol, it.priceToSales, it.marketCap, prevDayMap[it.symbol]?.close ?: 0.0)
    }

    runLater {
        FileChooser().apply {
            title = "Save as JSON"
            showSaveDialog(null)?.writeText(entries.toPrettyJson())
        }
    }

    priceToSales.forEach {
        println("${it.symbol} ${it.priceToSales} ${it.marketCap.toReadableNumber()} ")
    }
}

class PriceToSalesApp: Application() {
    override fun start(primaryStage: Stage) {
        async { topPriceToSales() }
    }
}

fun main(args: Array<String>) {
    Application.launch(PriceToSalesApp::class.java, *args)
}