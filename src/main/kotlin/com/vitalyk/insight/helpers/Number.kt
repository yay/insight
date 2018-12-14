package com.vitalyk.insight.helpers

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.system.measureTimeMillis

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

fun Double.toPercentString(): String = "%.2f%%".format(this * 100.0 - 100.0)

suspend fun main(args: Array<String>) {
    val t = 10L
    fun dot(): String {
        Thread.sleep(t)
        return "."
    }
    val ms = measureTimeMillis {
        (0..10_000).map { GlobalScope.async { dot() } }
            .map { it.await() }
            .forEach { print(it) }
    }
    println("Time taken: $ms ms")
    // no sleep - 155 ms
    // 1 ms sleep - 1888 ms
    // 10 ms sleep - 16705 ms
    // Expected duration of sequential execution is at least 100 seconds.
    // So it's not sequential by any means. But I would expect it to schedule
    // all calls and return immediately from the first 'map',
    // then wait for t milliseconds (the time of the longest 'await', in this
    // case they all last t milliseconds) and print all the dots.
    // As the time to run the `dot` function grows, the total execution time
    // increases exponentially.
    // This is probably because the coroutines run in a thread pool (with limited
    // number of threads) and not an event loop like in a browser or Node.

    // For example, this JS code doesn't wait for 16 seconds before printing,
    // it executes in split second:
    // {
    //     const t = 10;
    //     for (let i = 0; i < 10000; i++) {
    //         setTimeout(() => {
    //             process.stdout.write('.');
    //         }, t);
    //     }
    // }
}