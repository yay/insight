package com.vitalyk.insight.iex

import com.vitalyk.insight.iex.Iex.PreviousDay
import com.vitalyk.insight.iex.Iex.Symbol
import kotlinx.coroutines.experimental.launch

object IexSymbols {
    private var symbolMap = mapOf<String, Symbol>()
    private var prevDayMap = mapOf<String, PreviousDay>()

    fun update() {
        launch {
            Iex.getSymbols()?.let {
                symbolMap = it.map { it.symbol to it }.toMap()
            }
        }
        launch {
            Iex.getPreviousDay()?.let { prevDayMap = it }
        }
    }

    // Returns the list of symbols that have the `part` sequence in their name.
    private fun completeByName(part: String, max: Int = 10): List<Symbol> {
        val lowerCasePart = part.toLowerCase()
        return symbolMap.values.asSequence().filter {
            it.name.toLowerCase().contains(lowerCasePart)
        }.take(max).toList()
    }

    // Returns the list of symbols that match the first letters of the ticker (`part`)
    // or, if none found, the list of symbols that have the `part` sequence somewhere
    // in their name.
    fun complete(part: String?, max: Int = 10): List<Symbol> {
        if (part == null || part.isBlank()) return emptyList()

        val upperCasePart = part.toUpperCase()
        return symbolMap.values.asSequence().filter {
            it.symbol.startsWith(upperCasePart)
        }.take(max).toList().takeIf { it.isNotEmpty() } ?: completeByName(part)
    }

    // Given the exact ticker, returns the associated symbol.
    fun find(symbol: String): Symbol? = symbolMap[symbol]

    fun previousDay(symbol: String): PreviousDay? = prevDayMap[symbol]

    // Given the ticker, returns the name of the asset.
    fun name(symbol: String): String? = find(symbol)?.name

    // Check if the ticker is known, e.g: `"AAPL" in IexSymbols`.
    operator fun contains(symbol: String) = symbol in symbolMap
}