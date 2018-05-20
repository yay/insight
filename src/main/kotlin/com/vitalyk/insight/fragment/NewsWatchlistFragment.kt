package com.vitalyk.insight.fragment

import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.view.NewsFragment
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.*

class NewsWatchlistFragment(watchlist: Watchlist) : Fragment() {

    constructor(name: String) : this(Watchlist.getOrPut(name))

    val listFragment = WatchlistFragment(watchlist)
    val newsFragment = NewsFragment()

    override val root = vbox {
        hgrow = Priority.ALWAYS
        splitpane(Orientation.VERTICAL) {
            vgrow = Priority.ALWAYS

            this += listFragment
            this += newsFragment.apply {
                toolbarVisible.set(false)
            }
            setDividerPositions(.7, .3)

            listFragment.table.onSelectionChange { data ->
                // May not be on FxApplicationThread here.
                if (data != null)
                    runLater {
                        newsFragment.symbol.value = data.symbol
                    }
            }
        }
    }
}