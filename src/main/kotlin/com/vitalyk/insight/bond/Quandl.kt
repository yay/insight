package com.vitalyk.insight.bond

import com.vitalyk.insight.main.httpGet
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.text.SimpleDateFormat
import java.util.*

private const val usYieldUrl = "https://www.quandl.com/api/v3/datasets/USTREASURY/YIELD.csv"
private const val apiKey = "D56KmTLWdnWBzWexFcX2"
private val apiKeyParam = mapOf("api_key" to apiKey)

data class Yield(
    val date: Date,
    val mo1: Double? = null,
    val mo3: Double? = null ,
    val mo6: Double? = null,
    val yr1: Double? = null,
    val yr2: Double? = null,
    val yr3: Double? = null,
    val yr5: Double? = null,
    val yr7: Double? = null,
    val yr10: Double? = null,
    val yr20: Double? = null,
    val yr30: Double? = null
)

fun getUsYieldData(): List<Yield> {
    return httpGet(usYieldUrl, apiKeyParam)?.let {
        val records = CSVFormat.DEFAULT
            .withFirstRecordAsHeader()
            .withNullString("")
            .parse(it.reader())
        mapUsYieldRecords(records)
    } ?: emptyList()
}

fun getLocalUsYieldData(): List<Yield> {
    val reader = ClassLoader
        .getSystemResourceAsStream("data/bonds/USTREASURY-YIELD.csv")
        .bufferedReader()
    val records = CSVFormat.DEFAULT
        .withFirstRecordAsHeader()
        .withNullString("")
        .parse(reader)
    return mapUsYieldRecords(records)
}

private fun mapUsYieldRecords(records: CSVParser): List<Yield> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    return records.map {
        Yield(
            dateFormat.parse(it.get("Date")),
            it.get("1 MO")?.toDouble(),
            it.get("3 MO")?.toDouble(),
            it.get("6 MO")?.toDouble(),
            it.get("1 YR")?.toDouble(),
            it.get("2 YR")?.toDouble(),
            it.get("3 YR")?.toDouble(),
            it.get("5 YR")?.toDouble(),
            it.get("7 YR")?.toDouble(),
            it.get("10 YR")?.toDouble(),
            it.get("20 YR")?.toDouble(),
            it.get("30 YR")?.toDouble()
        )
    }
}