package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.InfoFragment
import com.vitalyk.insight.fragment.ReutersFragment
import com.vitalyk.insight.helpers.browseTo
import com.vitalyk.insight.helpers.getResourceAudioClip
import com.vitalyk.insight.helpers.newYorkZoneId
import com.vitalyk.insight.helpers.toReadableNumber
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.IexSymbols
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.insight.main.getAppLog
import com.vitalyk.insight.screener.HighsLows
import com.vitalyk.insight.screener.getAdvancersDecliners
import com.vitalyk.insight.screener.getHighsLows
import com.vitalyk.insight.yahoo.getDistributionInfo
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.control.Alert
import javafx.scene.input.MouseButton
import javafx.scene.layout.Priority
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tornadofx.*
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MainView : View("Insight") {
    private val iex = Iex(HttpClients.main)

    private fun research(symbol: String) {
        find(ResearchView::class).let {
            it.fetch(symbol)
            root.replaceWith(it.root)
        }
    }

    override val root = vbox {
        toolbar {
            button("Research").action { replaceWith(ResearchView::class) }
            button("Economy").action { replaceWith(EconomyView::class) }
            menubutton("Tools") {
                item("Share Repurchase").action {
                    find(BuybackView::class).openModal()
                }
                item("Distribution Days").action {
                    GlobalScope.launch {
                        val info = getDistributionInfo()
                        runLater {
                            alert(Alert.AlertType.INFORMATION,
                                "Market Distribution Days",
                                info,
                                owner = primaryStage
                            )
                        }
                    }
                }
                item("Movers (fool.com)").action {
                    browseTo("https://www.fool.com/market-movers/")
                }
                item("Screener (tradingview.com)").action {
                    browseTo("https://www.tradingview.com/screener/")
                }
            }
            button("Screener") {
                isDisable = true
                GlobalScope.launch {
                    while (isActive && IexSymbols.assetStats == null) {
                        delay(1000)
                    }
                    getResourceAudioClip("/sounds/alerts/message.wav").play()
                    runLater { isDisable = false }
                }

                action {
                    IexSymbols.assetStats?.let {
                        replaceWith(ScreenerView::class)
                    }
                }
            }
            button("Watchlists").action { replaceWith(WatchlistView::class) }

            spacer {}

            // Simplistic check
            @Suppress("NAME_SHADOWING")
            fun isMarketHours(datetime: LocalDateTime? = null): Boolean {
                val datetime = datetime ?: LocalDateTime.now(ZoneId.of("America/New_York"))
                val day = datetime.dayOfWeek
                val isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
                val hour = datetime.hour
                val minute = datetime.minute
                return !isWeekend && (hour > 9 || (hour == 9 && minute >= 30)) && hour < 16
            }

            label {
                data class ChartPoint(
                    val time: LocalDateTime,
                    val change: Int // advancers - decliners
                )

                val label = this
                val minPrice = 2.0 // Ignore penny stocks
                val interval = 60 * 1000
                val day = 6.5 * 60 * 60 * 1000
                val maxIntervals = day / interval
                val points = mutableListOf<ChartPoint>()
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

                tooltip("Advancers / Decliners\nMin price: $$minPrice")
                style {
                    padding = box(5.px)
                }

                GlobalScope.launch {
                    while (isActive) {
                        val now = LocalDateTime.now(ZoneId.of("America/New_York"))
                        if (isMarketHours(now)) {
                            if (points.size > maxIntervals) { points.clear() }
                            getAdvancersDecliners(iex, minPrice)?.let {
                                points.add(ChartPoint(now, it.advancerCount - it.declinerCount))
                                val msg = "${it.advancerCount} adv / ${it.declinerCount} dec"
                                runLater {
                                    label.text = msg
                                }
                            }
                        }
                        delay(interval)
                    }
                }

                fun showChart(): Stage? {
                    return object : Fragment() {
                        override val root = linechart(null, CategoryAxis(), NumberAxis()) {
                            animated = false
                            createSymbols = false
                            isLegendVisible = false
                            isHorizontalGridLinesVisible = false
                            verticalGridLinesVisible = false
                            vgrow = Priority.ALWAYS
                            hgrow = Priority.ALWAYS

                            title = "Advancers - Decliners = ${points.last().change}"

                            series("Advancers - Decliners") {
                                points.forEach { point ->
                                    data(timeFormatter.format(point.time), point.change)
                                }
                            }
                        }
                    }.openWindow()
                }

                var chart: Stage? = null
                onMouseEntered = EventHandler {
                    if (chart == null) {
                        chart = showChart()
                    }
                }
                onMouseExited = EventHandler {
                    chart?.close()
                    chart = null
                }

                onMouseClicked = EventHandler {
                    if (it.button == MouseButton.PRIMARY) showChart()
                }
            }

            label {
                data class ChartPoint(
                    val time: LocalDateTime,
                    val highCount: Int,
                    val lowCount: Int
                )

                val label = this
                val minCap = 50_000_000L
                val interval = 60 * 1000
                val day = 6.5 * 60 * 60 * 1000
                val maxIntervals = day / interval
                val points = mutableListOf<ChartPoint>()
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                var highsLows: HighsLows? = null

                tooltip("New 52-week highs and lows\nMin market cap: ${minCap.toReadableNumber()}")
                style {
                    padding = box(5.px)
                }

                GlobalScope.launch {
                    while (isActive) {
                        val stats = IexSymbols.assetStats
                        if (stats != null) {
                            val now = LocalDateTime.now(ZoneId.of("America/New_York"))
                            if (isMarketHours(now)) {
                                if (points.size > maxIntervals) { points.clear() }
                                getHighsLows(iex, stats, minCap)?.let {
                                    highsLows = it
                                    points.add(ChartPoint(now, it.highs.size, it.lows.size))
                                    val msg = "${it.highs.size} hi / ${it.lows.size} lo"
                                    runLater {
                                        label.text = msg
                                    }
                                }
                            }
                            delay(interval)
                        } else {
                            delay(1000 * 5)
                        }
                    }
                }

                fun showChart(): Stage? {
                    return object : Fragment() {
                        override val root = linechart(null, CategoryAxis(), NumberAxis()) {
                            animated = false
                            createSymbols = false
                            isHorizontalGridLinesVisible = false
                            verticalGridLinesVisible = false
                            vgrow = Priority.ALWAYS
                            hgrow = Priority.ALWAYS

                            title = "New 52-week highs and lows"

                            series("Highs") {
                                points.forEach { point ->
                                    data(timeFormatter.format(point.time), point.highCount)
                                }
                            }

                            series("Lows") {
                                points.forEach { point ->
                                    data(timeFormatter.format(point.time), point.lowCount)
                                }
                            }
                        }
                    }.openWindow()
                }

                var chart: Stage? = null
                onMouseEntered = EventHandler {
                    if (chart == null) {
                        chart = showChart()
                    }
                }
                onMouseExited = EventHandler {
                    chart?.close()
                    chart = null
                }

                onMouseClicked = EventHandler {
                    if (it.button == MouseButton.PRIMARY) showChart()
                }

                fun EventTarget.makeSymbolList(name: String, symbols: ObservableList<String>) = vbox {
                    label(name) {
                        alignment = Pos.CENTER
                        maxWidth = Double.MAX_VALUE
                    }
                    listview(symbols) {
                        vgrow = Priority.ALWAYS
                        onUserSelect {
                            research(it.split("\n").first())
                        }
                    }
                    hgrow = Priority.ALWAYS
                }

                contextmenu {
                    item("Show companies").action {
                        highsLows?.let {
                            label.runAsyncWithProgress {
                                Pair(
                                    it.highs.map { sym -> "$sym\n${IexSymbols.name(sym)}" },
                                    it.lows.map { sym -> "$sym\n${IexSymbols.name(sym)}" }
                                )
                            } ui { pair ->
                                object : Fragment() {
                                    override val root = hbox {
                                        padding = Insets(5.0)
                                        spacing = 5.0
                                        makeSymbolList("Highs", pair.first.observable())
                                        makeSymbolList("Lows", pair.second.observable())
                                    }
                                }.openWindow()
                            }
                        }
                    }
                }
            }

            label {
                val label = this
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                style {
                    fontFamily = "Menlo"
                }
                GlobalScope.launch {
                    while (isActive) {
                        delay(1000)
                        val timeStr = ZonedDateTime
                            .now(newYorkZoneId)
                            .format(timeFormatter)
                        runLater {
                            label.text = timeStr
                        }
                    }
                }
            }
        }
        hbox {
            vgrow = Priority.ALWAYS

            this += ReutersFragment()
        }
        toolbar {
            spacer {}
            button("Log").action {
                getAppLog()?.apply {
                    find(InfoFragment::class.java).apply {
                        setInfo("App Log", readText())
                        openModal()
                    }
                }
            }
        }
    }
}