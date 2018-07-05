package com.vitalyk.insight.view

import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.ui.PollingQuoteList
import javafx.scene.layout.Priority
import tornadofx.*

class MarketMoversView : Fragment("Market Movers") {
    private val iex = Iex(HttpClients.main)

    private val quoteLists = listOf(
        PollingQuoteList("Most active", iex::getMostActive),
        PollingQuoteList("Gainers", iex::getGainers),
        PollingQuoteList("Losers", iex::getLosers)
    )

    override fun onDock() {
        super.onDock()

        quoteLists.forEach { it -> it.startUpdating() }
    }

    override fun onUndock() {
        super.onUndock()

        quoteLists.forEach { it -> it.stopUpdating() }
    }

    override val root = vbox {
        toolbar {
            button("Main").action { replaceWith(MainView::class) }
        }
        hbox {
            quoteLists.forEach { it -> this += it }
            vgrow = Priority.ALWAYS
        }
    }
}
