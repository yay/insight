package com.vitalyk.insight.ui

import com.vitalyk.insight.iex.IexSymbols
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.control.ContextMenu
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
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

fun EventTarget.symbolfield(property: ObservableValue<String>, op: TextField.() -> Unit = {}) = textfield().apply {
    bind(property)

    textFormatter = TextFormatter<String> {
        it.text = it.text.toUpperCase()
        it
    }

    // TODO: validator
    val textfield = this
    val completeMenu = ContextMenu()
    val sb = StringBuilder()

    textProperty().onChange { value ->
        completeMenu.hide()
        val symbols = IexSymbols.complete(value)
        if (symbols.isNotEmpty()) {
            completeMenu.apply {
                items.clear()
                symbols.forEach {
                    val symbol = it.symbol
                    sb.append(symbol).append(" - ").append(it.name)
                    item(sb.toString()) {
                        setOnAction {
                            textfield.text = symbol
                        }
                    }
                    sb.setLength(0)
                }
                if (items.isNotEmpty()) {
                    show(textfield, Side.BOTTOM, 0.0, 0.0)
                }
            }
        }
    }

    op(this)
}