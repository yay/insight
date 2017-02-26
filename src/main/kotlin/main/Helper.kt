package main

import java.text.DecimalFormat

fun Long.toReadableNumber(): String {
    // E.g. 10_550_000_000.main.toReadableNumber()  // 10.6B
    if (this <= 0) return "0"
    val units = arrayOf("", "K", "M", "B", "T")
    val digitGroups = (Math.log10(this.toDouble()) / Math.log10(1000.0)).toInt()
    return DecimalFormat("#,##0.#").format(this / Math.pow(1000.0, digitGroups.toDouble())) + units[digitGroups]
}