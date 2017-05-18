package main

import org.quartz.impl.StdSchedulerFactory

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