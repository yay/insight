package com.vitalyk.insight.ui

import javafx.geometry.Pos
import javafx.scene.control.TableCell
import javafx.scene.paint.Color

class PercentChangeTableCell<S> : TableCell<S, Double>() {

    init {
        alignment = Pos.CENTER_RIGHT
    }

    override fun updateItem(value: Double?, empty: Boolean) {
        super.updateItem(value, empty)

        if (empty || value == null) {
            text = null
            graphic = null
        } else {
            text = "%.2f%%".format(value * 100.0)

            textFill = when {
                value > 0 -> INCREASE_COLOR_TEXT
                value < 0 -> DECREASE_COLOR_TEXT
                else -> NO_CHANGE_COLOR_TEXT
            }
        }
    }

    companion object {
        private val INCREASE_COLOR_TEXT = Color.GREEN
        private val DECREASE_COLOR_TEXT = Color(0.86, 0.18, 0.09, 1.0)
        private val NO_CHANGE_COLOR_TEXT = Color.BLACK
    }
}