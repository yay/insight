package com.vitalyk.insight.helpers

import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private val numberUnits = arrayOf("", "K", "M", "B", "T")
private val decimalFormat = DecimalFormat("#,##0.#")

fun Long.toReadableNumber(): String {
    // E.g. 10_550_000_000.main.toReadableNumber()  // 10.6B
    if (this <= 0) return "0"
    val thousands = (Math.log10(this.toDouble()) / Math.log10(1000.0)).toInt()
    return decimalFormat.format(this / Math.pow(1000.0, thousands.toDouble())) +
        numberUnits[thousands]
}

fun Long.toLocalTime(timezone: ZoneId = TimeZone.getDefault().toZoneId()) =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), timezone)

fun Long.toEasternTime() = this.toLocalTime(ZoneId.of("America/New_York"))