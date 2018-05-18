package com.vitalyk.insight.ui

import com.sun.javafx.scene.control.skin.LabeledText
import javafx.animation.FadeTransition
import javafx.animation.Interpolator.DISCRETE
import javafx.geometry.Insets
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Labeled
import javafx.scene.control.TableCell
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.util.Duration
import java.util.*


class ChangePulseTableCellGraphic(cell: Labeled) : StackPane() {
    private val bgIncrease = Background(BackgroundFill(INCREASE_COLOR, CornerRadii.EMPTY, Insets.EMPTY))
    private val bgDecrease = Background(BackgroundFill(DECREASE_COLOR, CornerRadii.EMPTY, Insets.EMPTY))
    private val bgChange = Background(BackgroundFill(CHANGE_COLOR, CornerRadii.EMPTY, Insets.EMPTY))

    private val background = BorderPane()
    private val labeledText = LabeledText(cell)
    private val transition = FadeTransition(PULSE_TIME, background) // animates opacity

    init {
        labeledText.textProperty().bindBidirectional(cell.textProperty())
        alignment = cell.alignment
        children.addAll(background, labeledText)
    }

    fun increase() {
        background.background = bgIncrease
        animate()
    }

    fun decrease() {
        background.background = bgDecrease
        animate()
    }

    fun change() {
        background.background = bgChange
        animate()
    }

    private fun animate() {
        transition.fromValue = 1.0
        transition.toValue = 0.0
        transition.cycleCount = 1
        transition.isAutoReverse = false
        transition.interpolator = DISCRETE
        transition.playFromStart()
    }

    companion object {
        private val INCREASE_COLOR = Color(0.802, 1.0, 0.68, 1.0)
        private val DECREASE_COLOR = Color(1.0, 0.747, 0.782, 1.0)
        private val CHANGE_COLOR = Color(1.0, 0.917, 0.68, 1.0)
        private val PULSE_TIME = Duration.millis(500.0)
    }
}

class ChangePulseTableCell<S, T>(
    private val comparator: Comparator<T>? = null,
    private val format: (T) -> String
) : TableCell<S, T>() {

    init {
        // We'll set both the `text` and the `graphic`, but only want
        // the graphic visible. The `textProperty` of the cell is going
        // to be used by the graphic.
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
    }

    // For TableColumn<S, T> (for example, TableColumn<Person, Int>("Age"))
    // TableRow.item is of type S (Person)
    // TableCell.item is of type T (Int)
    // To get the row model of a cell: tableRow?.item as? S
    // It's possible to listen for item changes using itemProperty().

    override fun updateItem(value: T?, empty: Boolean) {
        val prevValue = item

        super.updateItem(value, empty)

        if (empty || value == null) {
            text = null
            graphic = null
        } else if (tableRow != null) {
            text = format(value)

            val cellGraphic = tableColumn.properties.getOrPut(tableRow) {
                ChangePulseTableCellGraphic(this)
            } as ChangePulseTableCellGraphic
            graphic = cellGraphic

            val valueChanged = prevValue == null || prevValue.hashCode() != value.hashCode()

            if (valueChanged) {
                if (comparator != null) {
                    val compare = comparator.compare(value, prevValue)
                    if (compare > 0) {
                        cellGraphic.increase()
                    } else if (compare < 0) {
                        cellGraphic.decrease()
                    }
                } else {
                    cellGraphic.change()
                }
            }
        }
    }
}