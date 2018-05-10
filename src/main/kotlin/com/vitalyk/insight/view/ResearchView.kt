package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.AssetProfileFragment
import com.vitalyk.insight.fragment.AssetStatsFragment
import com.vitalyk.insight.fragment.EarningsFragment
import com.vitalyk.insight.iex.IexSymbols
import com.vitalyk.insight.ui.symbolfield
import com.vitalyk.insight.ui.toolbox
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import tornadofx.*

class ResearchView : View("Research") {
    val symbolField = symbolfield { fetch(it) }

    val profile = AssetProfileFragment()
    val earnings = EarningsFragment()
    val stats = AssetStatsFragment()

    override val root = vbox {
        toolbox {
            button("Main").action { replaceWith(MainView::class) }
            label("Symbol:")
            this += symbolField
        }

        scrollpane {
            hbox {
                vbox {
                    this += earnings
                    this += profile
                }
                vbox {
                    this += stats
                }
            }
        }
    }

    fun fetch(symbol: String) {
        title = "Research ${IexSymbols.name(symbol)} ($symbol)"
        symbolField.text = symbol
        profile.fetch(symbol)
        earnings.fetch(symbol)
        stats.fetch(symbol)
    }

    private var clipboardJob: Job? = null
    private var clipboardSymbol: String = ""

    override fun onDock() {
        clipboardJob = launch {
            while (isActive) {
                delay(1000)
                runLater {
                    val symbol = clipboard.string
                    if (symbol in IexSymbols && symbol != clipboardSymbol) {
                        clipboardSymbol = symbol
                        fetch(symbol)
                    }
                }
            }
        }
    }

    override fun onUndock() {
        clipboardJob?.cancel()
    }
}