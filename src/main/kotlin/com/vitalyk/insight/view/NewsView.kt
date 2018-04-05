package com.vitalyk.insight.view

import com.vitalyk.insight.ui.toolbox
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import tornadofx.*


class NewsView : View("Headlines") {

    lateinit var tabPane: TabPane

    override val root = vbox {
        toolbox {
            button("Back") {
                setOnAction {
                    replaceWith(SymbolTableView::class)
                }
            }
        }

        tabPane = tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            vgrow = Priority.ALWAYS

            tab("News") {
                this += NewsList()
            }
        }
    }
}