package main

import org.quartz.impl.StdSchedulerFactory

fun main(args: Array<String>) {

    Settings.load(AppSettings)
    Settings.saveOnShutdown(AppSettings)

    // The app won't exit while the scheduler is running.
    val appSchedulerFactory = StdSchedulerFactory()
    val appScheduler = appSchedulerFactory.scheduler

    appScheduler.start()

    scheduleEndOfDayFetcher(appScheduler)

}