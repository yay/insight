package main

import org.quartz.impl.StdSchedulerFactory

private val appSchedulerFactory = StdSchedulerFactory()
val appScheduler = appSchedulerFactory.getScheduler()