package sandbox

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun run() {
    // http://stackoverflow.com/questions/20387881/how-to-run-certain-task-every-day-at-a-particular-time-using-scheduledexecutorse
    val localNow = LocalDateTime.now()
    val currentZone = ZoneId.of("America/Los_Angeles")
    val zonedNow = ZonedDateTime.of(localNow, currentZone)
    var zonedNext5: ZonedDateTime
    zonedNext5 = zonedNow.withHour(5).withMinute(0).withSecond(0)
    if (zonedNow > zonedNext5)
        zonedNext5 = zonedNext5.plusDays(1)

    val duration = Duration.between(zonedNow, zonedNext5)
    val initialDelay = duration.getSeconds()

    val scheduler = Executors.newScheduledThreadPool(1)
    scheduler.scheduleAtFixedRate(object: TimerTask() {
        override fun run() {

        }
    }, initialDelay, (24 * 60 * 60).toLong(), TimeUnit.SECONDS)
}