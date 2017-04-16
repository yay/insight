package main

import kotlinx.coroutines.experimental.runBlocking
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {

    val map = loadAllDailyQuotes(listOf("nasdaq"))
    val appleQuotes = map["nasdaq"]?.get("AAPL")
    if (appleQuotes != null) {
        doesNewHighMakesNewHigh(appleQuotes)
    }

//    Settings.load(AppSettings)
//    Settings.saveOnShutdown(AppSettings)
//
//    // The app won't exit while the scheduler is running.
//    appScheduler.start()
//
//    setupEndOfDayFetcher()

}

/**
 * How likely it is that a stock that makes a new high today goes up tomorrow?
 * How likely it is that a stock that makes a new low today goes down tomorrow?
 */
fun doesNewHighMakesNewHigh(quotes: List<Quote>) {
    // The first quote is for the most recent day, but we want to start from the past.
    val list = quotes.asReversed()
    val n = list.count()

    val newHighs = mutableListOf<Quote>()
    val newLows = mutableListOf<Quote>()

    var countNewHigh = 0
    var countNewLow = 0

    var countNextIsNewHigh = 0
    var countNextIsNewLow = 0

    if (n > 2) {
        var max = list.first().close
        var min = max
        for (i in 1..n-2) {
            val current = list[i]
            val currentClose = current.close
            val nextClose = list[i+1].close

            if (currentClose > max) { // new high
                newHighs.add(current)
                max = currentClose
                if (nextClose > currentClose) {
                    countNextIsNewHigh++
                }
            }
            if (currentClose < min) { // new low
                newLows.add(current)
                min = currentClose
                if (nextClose < currentClose) {
                    countNextIsNewLow++
                }
            }
        }
    }

    println("A new high today is likely to make a new high tomorrow" +
            "${countNextIsNewHigh.toDouble() / newHighs.count().toDouble() * 100}% of the time.")
    println("A new low today is likely to make a new low tomorrow" +
            "${countNextIsNewLow.toDouble() / newLows.count().toDouble() * 100}% of the time.")
    println("This stock had a total of ${newHighs.count()} new highs and ${newLows.count()} new lows.")

    println("\nList of new highs:")
    for (item in newHighs) {
        println("${item.date} @ ${item.close}")
    }
    println("\nList of new lows:")
    for (item in newLows) {
        println("${item.date} @ ${item.close}")
    }
}

fun test() {
    val syncTime = measureTimeMillis {
        runBlocking {
            val map = loadAllDailyQuotes(listOf("nasdaq", "nyse", "amex"))

            println(map["nasdaq"]?.get("AAPL")?.get(0)?.date.toString())
            println(map["nasdaq"]?.get("AAPL")?.get(0)?.close)
        }
    }
    println("loadAllDailyQuotes completed in $syncTime ms.")

    val asyncTime = measureTimeMillis {
        runBlocking {
            val map = asyncLoadAllDailyQuotes(listOf("nasdaq", "nyse", "amex"))

            println(map["nasdaq"]?.get("AAPL")?.get(0)?.date.toString())
            println(map["nasdaq"]?.get("AAPL")?.get(0)?.close)
        }
    }
    println("asyncLoadAllDailyQuotes completed in $asyncTime ms.")
}