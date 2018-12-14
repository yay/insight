package com.vitalyk.insight.view

import com.vitalyk.insight.ui.PlusButton
import javafx.scene.control.Alert
import javafx.util.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import tornadofx.*

class TestView : View("Test View") {
    override val root = PlusButton("Add Contact").apply {
        action {
            alert(Alert.AlertType.INFORMATION, "Click", "You just clicked me!")
        }
        GlobalScope.launch(Dispatchers.JavaFx) {
            delay(5000)
            circle.radiusProperty().animate(200.0, Duration(1000.0))
        }
    }
}