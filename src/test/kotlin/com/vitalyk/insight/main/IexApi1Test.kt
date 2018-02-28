package com.vitalyk.insight.main

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class IexApi1Test {

    val symbol1 = "AAPL"
    val symbol2 = "AMZN"
    val symbol3 = "NFLX"

    @Test
    fun getCompany() {
        val company = IexApi1.getCompany(symbol1)
        assertEquals(company.symbol, symbol1, "Should fetch the right symbol.")
    }

    @Test
    fun getStats() {
        val stats = IexApi1.getStats(symbol1)
        assertEquals(stats.symbol, symbol1, "Should fetch the right symbol.")
    }

    @Test
    fun getMostActive() {
        val mostActive = IexApi1.getMostActive()
//        assertEquals(mostActive.isNotEmpty(), true, "Should return a non-empty list of quotes.")
    }

    @Test
    fun getGainers() {
        val gainers = IexApi1.getGainers()
    }

    @Test
    fun getLosers() {
        val losers = IexApi1.getLosers()
    }

    @Test
    fun getIexVolume() {
        val iexVolume = IexApi1.getIexVolume()
    }

    @Test
    fun getIexPercent() {
        val iexPercent = IexApi1.getIexPercent()
    }

    @Test
    fun getChart() {
        val chart = IexApi1.getChart(symbol2, IexApi1.Range.M)
        assertEquals(chart.isNotEmpty(), true, "Should contain a few chart data points.")
    }

    @Test
    fun getDayChart() {
        val dayChart = IexApi1.getDayChart(symbol1, "20180129")
        assertEquals(dayChart.isNotEmpty(), true, "Should contain a few chart data points.")
    }

    @Test
    fun getDividends() {
        val dividends = IexApi1.getDividends(symbol1)
    }

    @Test
    fun getEarnings() {
        val earnings = IexApi1.getEarnings(symbol3)
    }

    @Test
    fun getPeers() {
        val peers = IexApi1.getPeers(symbol3)
    }

    @Test
    fun getVolumeByVenue() {
        val volumeByVenue = IexApi1.getVolumeByVenue(symbol2)
    }

    @Test
    fun getLogoData() {
        val logoData = IexApi1.getLogoData(symbol3)
    }

    @Test
    fun getFinancials() {
        val financials = IexApi1.getFinancials(symbol1)
    }

    @Test
    fun getSpread() {
        val spread = IexApi1.getSpread(symbol2)
    }

    @Test
    fun getOHLC() {
        val ohlc = IexApi1.getOHLC(symbol3)
    }

    @Test
    fun getSplits() {
        val splits = IexApi1.getSplits(symbol1, IexApi1.Range.Y5)
    }

    @Test
    fun getSymbols() {
        val symbols = IexApi1.getSymbols()
    }

    @Test
    fun getBatchOne() {
        val batch = IexApi1.getBatch(symbol1)
    }

    @Test
    fun getBatchMany() {
        val batch = IexApi1.getBatch(listOf(symbol1, symbol2, symbol3))
    }

    @Test
    fun getLastTrade() {
        val lastTrades = IexApi1.getLastTrade(listOf(symbol1, symbol2))
        assertEquals(lastTrades.size, 2, "Should return last trades for 2 symbols.")

        val allLastTrades = IexApi1.getLastTrade()
        assertEquals(allLastTrades.size > 8000, true, "Should return last trades for 8000+ symbols.")
    }

    @Test
    fun getTops() {
        val tops = IexApi1.getTops(listOf(symbol1, symbol2))
        assertEquals(tops.size, 2, "Should return tops data for 2 symbols.")

        val allTops = IexApi1.getTops()
        assertEquals(allTops.size > 8000, true, "Should return tops data for 8000+ symbols.")
    }

    @Test
    fun parseTops() {
        val data = """
            {
                "symbol": "AAPL",
                "marketPercent": 0.02582,
                "bidSize": 100,
                "bidPrice": 179.6,
                "askSize": 100,
                "askPrice": 179.62,
                "volume": 271859,
                "lastSalePrice": 179.63,
                "lastSaleSize": 100,
                "lastSaleTime": 1519833938768,
                "lastUpdated": 1519833947449,
                "sector": "technologyhardwareequipmen",
                "securityType": "commonstock"
            }
        """.trimIndent()
        val tops = IexApi1.parseTops(data)
    }

    @Test
    fun getDepth() {
        val depth = IexApi1.getDepth(symbol3)
    }

    @Test
    fun getBook() {
        val book = IexApi1.getBook(symbol1)
    }

    @Test
    fun getTrades() {
        val trades = IexApi1.getTrades(symbol2, last = 10)
        assertEquals(trades.size, 10, "Should return last 10 trades.")
    }

    @Test
    fun getIntradayStats() {
        val intradayStats = IexApi1.getIntradayStats()
    }

    @Test
    fun getRecordsStats() {
        val recordsStats = IexApi1.getRecordsStats()
    }
}