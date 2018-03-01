package com.vitalyk.insight.ui

import com.vitalyk.insight.iex.IexApi
import javafx.application.Application
import javafx.application.Platform
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.junit.jupiter.api.Test
import tornadofx.*

internal class QuoteListTest {

    @Test
    fun testLook() {
        class TestView : View() {

            val testQuoteJson =
                """
                {
                    "symbol": "GE",
                    "companyName": "General Electric Company",
                    "primaryExchange": "New York Stock Exchange",
                    "sector": "Industrials",
                    "calculationPrice": "tops",
                    "open": 14.15,
                    "openTime": 1519914612300,
                    "close": 14.11,
                    "closeTime": 1519851624130,
                    "high": 14.26,
                    "low": 14,
                    "latestPrice": 14.015,
                    "latestSource": "IEX real time price",
                    "latestTime": "1:56:12 PM",
                    "latestUpdate": 1519930572195,
                    "latestVolume": 54704776,
                    "iexRealtimePrice": 14.015,
                    "iexRealtimeSize": 1,
                    "iexLastUpdated": 1519930572195,
                    "delayedPrice": 14.065,
                    "delayedPriceTime": 1519929682244,
                    "previousClose": 14.11,
                    "change": -0.095,
                    "changePercent": -0.00673,
                    "iexMarketPercent": 0.03036,
                    "iexVolume": 1660837,
                    "avgTotalVolume": 103507156,
                    "iexBidPrice": 14,
                    "iexBidSize": 22811,
                    "iexAskPrice": 14.02,
                    "iexAskSize": 500,
                    "marketCap": 121686302640,
                    "peRatio": 13.35,
                    "week52High": 30.54,
                    "week52Low": 13.95,
                    "ytdChange": -0.21523915461624032
                }
                """.trimIndent()

            val testQuote = IexApi.parseQuote(testQuoteJson)

            val testQuotes = listOf(
                testQuote.copy(change = 0.0, changePercent = 0.0, latestPrice = 100.0),
                testQuote.copy(change = 1.0, changePercent = 0.01, latestPrice = 101.0),
                testQuote.copy(change = -1.0, changePercent = -0.01, latestPrice = 99.0)
            )

            val quoteList = QuoteList("Most active") { testQuotes }

            val hbox = hbox {
                this += quoteList // += adds the quoteList.root
            }

            override val root = hbox

            init {
                launch {
//                    delay(5000)
                    Platform.exit()
                }
            }
        }

        class TestApp : App(TestView::class)

        Application.launch(TestApp::class.java)
    }
}