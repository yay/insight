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

class ChangeBlinkTableCell<S, T>(
    private val comparator: Comparator<T>? = null,
    private val format: (T) -> String
) : TableCell<S, T>() {

    override fun updateItem(value: T?, empty: Boolean) {
        val prevValue = item

        super.updateItem(value, empty)

        val rowObject = tableRow?.item
        val oldRowObject = tableColumn.properties[tableRow]

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
                        background = Background(BackgroundFill(INCREASE_COLOR, CornerRadii.EMPTY, Insets.EMPTY))
                        textFill = Color.GREEN
                    } else if (compare < 0) {
                        background = Background(BackgroundFill(DECREASE_COLOR, CornerRadii.EMPTY, Insets.EMPTY))
                        textFill = Color(0.86, 0.18, 0.09, 1.00)
                    }
                }
            }

            launch {
                delay(BLINK_TIME)
                Platform.runLater { background = null }
            }
        }

        tableColumn.properties[tableRow] = rowObject
    }

    companion object {
        private val INCREASE_COLOR = Color(0.802, 1.0, 0.68, 1.0)
        private val DECREASE_COLOR = Color(1.0, 0.747, 0.782, 1.0)
        private val CHANGE_COLOR = Color(1.0, 0.917, 0.68, 1.0)
        private const val BLINK_TIME = 1000
    }
}