package main

import org.apache.commons.csv.CSVFormat
import org.quartz.impl.StdSchedulerFactory
import java.io.File
import java.text.SimpleDateFormat

fun main(args: Array<String>) {

    fetchEndOfDayData()

//    Settings.load(AppSettings)
//    Settings.saveOnShutdown(AppSettings)
//
//    // The app won't exit while the scheduler is running.
//    val appSchedulerFactory = StdSchedulerFactory()
//    val appScheduler = appSchedulerFactory.getScheduler()
//
//    appScheduler.start()
//
//    scheduleEndOfDayFetcher(appScheduler)

}