package main

import org.quartz.impl.StdSchedulerFactory

val appSchedulerFactory = StdSchedulerFactory()
val appScheduler = appSchedulerFactory.getScheduler()