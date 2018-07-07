package com.vitalyk.insight.ui

import com.vitalyk.insight.iex.IexSymbols
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import tornadofx.*
import java.awt.Desktop
import java.net.URI

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

/**
 * `onAction` is called either when the user pressed the Enter key
 * or selected a symbol from the autocomplete menu.
 */
fun EventTarget.symbolfield(property: ObservableValue<String>? = null,
                            op: TextField.() -> Unit = {},
                            onAction: (String) -> Unit = {}) = textfield().apply {
    if (property != null) {
        bind(property)
    }

    textFormatter = TextFormatter<String> {
        it.text = it.text.toUpperCase()
        it
    }

    // TODO: validator
    val textfield = this
    val completeMenu = ContextMenu()
    val sb = StringBuilder()

    textProperty().onChange { value ->
        if (!isFocused) return@onChange
        completeMenu.hide()
        val symbols = IexSymbols.complete(value)
        if (symbols.isNotEmpty()) {
            completeMenu.apply {
                items.clear()
                symbols.forEach {
                    val symbol = it.symbol
                    sb.append(symbol).append(" - ").append(it.name)
                    item(sb.toString()) {
                        action {
                            textfield.text = symbol
                            onAction(symbol)
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

    onKeyReleased = EventHandler { event ->
        if (event.code == KeyCode.ENTER) {
            onAction(text)
        }
    }

    maxWidth = 100.0
    promptText = "Enter Symbol"

    op(this)
}


fun ToolBar.browsebutton(text: String = "", url: String, op: Button.() -> Unit = {}) = Button(text).also {
    it.graphic = MaterialIconView(MaterialIcon.EXPLORE).apply {
        glyphSize = 16.0
    }
    items += it
    it.action { Desktop.getDesktop().browse(URI(url)) }
    op(it)
}

fun EventTarget.browsebutton(text: String = "", url: String, op: Button.() -> Unit = {}) = Button(text).also {
    it.graphic = MaterialIconView(MaterialIcon.EXPLORE).apply {
        glyphSize = 16.0
    }
    it.action { Desktop.getDesktop().browse(URI(url)) }
    addChildIfPossible(it)
    op(it)
}