package com.vitalyk.insight.view

import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import tornadofx.*


class NewsView : View("Headlines") {

    lateinit var tabPane: TabPane

    override val root = vbox {
        toolbar {
            button("Main") {
                action {
                    replaceWith(MainView::class)
                }
            }
        }

        tabPane = tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            vgrow = Priority.ALWAYS

            tab("News") {
                this += NewsFragment()
            }
        }
    }
}