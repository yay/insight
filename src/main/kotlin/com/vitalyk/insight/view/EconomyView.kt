package com.vitalyk.insight.view

import com.vitalyk.insight.ui.browsebutton
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import tornadofx.*

class EconomyView : View("Economy") {
    override val root = vbox {
        toolbar {
            button("Main").action {
                replaceWith(MainView::class)
            }
        }
        hbox {
            vgrow = Priority.ALWAYS
            scrollpane {
                padding = Insets(10.0)
                vbox {
                    spacing = 10.0

                    button("U.S. Yield Curve").action {
                        replaceWith(YieldCurveView::class)
                    }
                    browsebutton("Nasdaq stats", "https://www.nasdaq.com/markets/most-active.aspx")
                    browsebutton("Central Banks", "https://www.investing.com/central-banks/")
                    browsebutton("Employment (BLS)", "https://www.bls.gov/news.release/empsit.nr0.htm")
                    browsebutton("Unemployment (FRED)", "https://fred.stlouisfed.org/series/UNRATE")
                    browsebutton("Savings rate", "https://tradingeconomics.com/united-states/personal-savings")
                    browsebutton("Shiller P/E", "http://www.multpl.com/table?f=m")
                    browsebutton("World Equity Index", "https://www.cnbc.com/quotes/?symbol=.MIWD00000PUS")

                    children.forEach {
                        if (it is Button) {
                            it.maxWidth = Double.MAX_VALUE
                        }
                    }
                }
            }
            scrollpane {
                padding = Insets(10.0)
                vbox {
                    spacing = 10.0

                    browsebutton("Economic Calendar", "http://hosting.briefing.com/cschwab/Calendars/EconomicCalendar.htm")
                    browsebutton("Earnings Calendar", "http://hosting.briefing.com/cschwab/Calendars/EarningsCalendar5Weeks.htm")
                    browsebutton("IPO Lockup Expirations", "https://www.marketbeat.com/ipos/lockup-expirations/") {
                        tooltip("""
                            A lock-up agreement is a legally binding contract between the underwriters
                            and insiders of a company prohibiting these individuals from selling any shares
                            of stock for a specified period of time. Lock-up periods typically last 180 days.
                        """.trimIndent())
                    }

                    children.forEach {
                        if (it is Button) {
                            it.maxWidth = Double.MAX_VALUE
                        }
                    }
                }
            }
            scrollpane {
                padding = Insets(10.0)
                vbox {
                    spacing = 10.0

                    button("Share Buyback").action {
                        find(BuybackView::class).openModal()
                    }
                }
            }
        }
    }
}

