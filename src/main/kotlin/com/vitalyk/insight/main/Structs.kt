package com.vitalyk.insight.main

import java.math.BigDecimal
import java.time.LocalDate

data class IntradayQuote(
    val time: LocalDate,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long
)

data class Quote(
    val date: LocalDate,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val adjClose: BigDecimal,
    val volume: Long
)

//data class DailyQuotes(
//        val exchange: String,
//        val market: String,
//        val symbol: String,
//        val quotes: List<Quote>
//)
