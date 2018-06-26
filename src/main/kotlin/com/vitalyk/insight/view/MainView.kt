package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.InfoFragment
import com.vitalyk.insight.fragment.NewsWatchlistFragment
import com.vitalyk.insight.fragment.ReutersFragment
import com.vitalyk.insight.helpers.browseTo
import com.vitalyk.insight.helpers.newYorkZoneId
import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.main.getAppLog
import com.vitalyk.insight.screener.getAdvancersDecliners
import com.vitalyk.insight.screener.getHighsLows
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import tornadofx.*
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MainView : View("Insight") {
    override val root = vbox {
        toolbar {
            button("Symbol Table").action { replaceWith(SymbolTableView::class) }
            button("Research").action { replaceWith(ResearchView::class) }
//            button("Watchlists").action { replaceWith(WatchlistView::class) }
            button("Economy").action { replaceWith(EconomyView::class) }
            button("Movers").action {
                browseTo("https://www.fool.com/market-movers/")
            }
            button("Screener").action { replaceWith(ScreenerView::class) }
            button("Log").action {
                getAppLog()?.apply {
                    find(InfoFragment::class.java).setInfo("App Log", readText()).openModal()
                }
            }
//            button("Notify").action {
//                // notification("Title", "Message") {
//                //     showAndWait()
//                // }
//                // notification("Urgent", "Act now!").show()
//                // notification("Title", "Message").action {
//                // }
//                val tray = TrayNotification().apply {
//                    title = "Hello"
//                    message = "Vitaly"
//                    notification = Notifications.INFORMATION
//                    showAndWait()
//                }
//            }
//            button("Quotes").action { replaceWith(MarketMoversView::class) }
//            button("News").action { replaceWith(NewsView::class) }

            spacer {}

            // Simplistic check
            fun isMarketHours(): Boolean {
                val datetime = LocalDateTime.now(ZoneId.of("America/New_York"))
                val day = datetime.dayOfWeek
                val isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
                val hour = datetime.hour
                val minute = datetime.minute
                return !isWeekend && (hour > 9 || (hour == 9 && minute >= 30)) && hour < 16
            }

            val advancerProperty = SimpleStringProperty()
            label(advancerProperty) {
                tooltip("Advancers / Decliners")
                style {
                    padding = box(5.px)
                    borderColor = multi(box(c("#000")))
                    borderWidth = multi(box(1.px))
                }
                launch {
                    while(isActive) {
                        if (isMarketHours()) {
                            getAdvancersDecliners()?.let {
                                val msg = "${it.advancerCount} ↑ / ${it.declinerCount} ↓"
                                runLater { advancerProperty.value = msg }
                            }
                        }
                        delay(1000 * 60)
                    }
                }
            }

            val breadthProperty = SimpleStringProperty()
            label(breadthProperty) {
                tooltip("New 52-week highs and lows")
                style {
                    padding = box(5.px)
                    borderColor = multi(box(c("#000")))
                    borderWidth = multi(box(1.px))
                }
                launch {
                    while(isActive) {
                        if (isMarketHours()) {
                            getHighsLows()?.let {
                                val msg = "${it.highCount} ↑ / ${it.lowCount} ↓"
                                runLater { breadthProperty.value = msg }
                            }
                        }
                        delay(1000 * 60)
                    }
                }
            }

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