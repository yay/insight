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
import com.vitalyk.insight.main.httpGet
import com.vitalyk.insight.screener.getAdvancersDecliners
import com.vitalyk.insight.screener.getHighsLows
import com.vitalyk.insight.screener.loadAssetStatsJson
import com.vitalyk.insight.ui.browsebutton
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Side
import javafx.scene.control.ContextMenu
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
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
                    find(InfoFragment::class.java).apply {
                        setInfo("App Log", readText())
                        openModal()
                    }
                }
            }

            button("DePorre") {
                val menu = ContextMenu()
                menu.hide()

                val newStoryChime = getResourceAudioClip("/sounds/alerts/buzz.wav")
                val newStoryCount = SimpleIntegerProperty(0).apply {
                    onChange {
                        if (it > 0) newStoryChime.play()
                    }
                }
                label(newStoryCount) {
                    style {
                        borderWidth = multi(box(2.px))
                        borderRadius = multi(box(20.px))
                        borderColor = multi(box(Color.BLACK))
                        padding = box(0.px, 4.px, 0.px, 4.px)
                        fontFamily = "Menlo"
                        fontSize = .9.em
                    }
                }

                val rssFeed = "https://realmoney.thestreet.com/node/3203/feed"

                var oldDates = listOf<String>()
                launch {
                    while (isActive) {
                        val xml = httpGet(rssFeed)
                        val items = Jsoup.parse(xml, "", Parser.xmlParser()).select("item")
                        val dates = items.map { it.select("pubDate").text() }
                        val count = if (oldDates.isNotEmpty()) dates.minus(oldDates).size else 0
                        runLater {
                            newStoryCount.set(newStoryCount.get() + count)
                        }
                        oldDates = dates
                        delay(5 * 60 * 1000)
                    }
                }

                data class Story(
                    val date: String,
                    val title: String,
                    val link: String
                )

                action {
                    newStoryCount.set(0)
                    runAsyncWithProgress {
                        val xml = httpGet(rssFeed)
                        val items = Jsoup.parse(xml, "", Parser.xmlParser()).select("item")
                        items.map {
                            val date = it.select("pubDate").text().substringBeforeLast("0")
                            Story(date, it.select("title").text(), it.select("link").text())
                        }
                    } ui { stories ->
                        menu.items.clear()
                        stories.forEach {
                            menu.item(it.title).action {
                                runAsyncWithProgress {
                                    val html = httpGet(it.link)
                                    val content = Jsoup.parse(html).select(".content")
                                    val text = content.first().wholeText()
                                        .substringBefore("Get an email alert").trim()
                                    runLater {
                                        find(InfoFragment::class.java).apply {
                                            setInfo(it.title, it.date + "\n\n" + text)
                                            setSize(600, 600)
                                            openWindow()
                                        }
                                    }
                                }
                            }
                        }
                        if (menu.items.isNotEmpty()) {
                            menu.show(this, Side.BOTTOM, 0.0, 0.0)
                        }
                    }
                }
            }

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
                val minPrice = 5.0 // Ignore penny stocks
                tooltip("Advancers / Decliners\nMin price: $$minPrice")
                style {
                    padding = box(5.px)
                }
                launch {
                    while (isActive) {
                        if (isMarketHours()) {
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