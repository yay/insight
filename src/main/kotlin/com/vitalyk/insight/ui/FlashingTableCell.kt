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


class FlashingTableCellGraphic(cell: Labeled) : StackPane() {
    val bgIncrease = Background(BackgroundFill(INCREASE_HIGHLIGHT_COLOR, CornerRadii.EMPTY, Insets.EMPTY))
    val bgDecrease = Background(BackgroundFill(DECREASE_HIGHLIGHT_COLOR, CornerRadii.EMPTY, Insets.EMPTY))
    val bgChange = Background(BackgroundFill(HIGHLIGHT_COLOR, CornerRadii.EMPTY, Insets.EMPTY))

    val background = BorderPane()
    val labeledText = LabeledText(cell)
    val transition = FadeTransition(HIGHLIGHT_TIME, background) // animates opacity

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
//        private val INCREASE_HIGHLIGHT_COLOR = Color(0.80, 1.00, 0.68, 1.00)
//        private val DECREASE_HIGHLIGHT_COLOR = Color(1.00, 0.75, 0.78, 1.00)
        private val INCREASE_HIGHLIGHT_COLOR = Color.rgb(0, 255, 0, 0.8)
        private val DECREASE_HIGHLIGHT_COLOR = Color.rgb(255, 0, 0, 0.8)
        private val HIGHLIGHT_COLOR = Color.rgb(255, 255, 0, 0.8)
        private val HIGHLIGHT_TIME = Duration.millis(500.0)
    }
}

class FlashingTableCell<S, T>(
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
                FlashingTableCellGraphic(this)
            } as FlashingTableCellGraphic
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