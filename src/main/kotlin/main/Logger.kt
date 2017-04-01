package main

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun getAppLogger(): Logger = LoggerFactory.getLogger(AppSettings.defaultLogger)