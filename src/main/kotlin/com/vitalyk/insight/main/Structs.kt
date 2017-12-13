package com.vitalyk.insight.main

import org.joda.time.DateTime
import java.math.BigDecimal

data class IntradayQuote(
    val time: DateTime,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long
)

data class Quote(
    val date: DateTime,
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
