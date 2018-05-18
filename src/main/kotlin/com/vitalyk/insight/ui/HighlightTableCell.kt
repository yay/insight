package com.vitalyk.insight.ui

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.TableCell
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.*

class HighlightTableCell<S, T>(
    private val comparator: Comparator<T>? = null,
    private val format: (T) -> String
) : TableCell<S, T>() {

    override fun updateItem(value: T?, empty: Boolean) {
        val prevValue = item

        super.updateItem(value, empty)

        if (empty || value == null) {
            text = null
            graphic = null
        } else if (tableRow != null) {
            text = format(value)

            val valueChanged = prevValue == null || prevValue.hashCode() != value.hashCode()

            if (valueChanged) {
                if (comparator != null) {
                    val compare = comparator.compare(value, prevValue)
                    if (compare > 0) {
                        background = Background(BackgroundFill(INCREASE_HIGHLIGHT_COLOR, CornerRadii.EMPTY, Insets.EMPTY))
                        textFill = Color.GREEN
                    } else if (compare < 0) {
                        background = Background(BackgroundFill(DECREASE_HIGHLIGHT_COLOR, CornerRadii.EMPTY, Insets.EMPTY))
                        textFill = Color(0.86, 0.18, 0.09, 1.00)
                    }
                } else {
                    background = Background(BackgroundFill(HIGHLIGHT_COLOR, CornerRadii.EMPTY, Insets.EMPTY))
                }
                launch {
                    delay(HIGHLIGHT_TIME)
                    Platform.runLater { background = null }
                }
            }
        }
    }

    companion object {
//        private val INCREASE_HIGHLIGHT_COLOR = Color(0.80, 1.00, 0.68, 1.00)
//        private val DECREASE_HIGHLIGHT_COLOR = Color(1.00, 0.75, 0.78, 1.00)
//        private val INCREASE_HIGHLIGHT_COLOR = Color(0.67, 0.82, 0.36, 1.00)
        private val INCREASE_HIGHLIGHT_COLOR = Color(0.51, 0.78, 0.53, 1.00)
//        private val DECREASE_HIGHLIGHT_COLOR = Color(0.95, 0.35, 0.29, 1.00)
        private val DECREASE_HIGHLIGHT_COLOR = Color(0.99, 0.54, 0.42, 1.00)
        private val HIGHLIGHT_COLOR = Color(0.97, 0.78, 0.18, 1.00)
        private const val HIGHLIGHT_TIME = 1000
    }
}