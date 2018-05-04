package com.vitalyk.insight.view

import com.vitalyk.insight.helpers.browseTo
import com.vitalyk.insight.ui.toolbox
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import tornadofx.*

class StatsView : View("Statistics") {
    override val root = vbox {
        toolbox(border = false) {
            button("Back").action {
                replaceWith(SymbolTableView::class)
            }
        }
        hbox {
            vgrow = Priority.ALWAYS
            scrollpane {
                padding = Insets(10.0)
                vbox {
                    spacing = 10.0
                    button("Nasdaq stats").action {
                        browseTo("https://www.nasdaq.com/markets/most-active.aspx")
                    }
                    button("U.S. Yield Curve").action {
                        replaceWith(YieldCurveView::class)
                    }
                    button("Employment (BLS)").action {
                        browseTo("https://www.bls.gov/news.release/empsit.nr0.htm")
                    }
                    button("Unemployment (FRED)").action {
                        browseTo("https://fred.stlouisfed.org/series/UNRATE")
                    }
                    button("Shiller P/E").action {
                        browseTo("http://www.multpl.com/table?f=m")
                    }

                    children.forEach {
                        if (it is Button) {
                            it.maxWidth = Double.MAX_VALUE
                        }
                    }
                }
            }
        }
    }
}

