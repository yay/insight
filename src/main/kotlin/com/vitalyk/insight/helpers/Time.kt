package com.vitalyk.insight.helpers

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

fun getLastWorkDay(zoneId: String = "America/New_York"): LocalDate {
    val now = LocalDate.now(ZoneId.of(zoneId))
    return when (DayOfWeek.of(now.get(ChronoField.DAY_OF_WEEK))) {
        DayOfWeek.MONDAY -> now.minus(3, ChronoUnit.DAYS)
        DayOfWeek.SUNDAY -> now.minus(2, ChronoUnit.DAYS)
        else -> now.minus(1, ChronoUnit.DAYS)
    }
}