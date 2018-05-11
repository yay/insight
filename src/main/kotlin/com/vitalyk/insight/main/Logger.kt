package com.vitalyk.insight.main

import com.vitalyk.insight.Insight
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// Loggers are thread safe, so it is okay to make them static.
// Having several hundreds of Logger instances does not put
// any burden of consequence on the system, it's OK to have one
// per class.

// For use in standalone functions, UI classes and singletons.
// For non-UI classes, creating a logger per class inside a companion
// object is the preferred way to go.
val appLogger: Logger = LoggerFactory.getLogger(Insight::class.java)