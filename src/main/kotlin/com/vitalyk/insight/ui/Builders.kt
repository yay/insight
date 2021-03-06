package com.vitalyk.insight.ui

import com.vitalyk.insight.iex.IexSymbols
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.channels.actor
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

// TODO: WIP - onAction calls addSymbol() in WatchlistFragment, which gets the old symbol.value
fun EventTarget.symbolcombo(property: ObservableValue<String>? = null,
                            op: ComboBox<String>.() -> Unit = {},
                            onAction: (String) -> Unit = {}) = combobox<String>().apply {
    if (property != null) {
        editor.bind(property)
    }

    val historySize = 10
    val editor: TextField = editor
    val comboItems = items
    isEditable = true

    editor.textFormatter = TextFormatter<String> {
        it.text = it.text.toUpperCase()
        it
    }

    fun addToHistory(symbol: String) {
        if (!comboItems.contains(symbol)) {
            if (comboItems.size >= historySize) {
                comboItems.removeAt(comboItems.lastIndex)
            }
            comboItems.add(0, symbol)
        }
    }

    // TODO: validator
    val completeMenu = ContextMenu()
    val sb = StringBuilder()

    editor.textProperty().onChange { value ->
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
                            editor.text = symbol
                            addToHistory(symbol)
                            onAction(symbol)
                        }
                    }
                    sb.setLength(0)
                }
                if (items.isNotEmpty()) {
                    show(editor, Side.BOTTOM, 0.0, 0.0)
                }
            }
        }
    }

    onKeyReleased = EventHandler { event ->
        if (event.code == KeyCode.ENTER) {
            val symbol = value
            println(symbol)
            addToHistory(symbol)
            onAction(symbol)
        }
    }

    promptText = "Enter Symbol"

    op(this)
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

    promptText = "Enter Symbol"

    op(this)
}


fun EventTarget.browsebutton(text: String = "", url: String, op: Button.() -> Unit = {}) = Button(text).also {
    it.action { Desktop.getDesktop().browse(URI(url)) }
    addChildIfPossible(it)
    op(it)
}

/**
 * Performs the given action when the button is clicked, while ignoring subsequent
 * clicks until the action completes.
 * Only a single concurrent operation will be performed at a time, even if multiple
 * buttons were clicked.
 */
fun Button.onClickActor(action: suspend (MouseEvent) -> Unit) {
    val eventActor = GlobalScope.actor<MouseEvent>(Dispatchers.JavaFx) {
        for (event in channel) action(event)
    }
    onMouseClicked = EventHandler { event ->
        // `offer` sends an element to the actor immediately, if it is possible,
        // or discards an element otherwise.
        eventActor.offer(event)
    }
}