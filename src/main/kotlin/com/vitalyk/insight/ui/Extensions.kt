package com.vitalyk.insight.ui

import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import tornadofx.*

fun EventTarget.toolbox(border: Boolean = true, op: HBox.() -> Unit = {}): HBox {
    val hbox = HBox().apply {
        spacing = 10.0
        padding = Insets(10.0)
        alignment = Pos.CENTER_LEFT
        style {
            val stops = listOf(
                Stop(0.0, Color(0.98, 0.98, 0.98, 1.0)),
                Stop(1.0, Color(0.91, 0.91, 0.91, 1.0))
            )
            backgroundColor.add(LinearGradient(0.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE, stops))
            if (border) {
                borderColor += box(
                    top = Color.TRANSPARENT,
                    right = Color.TRANSPARENT,
                    bottom = Color(0.71, 0.71, 0.71, 1.0),
                    left = Color.TRANSPARENT
                )
            }
        }
    }
    return opcr(this, hbox, op)
}