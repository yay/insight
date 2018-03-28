package com.vitalyk.insight.view

import com.vitalyk.insight.iex.IexApi
import com.vitalyk.insight.ui.PollingQuoteList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*
import kotlinx.coroutines.experimental.javafx.JavaFx as UI

class QuoteView : View("Quotes") {
    val quoteLists = listOf(
        PollingQuoteList("Most active", IexApi::getMostActive),
        PollingQuoteList("Gainers", IexApi::getGainers),
        PollingQuoteList("Losers", IexApi::getLosers)
    )

    override fun onDock() {
        super.onDock()

        quoteLists.forEach { it -> it.startUpdating() }
    }

    override fun onUndock() {
        super.onUndock()

        quoteLists.forEach { it -> it.stopUpdating() }
    }

//    override fun onDelete() {
//        super.onDelete()
//
//        quoteLists.forEach { it -> it.stopUpdating() }
//    }

    override val root = vbox {
        hbox {
            spacing = 10.0
            padding = Insets(10.0)
            alignment = Pos.CENTER_LEFT

            button("Back") {
                setOnAction {
                    replaceWith(SymbolTableView::class)
                }
            }
        }
        hbox {
            quoteLists.forEach { it -> this += it }
            vgrow = Priority.ALWAYS
        }
    }
}
