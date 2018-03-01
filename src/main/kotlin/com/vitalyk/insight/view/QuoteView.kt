package com.vitalyk.insight.view

import com.vitalyk.insight.iex.IexApi
import com.vitalyk.insight.ui.QuoteList
import tornadofx.*
import kotlinx.coroutines.experimental.javafx.JavaFx as UI

class QuoteView : View("Quotes") {
    override val root = hbox {
        this += QuoteList("Most active", IexApi::getMostActive)
        this += QuoteList("Gainers", IexApi::getGainers)
        this += QuoteList("Losers", IexApi::getLosers)
    }
}
