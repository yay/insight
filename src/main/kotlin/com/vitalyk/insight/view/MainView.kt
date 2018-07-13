package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.InfoFragment
import com.vitalyk.insight.fragment.NewsWatchlistFragment
import com.vitalyk.insight.fragment.ReutersFragment
import com.vitalyk.insight.helpers.getResourceAudioClip
import com.vitalyk.insight.helpers.newYorkZoneId
import com.vitalyk.insight.helpers.toReadableNumber
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.Watchlist
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.getAppLog
import com.vitalyk.insight.screener.getAdvancersDecliners
import com.vitalyk.insight.screener.getHighsLows
import com.vitalyk.insight.screener.loadAssetStatsJson
import com.vitalyk.insight.ui.browsebutton
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import tornadofx.*
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MainView : View("Insight") {
    private val iex = Iex(HttpClients.main)

    override val root = vbox {
        toolbar {
            button("Symbol Table").action { replaceWith(SymbolTableView::class) }
            button("Research").action { replaceWith(ResearchView::class) }
//            button("Watchlists").action { replaceWith(WatchlistView::class) }
            button("Economy").action { replaceWith(EconomyView::class) }
            button("Screener").action { replaceWith(ScreenerView::class) }
            browsebutton("Movers", "https://www.fool.com/market-movers/")
            browsebutton("Screener", "https://www.tradingview.com/screener/")
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
                val minPrice = 5.0
                tooltip("Advancers / Decliners\nMin price: $$minPrice")
                style {
                    padding = box(5.px)
                }
                launch {
                    while (isActive) {
                        if (isMarketHours()) {
                            // Ignore penny stocks:
                            getResourceAudioClip("/sounds/alerts/coin.wav").play()
                            getAdvancersDecliners(iex, minPrice)?.let {
                                val msg = "${it.advancerCount} adv / ${it.declinerCount} dec"
                                runLater {
                                    advancerProperty.value = msg
                                }
                            }
                        }
                        delay(1000 * 60)
                    }
                }
            }

            val breadthProperty = SimpleStringProperty()
            label(breadthProperty) {
                val minCap = 200_000_000L
                tooltip("New 52-week highs and lows\nMin market cap: ${minCap.toReadableNumber()}")
                style {
                    padding = box(5.px)
                }
                launch {
                    var stats: Map<String, Iex.AssetStats>? = null
                    while (isActive) {
                        if (isMarketHours()) {
                            if (stats == null) {
                                stats = loadAssetStatsJson()?.filter {
                                    it.value.marketCap >= minCap
                                }
                            }
                            getHighsLows(iex, stats)?.let {
                                val msg = "${it.highCount} hi / ${it.lowCount} lo"
                                runLater {
                                    breadthProperty.value = msg
                                }
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
                        val timeStr = ZonedDateTime
                            .now(newYorkZoneId)
                            .format(timeFormatter)
                        runLater {
                            timeProperty.value = timeStr
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