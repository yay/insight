package com.vitalyk.dataviz.examples

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

class AxisApp : Application() {
    override fun start(stage: Stage) {
        val hbox = Pane().apply {

        }
        stage.scene = Scene(hbox, 800.0, 800.0)
    }
}

fun main(args: Array<String>) {
    Application.launch(AxisApp::class.java)
}