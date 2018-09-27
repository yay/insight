package com.vitalyk.insight.ui

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.TableCell
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ChangeBlinkTableCell<S, T>(
    private val comparator: Comparator<T>? = null,
    private val format: (T) -> String
) : TableCell<S, T>() {

    override fun updateItem(value: T?, empty: Boolean) {
        val prevValue = item

        super.updateItem(value, empty)

        val row = tableRow
        val rowObject = row?.item
        val oldRowObject = tableColumn.properties[row]

        if (empty || value == null) {
            text = null
            graphic = null
        } else if (prevValue != value) {
            text = format(value)

            // TableView reuses rows, so row object may not be the same.
            if (rowObject == oldRowObject) {
                if (comparator == null) {
                    background = Background(BackgroundFill(CHANGE_COLOR, CornerRadii.EMPTY, Insets.EMPTY))
                } else if (prevValue != null) {
                    val compare = comparator.compare(value, prevValue)
                    if (compare > 0) {
                        background = Background(BackgroundFill(INCREASE_COLOR_BG, CornerRadii.EMPTY, Insets.EMPTY))
                        textFill = INCREASE_COLOR_TEXT
                    } else if (compare < 0) {
                        background = Background(BackgroundFill(DECREASE_COLOR_BG, CornerRadii.EMPTY, Insets.EMPTY))
                        textFill = DECREASE_COLOR_TEXT
                    }
                }
            }

            launch {
                delay(BLINK_TIME)
                Platform.runLater { background = null }
            }
        }

        tableColumn.properties[row] = rowObject
    }

    companion object {
        private val INCREASE_COLOR_TEXT = Color.GREEN
        private val DECREASE_COLOR_TEXT = Color(0.86, 0.18, 0.09, 1.0)
        private val INCREASE_COLOR_BG = Color(0.802, 1.0, 0.68, 1.0)
        private val DECREASE_COLOR_BG = Color(1.0, 0.747, 0.782, 1.0)
        private val CHANGE_COLOR = Color(1.0, 0.917, 0.68, 1.0)
        private const val BLINK_TIME = 1000
    }
}