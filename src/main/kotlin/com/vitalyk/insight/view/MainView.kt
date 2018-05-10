package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.MainWatchlistFragment
import com.vitalyk.insight.fragment.ReutersFragment
import com.vitalyk.insight.ui.toolbox
import javafx.scene.layout.Priority
import tornadofx.*

class MainView : View("Insight") {
    override val root = vbox {
        toolbox(border = false) {
            button("Symbol Table").action {
                replaceWith(SymbolTableView::class)
            }
            button("Research").action { replaceWith(ResearchView::class) }
            button("Watchlists").action { replaceWith(WatchlistView::class) }
            button("Stats").action { replaceWith(StatsView::class) }
            button("Movers").action { replaceWith(MarketMoversView::class) }
            button("News").action { replaceWith(NewsView::class) }
        }
        splitpane {
            vgrow = Priority.ALWAYS

            this += ReutersFragment()
            this += MainWatchlistFragment()
            setDividerPositions(.3, .7)
        }
    }
}