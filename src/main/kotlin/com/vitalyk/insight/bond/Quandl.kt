package com.vitalyk.insight.bond

import com.vitalyk.insight.main.appLogger
import com.vitalyk.insight.main.httpGet
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val usYieldUrl = "https://www.quandl.com/api/v3/datasets/USTREASURY/YIELD.csv"
private const val apiKey = "D56KmTLWdnWBzWexFcX2"
private val apiKeyParam = mapOf("api_key" to apiKey)

data class UsYield(
    val date: Date,
    val mo1: Double?,
    val mo3: Double?,
    val mo6: Double?,
    val yr1: Double?,
    val yr2: Double?,
    val yr3: Double?,
    val yr5: Double?,
    val yr7: Double?,
    val yr10: Double?,
    val yr20: Double?,
    val yr30: Double?
)

fun getUsYieldData(): List<UsYield> {
    return try {
        httpGet(usYieldUrl, apiKeyParam)
    } catch (e: IOException) {
        appLogger.error(e.message)
        null
    }?.let {
        val records = CSVFormat.DEFAULT
            .withFirstRecordAsHeader()
            .withNullString("")
            .parse(it.reader())
        mapUsYieldRecords(records)
    } ?: emptyList()
}

fun getLocalUsYieldData(): List<UsYield> {
    val reader = ClassLoader
        .getSystemResourceAsStream("data/bonds/USTREASURY-YIELD.csv")
        .bufferedReader()
    val records = CSVFormat.DEFAULT
        .withFirstRecordAsHeader()
        .withNullString("")
        .parse(reader)
    return mapUsYieldRecords(records)
}

private fun mapUsYieldRecords(records: CSVParser): List<UsYield> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    return records.map {
        UsYield(
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