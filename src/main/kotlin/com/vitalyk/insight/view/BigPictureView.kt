package com.vitalyk.insight.view

import javafx.scene.Parent
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.layout.Priority
import tornadofx.*

class BigPictureView : View("Big Picture") {
    override val root = vbox {
        barchart("S&P 500", CategoryAxis(), NumberAxis()) {
            isLegendVisible = false
            vgrow = Priority.ALWAYS

            series("Distribution days") {
                data("03/21/2018", -1.23)
                data("03/23/2018", -0.43)
                data("03/24/2018", -1.85)
            }
        }
        barchart("Nasdaq", CategoryAxis(), NumberAxis()) {
            isLegendVisible = false
            vgrow = Priority.ALWAYS

            series("Distribution days") {
                data("03/21/2018", -1.23)
                data("03/23/2018", -0.43)
                data("03/24/2018", -1.85)
            }
        }
        barchart("Dow 30", CategoryAxis(), NumberAxis()) {
            isLegendVisible = false
            vgrow = Priority.ALWAYS

            series("Distribution days") {
                data("03/21/2018", -1.23)
                data("03/23/2018", -0.43)
                data("03/24/2018", -1.85)
            }
        }
        barchart("Russel 2000", CategoryAxis(), NumberAxis()) {
            isLegendVisible = false
            vgrow = Priority.ALWAYS

            series("Distribution days") {
                data("03/21/2018", -1.23)
                data("03/23/2018", -0.43)
                data("03/24/2018", -1.85)
            }
        }
    }
}