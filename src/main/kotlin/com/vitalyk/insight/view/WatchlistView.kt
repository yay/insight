package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.WatchlistFragment
import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.ui.toolbox
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.*

class WatchlistView : View("Watchlists") {

    private val watchlist = WatchlistFragment(Watchlist.getOrPut("Main"))
    private val newslist = NewsFragment()

    private val tabpane = tabpane {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        tab("Main") {
            this += watchlist
        }
    }

    override val root = vbox {
        toolbox {
            button("Main").action { replaceWith(MainView::class) }
        }
        splitpane(Orientation.VERTICAL) {
            vgrow = Priority.ALWAYS
            this += tabpane
            this += newslist.apply {
                toolbox.hide()
            }
            setDividerPositions(.6, .4)

            watchlist.table.onSelectionChange { data ->
                if (data != null)
                // May not be on FxApplicationThread here so ...
                    runLater {
                        newslist.symbol.value = data.symbol
                    }
            }
        }
    }
}
