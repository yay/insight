package com.vitalyk.insight.yahoo

import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit

fun isDistributionDay(prev: ChartPoint, curr: ChartPoint): Boolean {
    // Volume is higher than the previous day and decline is greater than 0.2%.
    return (curr.volume > prev.volume) && (curr.close / prev.close - 1.0 < -0.002)
}

data class DistributionData(
    val symbol: String,
    // Past 5 weeks of trading days.
    val recentDays: List<ChartPoint>,
    // Indexes of distribution days.
    val downDays: List<Int>
)

fun getDistributionData(symbol: String): DistributionData? {
    val down = mutableListOf<Int>()
    return getChartPoints(symbol, 5, ChronoUnit.WEEKS)?.let {
        it.reduceIndexed { index, prev, curr ->
            if (isDistributionDay(prev, curr)) {
                down.add(index)
            }
            curr
        }
        DistributionData(symbol, it, down)
    }
}

fun getDistributionInfo(symbols: Set<String> = marketIndexes.keys,
                        listDays: Boolean = true): String {
    val dateFormat = SimpleDateFormat("d MMM")
    val sb = StringBuilder()
    symbols.forEach {
        getDistributionData(it)?.apply {
            val name = marketIndexes[symbol] ?: symbol
            sb.append(downDays.size)
            sb.append(" - ")
            sb.append(name)
            if (listDays) {
                sb.append("\n")
                sb.append(downDays.joinToString(", ") {
                    dateFormat.format(recentDays[it].date)
                })
                sb.append("\n")
            }
            sb.append("\n")
        }
    }
    return sb.toString()
}