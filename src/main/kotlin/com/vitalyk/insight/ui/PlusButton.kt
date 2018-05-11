package com.vitalyk.insight.ui

import javafx.geometry.Insets
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Path
import javafx.util.Duration
import tornadofx.*

class PlusButton(tooltip: String? = null) : StackPane() {

    var normalColor = Color.GRAY
    var highlightColor = Color.BLACK

    private var button = this
    private val crossRatio = 0.35

    val circle = circle {
        val circle = this

        if (tooltip != null) tooltip(tooltip)

        radius = 14.0
        fill = Color.TRANSPARENT
        stroke = normalColor
        strokeDashArray.setAll(4.0, 4.0)
        padding = Insets(10.0)

        var cross = getCross(this)

        radiusProperty().onChange {
            button.children.remove(cross)
            cross = getCross(circle)
            button += cross
        }

        onHover {
            timeline {
                keyframe(Duration.seconds(0.2)) {
                    keyvalue(circle.strokeProperty(), if (it) highlightColor else normalColor)
                    keyvalue(cross.scaleXProperty(), if (it) 1.2 else 1.0)
                    keyvalue(cross.scaleYProperty(), if (it) 1.2 else 1.0)
                    keyvalue(cross.strokeProperty(), if (it) highlightColor else normalColor)
                    keyvalue(cross.rotateProperty(), if (it) 90.0 else 0.0)
                }
            }
        }

        fun pressButton(isDown: Boolean) {
            timeline {
                keyframe(Duration.seconds(0.1)) {
                    keyvalue(circle.scaleXProperty(), if (isDown) 0.8 else 1.0)
                    keyvalue(circle.scaleYProperty(), if (isDown) 0.8 else 1.0)
                    keyvalue(cross.scaleXProperty(), if (isDown) 0.8 else 1.2)
                    keyvalue(cross.scaleYProperty(), if (isDown) 0.8 else 1.2)
                }
            }
        }

        setOnMousePressed {
            pressButton(true)
        }
        setOnMouseReleased {
            pressButton(false)
        }
    }

    fun getCross(circle: Circle): Path = path {
        val r = circle.radius * crossRatio
        moveTo(-r, 0.0)
        lineTo(r, 0.0)
        moveTo(0.0, -r)
        lineTo(0.0, r)
        stroke = if (circle.isHover) highlightColor else normalColor
        isMouseTransparent = true
    }

    fun action(op: () -> Unit) = circle.setOnMouseClicked { op() }

    init {
//        background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
        this += circle
    }
}