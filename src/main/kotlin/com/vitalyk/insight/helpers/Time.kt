package com.vitalyk.insight.helpers

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.*

// https://stackoverflow.com/questions/5175728/how-to-get-the-current-date-time-in-java
val newYorkTimeZone = TimeZone.getTimeZone("America/New_York")
val newYorkZoneId = ZoneId.of("America/New_York")

fun getLastWorkDay(zoneId: String = "America/New_York"): LocalDate {
    val now = LocalDate.now(ZoneId.of(zoneId))
    return when (DayOfWeek.of(now.get(ChronoField.DAY_OF_WEEK))) {
        DayOfWeek.MONDAY -> now.minus(3, ChronoUnit.DAYS)
        DayOfWeek.SUNDAY -> now.minus(2, ChronoUnit.DAYS)
        else -> now.minus(1, ChronoUnit.DAYS)
    }
}