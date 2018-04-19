package com.vitalyk.insight.iex

import com.vitalyk.insight.iex.Iex.Symbol
import kotlinx.coroutines.experimental.launch

object IexSymbols {
    private var cache = listOf<Symbol>()

    fun update() {
        launch {
            Iex.getSymbols()?.let {
                cache = it
            }
        }
    }

    private fun completeByName(part: String, max: Int = 10): List<Symbol> {
        val lowerCasePart = part.toLowerCase()
        return cache.asSequence().filter {
            it.name.toLowerCase().contains(lowerCasePart)
        }.take(max).toList()
    }

    fun complete(part: String?, max: Int = 10): List<Symbol> {
        if (part == null || part.isBlank()) return emptyList()

        val upperCasePart = part.toUpperCase()
        return cache.asSequence().filter {
            it.symbol.startsWith(upperCasePart)
        }.take(max).toList().takeIf { it.isNotEmpty() } ?: completeByName(part)
    }

    fun find(symbol: String): Symbol? = cache.firstOrNull { it.symbol == symbol }

    fun name(symbol: String): String? = find(symbol)?.name

//    init { // The 'init' will be triggered on first use of the singleton
//        update()
//    }
}