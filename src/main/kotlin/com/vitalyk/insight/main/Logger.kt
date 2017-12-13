package com.vitalyk.insight.main

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// TODO: Is this called from other threads? If so, what are the implications?
fun getAppLogger(): Logger = LoggerFactory.getLogger(AppSettings.defaultLogger)