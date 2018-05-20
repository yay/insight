package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.NewsWatchlistFragment
import com.vitalyk.insight.fragment.ReutersFragment
import com.vitalyk.insight.helpers.newYorkZoneId
import com.vitalyk.insight.iex.Watchlist
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import tornadofx.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MainView : View("Insight") {
    override val root = vbox {
        toolbar {
            button("Symbol Table").action {
                replaceWith(SymbolTableView::class)
            }
            button("Research").action { replaceWith(ResearchView::class) }
//            button("Watchlists").action { replaceWith(WatchlistView::class) }
            button("Economy").action { replaceWith(EconomyView::class) }
            button("Movers").action { replaceWith(MarketMoversView::class) }
            button("News").action { replaceWith(NewsView::class) }

            spacer {}

            val timeProperty = SimpleStringProperty()
            label(timeProperty) {
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                style {
                    fontFamily = "Menlo"
                }
                launch {
                    while (isActive) {
                        delay(1000)
                        runLater {
                            timeProperty.value = ZonedDateTime
                                .now(newYorkZoneId)
                                .format(timeFormatter)
                        }
                    }
                }
            }
        }
        hbox {
            vgrow = Priority.ALWAYS

            this += ReutersFragment()
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
}