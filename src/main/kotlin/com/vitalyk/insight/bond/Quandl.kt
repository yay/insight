package com.vitalyk.insight.bond

import org.apache.commons.csv.CSVFormat
import java.text.SimpleDateFormat
import java.util.*

const val usYieldCurveUrl = "https://www.quandl.com/api/v3/datasets/USTREASURY/YIELD.csv?api_key="

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

fun getUsYieldCurveData(): List<UsYield> {
    val reader = ClassLoader.getSystemResourceAsStream("data/bonds/USTREASURY-YIELD.csv")
        .bufferedReader()
    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().withNullString("").parse(reader)
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