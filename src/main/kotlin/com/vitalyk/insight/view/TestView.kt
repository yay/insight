package com.vitalyk.insight.view

import javafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import javafx.util.Duration
import tornadofx.*

class TestView : View("Test View") {
    override val root = stackpane {
        background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
        val normalColor = Color.GRAY
        val highlightColor = Color.WHITE
        circle {
            val circle = this
            radius = 40.0
            fill = Color.TRANSPARENT
            stroke = normalColor
            strokeDashArray.setAll(4.0, 4.0)
            padding = Insets(10.0)
            val cross = path {
                moveTo(-10.0, 0.0)
                lineTo(10.0, 0.0)
                moveTo(0.0, -10.0)
                lineTo(0.0, 10.0)
                stroke = normalColor
                isMouseTransparent = true
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
            setOnMouseClicked {
                alert(Alert.AlertType.INFORMATION, "Click", "You just clicked me!")
            }
        }
    }
}