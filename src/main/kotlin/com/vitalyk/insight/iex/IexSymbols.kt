package com.vitalyk.insight.iex

import com.vitalyk.insight.iex.Iex.Symbol
import kotlinx.coroutines.experimental.async

object IexSymbols {
    private var cache = listOf<Symbol>()

    fun update() {
        async {
            Iex.getSymbols()?.let {
                cache = it
            }
        }
    }

    fun complete(part: String, max: Int = 10): List<Symbol> = cache.filter { it.symbol.startsWith(part) }.take(max)

    fun find(symbol: String): Symbol? = cache.firstOrNull { it.symbol == symbol }

    fun name(symbol: String): String? = find(symbol)?.name
}