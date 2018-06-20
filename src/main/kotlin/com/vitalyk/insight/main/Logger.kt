package com.vitalyk.insight.main

import com.vitalyk.insight.Insight
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import ch.qos.logback.core.FileAppender
import ch.qos.logback.classic.LoggerContext



// Loggers are thread safe, so it is okay to make them static.
// Having several hundreds of Logger instances does not put
// any burden of consequence on the system, it's OK to have one
// per class.

// For use in standalone functions, UI classes and singletons.
// For non-UI classes, creating a logger per class inside a companion
// object is the preferred way to go.
val appLogger: Logger = LoggerFactory.getLogger(Insight::class.java)

fun getAppLog(): File? {
    var fileAppender: FileAppender<*>? = null
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    for (logger in context.loggerList) {
        val index = logger.iteratorForAppenders()
        while (index.hasNext()) {
            val enumElement = index.next()
            if (enumElement is FileAppender<*>) {
                fileAppender = enumElement
            }
        }
    }

    return fileAppender?.let { File(fileAppender.file) }
}