package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.MainWatchlistFragment
import com.vitalyk.insight.fragment.ReutersFragment
import com.vitalyk.insight.helpers.newYorkZoneId
import javafx.beans.property.SimpleStringProperty
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
            button("Watchlists").action { replaceWith(WatchlistView::class) }
            button("Stats").action { replaceWith(StatsView::class) }
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
            this += MainWatchlistFragment()
        }
    }
}

fun main(args: Array<String>) {
    javafx.scene.text.Font.getFamilies().forEach {
        println(it)
    }
}