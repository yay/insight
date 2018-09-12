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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        println("Select where to save the data...")
        val date = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        FileChooser().apply {
            title = "Save as JSON"
            initialFileName = "TopPriceToSales-$date.json"
            showSaveDialog(null)?.apply {
                writeText(entries.toPrettyJson())
                println("Saved to: $absolutePath")
            }
        }
    }

    priceToSales.forEach {
        println("${it.symbol} ${it.priceToSales} ${it.marketCap.toReadableNumber()} ")
    }
}

class PriceToSalesApp: Application() {
    override fun start(primaryStage: Stage) {
        async {
            topPriceToSales()
        }
    }
}

fun main(args: Array<String>) {
    Application.launch(PriceToSalesApp::class.java, *args)
}