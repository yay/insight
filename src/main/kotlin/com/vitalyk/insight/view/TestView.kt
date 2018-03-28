package com.vitalyk.insight.view

import javafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Path
import javafx.util.Duration
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import tornadofx.*

class CircleAddButton(tooltip: String? = null) : StackPane() {

    var normalColor = Color.GRAY
    var highlightColor = Color.WHITE

    private var button = this
    private val crossRatio = 0.25

    val circle = circle {
        val circle = this

        if (tooltip != null) tooltip(tooltip)

        radius = 40.0
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
                }
            }
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

    init {
        background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
        this += circle
    }
}

class TestView : View("Test View") {
    override val root = CircleAddButton("Add Contact").apply {
        circle.setOnMouseClicked {
            alert(Alert.AlertType.INFORMATION, "Click", "You just clicked me!")
        }
        launch(JavaFx) {
            delay(5000)
            circle.radiusProperty().animate(200.0, Duration(1000.0))
        }
    }
}