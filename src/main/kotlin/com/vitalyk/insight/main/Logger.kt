package com.vitalyk.insight.main

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// Loggers are thread safe so it is okay to make them static.
fun getAppLogger(): Logger = LoggerFactory.getLogger("insight")