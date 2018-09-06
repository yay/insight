package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.NewsWatchlistFragment
import com.vitalyk.insight.iex.Watchlist
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import tornadofx.*

class WatchlistView : View("Watchlists") {
    override val root = vbox {
        toolbar {
            button("Main View").action { replaceWith(MainView::class) }
        }
        tabpane {
            hgrow = Priority.ALWAYS
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            tab("Main") {
                this += NewsWatchlistFragment("Main")
            }
            tab("Indexes") {
                this += NewsWatchlistFragment(Watchlist.getOrPut("Indexes").apply {
                    addSymbols(listOf(
                        "SPY", // SPDR S&P 500
                        "DIA", // SPDR Dow Jones Industrial Average
                        "QQQ", // PowerShares QQQ Trust (tracks Nasdaq 100 Index)
                        "MDY", // SPDR S&P Midcap 400
                        "IWM", // iShares Russell 2000 (small caps)
                        "IFA", // iShares MSCI EAFE (developed markets: UK, France, German, Japan, ...)
                        "EEM"  // iShares MSCI Emerging Markets (China, Korea, Taiwan, Brazil, ...)
                    ))
                })
            }
        }
    }
}