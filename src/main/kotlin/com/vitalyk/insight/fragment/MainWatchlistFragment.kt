package com.vitalyk.insight.fragment

import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.view.NewsFragment
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.*

class MainWatchlistFragment : Fragment() {
    val watchlist = WatchlistFragment(Watchlist.getOrPut("Main"))
    val newslist = NewsFragment()

    override val root = vbox {
        hgrow = Priority.ALWAYS
        splitpane(Orientation.VERTICAL) {
            vgrow = Priority.ALWAYS

            this += watchlist
            this += newslist.apply {
                toolbarVisible.set(false)
            }
            setDividerPositions(.7, .3)

            watchlist.table.onSelectionChange { data ->
                // May not be on FxApplicationThread here.
                if (data != null)
                    runLater {
                        newslist.symbol.value = data.symbol
                    }
            }
        }
    }
}