package com.vitalyk.insight.view

import com.vitalyk.insight.fragment.AssetProfileFragment
import com.vitalyk.insight.fragment.AssetStatsFragment
import com.vitalyk.insight.fragment.EarningsFragment
import com.vitalyk.insight.iex.IexSymbols
import com.vitalyk.insight.ui.symbolfield
import javafx.scene.layout.Priority
import kotlinx.coroutines.*
import tornadofx.*

class ResearchView : View("Research") {
    val symbolField = symbolfield { fetch(it) }

    val profile = AssetProfileFragment()
    val earnings = EarningsFragment()
    val stats = AssetStatsFragment()

    override val root = vbox {
        toolbar {
            button("Main").action { replaceWith(MainView::class) }
            label("Symbol:")
            this += symbolField
        }

        scrollpane {
            vgrow = Priority.ALWAYS
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
        clipboardJob = GlobalScope.launch {
            while (isActive) {
                delay(1000)
                runLater {
                    clipboard.string?.let {
                        if (it in IexSymbols && it != clipboardSymbol) {
                            clipboardSymbol = it
                            fetch(it)
                        }
                    }
                }
            }
        }
    }

    override fun onUndock() {
        clipboardJob?.cancel()
    }
}