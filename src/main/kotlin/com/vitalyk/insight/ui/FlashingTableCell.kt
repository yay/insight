package com.vitalyk.insight.ui

import com.sun.javafx.scene.control.skin.LabeledText
import javafx.animation.FadeTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.control.TableCell
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.util.Duration
import java.util.*


class FlashingTableCell<S, T>(private val comparator: Comparator<T>? = null, alignment: Pos = Pos.CENTER_LEFT) : TableCell<S, T>() {

    private val bgIncrease = Background(BackgroundFill(INCREASE_HIGHLIGHT_COLOR, CornerRadii.EMPTY, Insets.EMPTY))
    private val bgDecrease = Background(BackgroundFill(DECREASE_HIGHLIGHT_COLOR, CornerRadii.EMPTY, Insets.EMPTY))
    private val bgChange = Background(BackgroundFill(HIGHLIGHT_COLOR, CornerRadii.EMPTY, Insets.EMPTY))

    private val background = BorderPane()
    private val labeledText = LabeledText(this)
    private val transition = FadeTransition(HIGHLIGHT_TIME, background) // animates opacity

    private val container = StackPane()

    private var prevItem: S? = null
    private var prevValue: T? = null


    init {
        labeledText.textProperty().bindBidirectional(textProperty())
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
        padding = Insets.EMPTY
        container.children.addAll(background, labeledText)
        container.alignment = alignment
        graphic = container
    }

    override fun updateItem(value: T?, empty: Boolean) {
        super.updateItem(value, empty)

        @Suppress("UNCHECKED_CAST")
        val currentItem = tableRow?.item as? S // data associated with the cell

        /*
         * We check that the value has been updated and that the row model/item
         * under the cell is the same. JavaFX table reuses cells so item is not
         * always the same!
         */
        val valueChanged = prevValue == null && value != null || value != null && prevValue!!.hashCode() != value.hashCode()
        val sameItem = currentItem == prevItem

//        if (empty || value == null) {
//            text = null
//            graphic = null
//        } else {
//            text = value.toString()
//        }

        if (valueChanged && sameItem) {

            if (comparator != null) {
                val compare = comparator.compare(value, prevValue)
                if (compare > 0) {
                    background.background = bgIncrease
                } else if (compare < 0) {
                    background.background = bgDecrease
                }
            } else {
                background.background = bgChange
            }

            transition.fromValue = 1.0
            transition.toValue = 0.0
            transition.cycleCount = 1
            transition.isAutoReverse = false
            transition.playFromStart()
        }

//        println("$prevValue, $value")
        prevValue = value
        prevItem = currentItem
    }

    companion object {
        private val INCREASE_HIGHLIGHT_COLOR = Color.rgb(0, 255, 0, 0.8)
        private val DECREASE_HIGHLIGHT_COLOR = Color.rgb(255, 0, 0, 0.8)
        private val HIGHLIGHT_COLOR = Color.rgb(255, 255, 0, 0.8)
        private val HIGHLIGHT_TIME = Duration.millis(300.0)
    }
}