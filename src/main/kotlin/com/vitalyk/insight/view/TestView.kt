package com.vitalyk.insight.view

import com.vitalyk.insight.ui.PlusButton
import javafx.scene.control.Alert
import javafx.util.Duration
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import tornadofx.*

class TestView : View("Test View") {
    override val root = PlusButton("Add Contact").apply {
        action {
            alert(Alert.AlertType.INFORMATION, "Click", "You just clicked me!")
        }
        launch(JavaFx) {
            delay(5000)
            circle.radiusProperty().animate(200.0, Duration(1000.0))
        }
    }
}