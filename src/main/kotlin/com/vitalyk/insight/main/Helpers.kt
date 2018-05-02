package com.vitalyk.insight.main

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.time.*
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.zip.ZipInputStream

fun Long.toReadableNumber(): String {
    // E.g. 10_550_000_000.main.toReadableNumber()  // 10.6B
    if (this <= 0) return "0"
    val units = arrayOf("", "K", "M", "B", "T")
    val digitGroups = (Math.log10(this.toDouble()) / Math.log10(1000.0)).toInt()
    return DecimalFormat("#,##0.#").format(this / Math.pow(1000.0, digitGroups.toDouble())) + units[digitGroups]
}

fun Long.toLocalTime(timezone: ZoneId = TimeZone.getDefault().toZoneId()) =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), timezone)

fun Long.toEasternTime() = this.toLocalTime(ZoneId.of("America/New_York"))

// Replaces consecutive `char`s with a single `char`, while removing single `char`s.
fun dropSingleSquashSequence(text: String, char: Char): String {
    val sb = StringBuilder()
    if (text.isNotEmpty()) {
        var prev: Char? = null
        var lastAppended: Char? = null
        text.forEach {
            val skip = it == char && (prev != char || lastAppended == char)
            if (!skip) {
                sb.append(it)
                lastAppended = it
            }
            prev = it
        }
    }
    return sb.toString()
}

fun takeOneOffSequence(text: String, char: Char): String {
    val sb = StringBuilder()
    if (text.isNotEmpty()) {
        var skippedPrev = false
        text.forEach {
            val skip = it == char && !skippedPrev
            if (!skip) {
                sb.append(it)
                if (it != char) skippedPrev = false
            } else skippedPrev = true
        }
    }
    return sb.toString()
}

fun dropSingle(text: String, char: Char): String {
    val sb = StringBuilder()
    if (text.isNotEmpty()) {
        val last = text.lastIndex
        text.forEachIndexed { i, c ->
            val pi = i - 1
            val ni = i + 1
            val prev = if (pi >= 0) text[pi] else null
            val next = if (ni <= last) text[ni] else null
            val skip = c == char && prev != char && next != char
            if (!skip) sb.append(c)
        }
    }
    return sb.toString()
}

fun getLastWorkDay(zoneId: String = "America/New_York"): LocalDate {
    val now = LocalDate.now(ZoneId.of(zoneId))
    return when (DayOfWeek.of(now.get(ChronoField.DAY_OF_WEEK))) {
        DayOfWeek.MONDAY -> now.minus(3, ChronoUnit.DAYS)
        DayOfWeek.SUNDAY -> now.minus(2, ChronoUnit.DAYS)
        else -> now.minus(1, ChronoUnit.DAYS)
    }
}

/**
 * Writes the string to a file with the specified `pathname`, creating all parent
 * directories in the process.
 * @param  pathname  A pathname string
 * @throws  SecurityException
 * @throws  IOException
 */
fun String.writeToFile(pathname: String) {
    val file = File(pathname)

    file.parentFile.mkdirs()
    file.writeText(this)
}

val objectMapper: ObjectMapper = jacksonObjectMapper()
val objectWriter: ObjectWriter = objectMapper.writerWithDefaultPrettyPrinter()

fun String.toJsonNode(): JsonNode {
    return try {
        objectMapper.readTree(this)
    } catch (e: JsonProcessingException) {
        objectMapper.readTree("{}")
    }
}

fun String.toPrettyJson(): String = objectWriter.writeValueAsString(this.toJsonNode())

fun Any.toPrettyJson(): String = objectWriter.writeValueAsString(this)

fun listResources(cls: Class<*>) {
    cls.protectionDomain.codeSource?.apply {
        val zip = ZipInputStream(location.openStream())
        while (true) {
            zip.nextEntry?.name?.let {
                println(it)
            } ?: break
        }
    }
}