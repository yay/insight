package com.vitalyk.insight.fragment

import com.vitalyk.insight.helpers.toReadableNumber
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.Iex.AssetStats
import com.vitalyk.insight.main.HttpClients
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.text.FontWeight
import tornadofx.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AssetStatsFragment : Fragment() {
    val marketCap = Label()
    val beta = Label()

    val shortInterest = Label()
    val shortInterestPct = Label()
    val shortDate = Label()

    val dividendRate = Label()
    val dividendYield = Label()
    val exDividendDate = Label()

    val latestEps = Label()
    val latestEpsDate = Label()
    val consensusEps = Label()
    val ttmEps = Label()

    val sharesOutstanding = Label()
    val float = Label()
    val insiderPercent = Label()
    val institutionPercent = Label()

    val returnOnEquity = Label()
    val returnOnAssets = Label()
    val returnOnCapital = Label()
    val ebitda = Label()
    val revenue = Label()
    val grossProfit = Label()
    val cash = Label()
    val debt = Label()
    val revenuePerShare = Label()
    val revenuePerEmployee = Label()

    override val root = vbox {
        label("Metrics") {
            alignment = Pos.CENTER
            maxWidth = Double.MAX_VALUE
            padding = Insets(5.0)
            style {
                fontSize = 1.4.em
            }
        }
        form {
            fieldset(labelPosition = Orientation.VERTICAL) {
                gridpane {
                    hgap = 40.0

                    headerRow("Overview")
                    row {
                        label("Market Cap")
                        this += marketCap
                    }
                    row {
                        label("Beta") {
                            tooltip("A measure of the volatility of a security\n" +
                                "in comparison to the market as a whole.\n" +
                                "Market Change x Beta = Estimated Change")
                            // A security's beta should only be used when a security
                            // has a high R-squared value in relation to the benchmark.
                        }
                        this += beta
                    }
                    row {
                        label("Shares Outstanding") {
                            tooltip("Total shares (from IPO, subsequent offerings" +
                                " and exercise of convertible securities).")
                        }
                        this += sharesOutstanding
                    }
                    row {
                        label("Float") {
                            tooltip("Shares available for trading (outstanding shares" +
                                " minus restricted stock).")
                        }
                        this += float
                    }
                    row {
                        label("Insider Ownership")
                        this += insiderPercent
                    }
                    row {
                        label("Institutional Ownership")
                        this += institutionPercent
                    }

                    headerRow("Earnings")
                    row {
                        label("Latest EPS")
                        this += latestEps
                    }
                    row {
                        label("Latest EPS date")
                        this += latestEpsDate
                    }
                    row {
                        label("Consensus EPS")
                        this += consensusEps
                    }
                    row {
                        label("TTM EPS")
                        this += ttmEps
                    }

                    headerRow("Dividends")
                    row {
                        label("Dividend Rate")
                        this += dividendRate
                    }
                    row {
                        label("Dividend Yield")
                        this += dividendYield
                    }
                    row {
                        label("Ex Dividend Date")
                        this += exDividendDate
                    }

                    headerRow("Short Interest") {
                        this += shortDate
                    }
                    row {
                        label("Shares short")
                        this += shortInterest
                    }
                    row {
                        label("% of Shares Outstanding")
                        this += shortInterestPct
                    }

                    headerRow("Management Effectiveness")
                    row {
                        label("Return On Equity")
                        this += returnOnEquity
                    }
                    row {
                        label("Return On Assets")
                        this += returnOnAssets
                    }
                    row {
                        label("Return On Capital")
                        this += returnOnCapital
                    }
                    row {
                        label("Revenue / Employee")
                        this += revenuePerEmployee
                    }

                    headerRow("Financial Metrics")
                    row {
                        label("Revenue")
                        this += revenue
                    }
                    row {
                        label("Revenue / Share")
                        this += revenuePerShare
                    }
                    row {
                        label("EBITDA")
                        this += ebitda
                    }
                    row {
                        label("Gross Profit")
                        this += grossProfit
                    }
                    row {
                        label("Cash")
                        this += cash
                    }
                    row {
                        label("Debt")
                        this += debt
                    }
                }
            }
        }
    }

    fun GridPane.headerRow(title: String, op: Pane.() -> Unit = {}) = row {
        label(title) {
            padding = Insets(10.0, 0.0, 5.0, 0.0)
            style {
                fontFamily = "Verdana"
                fontWeight = FontWeight.BOLD
            }
        }
        op(this)
    }

    fun fetch(symbol: String) {
        runAsync {
            val iex = Iex(HttpClients.main)
            iex.getAssetStats(symbol)
        } ui {
            (it ?: AssetStats()).let {
                marketCap.text = formatNumber(it.marketCap)
                beta.text = formatNumber(it.beta, 4)

                val shortInterestRatio = it.shortInterest.toDouble() / it.sharesOutstanding.toDouble()
                shortInterest.text = formatNumber(it.shortInterest)
                shortInterestPct.text = formatPercent(shortInterestRatio * 100.0)
                shortDate.text = formatDate(it.shortDate)

                dividendRate.text = formatNumber(it.dividendRate)
                dividendYield.text = formatNumber(it.dividendYield, percent = true)
                exDividendDate.text = formatDate(it.exDividendDate)

                latestEps.text = formatNumber(it.latestEps)
                latestEpsDate.text = formatDate(it.latestEpsDate)
                consensusEps.text = formatNumber(it.consensusEps)
                ttmEps.text = formatNumber(it.ttmEps)

                sharesOutstanding.text = formatNumber(it.sharesOutstanding)
                float.text = formatNumber(it.float)
                insiderPercent.text = formatNumber(it.insiderPercent, percent = true)
                institutionPercent.text = formatNumber(it.institutionPercent, percent = true)

                returnOnEquity.text = formatPercent(it.returnOnEquity)
                returnOnAssets.text = formatPercent(it.returnOnAssets)
                returnOnCapital.text = formatPercent(it.returnOnCapital)
                ebitda.text = formatNumber(it.ebitda)
                revenue.text = formatNumber(it.revenue)
                grossProfit.text = formatNumber(it.grossProfit)
                cash.text = formatNumber(it.cash)
                debt.text = formatNumber(it.debt)
                revenuePerShare.text = formatNumber(it.revenuePerShare)
                revenuePerEmployee.text = formatNumber(it.revenuePerEmployee)
            }
        }
    }

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("E MMM dd, yy")

    private fun formatDate(date: LocalDate?): String {
        return when (date) {
            null -> "--"
            else -> date.format(dateTimeFormatter)
        }
    }

    private fun formatNumber(value: Double, significand: Int = 2, percent: Boolean = false): String {
        val suffix = if (percent) "%%" else ""
        return when {
            value == 0.0 || value.isNaN() || value.isInfinite() -> "--"
            else -> "%.${significand}f$suffix".format(value)
        }
    }

    private fun formatNumber(value: Long): String {
        return when (value) {
            0L -> "--"
            else -> value.toReadableNumber()
        }
    }

    private fun formatNumber(value: Int) = formatNumber(value.toLong())

    private fun formatPercent(value: Double): String {
        return when {
            value == 0.0 || value.isNaN() || value.isInfinite() -> "--"
            else -> "%.2f%%".format(value)
        }
    }
}