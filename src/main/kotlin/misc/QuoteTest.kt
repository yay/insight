package misc

import kotlinx.coroutines.experimental.runBlocking
import main.*
import kotlin.system.measureTimeMillis

fun test1() {
    val map = loadAllDailyQuotes(listOf("nasdaq"))

    var count = 0
    var upSum = 0.0
    var downSum = 0.0
    val counted = mutableListOf<Pair<ExchangeName, Ticker>>()

    for ((exchangeName, exchangeMap) in map) {
        for ((ticker, quotes) in exchangeMap) {
            val values = doesNewHighMakeNewHigh(quotes, 125)

            if (!values.first.isNaN() && !values.second.isNaN()) {
                upSum += values.first
                downSum += values.second
                count++
                counted.add(exchangeName to ticker)
            }
        }
    }

    val upAverage = upSum / count.toDouble()
    val downAverage = downSum / count.toDouble()

    println("$count: $upAverage / $downAverage")
    for (item in counted) {
        println(item)
    }

//    val appleQuotes = map["nasdaq"]?.get("MSFT")
//    if (appleQuotes != null) {
//        doesNewHighMakeNewHigh(appleQuotes, 1, true)
//    }
}

/**
 * How likely it is that a stock that makes a new high today goes up tomorrow?
 * How likely it is that a stock that makes a new low today goes down tomorrow?
 * `tomorrow` is the default, use the `lookAheadDays` param to tweak this.
 * Note: for `lookAheadDays` ever 5 market days mean 1 regular week.
 * E.g. to look two quarters ahead, use 125.
 * Returns a pair of doubles, first value is for the up percentage, second for down.
 */
fun doesNewHighMakeNewHigh(quotes: List<Quote>, lookAheadDays: Int = 1, debug: Boolean = false): Pair<Double, Double> {
    // The first quote is for the most recent day, but we want to start from the past.
    val list = quotes.asReversed()
    val n = list.count()

    val newHighs = mutableListOf<Quote>()
    val newLows = mutableListOf<Quote>()

    var countNextIsNewHigh = 0
    var countNextIsNewLow = 0

    if (n > lookAheadDays + 1) {
        var max = list.first().close
        var min = max
        for (i in 1..n - 1 - lookAheadDays) {
            val current = list[i]
            val currentClose = current.close
            val nextClose = list[i + lookAheadDays].close

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

    val upRatio = countNextIsNewHigh.toDouble() / newHighs.count().toDouble()
    val downRatio = countNextIsNewLow.toDouble() / newLows.count().toDouble()
    val percentUp = "%.2f%%".format(upRatio * 100)
    val percentDown = "%.2f%%".format(downRatio * 100)

    if (debug) {
        println("A stock making a new high today is likely to worth more " +
            "$lookAheadDays day(s) from now $percentUp of the time.")
        println("A stock making a new low today is likely to worth less " +
            "$lookAheadDays day(s) from now $percentDown of the time.")
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

    return upRatio to downRatio
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