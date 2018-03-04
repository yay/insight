package com.vitalyk.insight.iex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class IexApiTest {

    val symbol1 = "AAPL"
    val symbol2 = "AMZN"
    val symbol3 = "NFLX"
    val badSymbol = "ABRACADABRA"

    @Test
    fun getCompany() {
        val company = IexApi.getCompany(symbol1)
        assertEquals(symbol1, company?.symbol, "Should fetch the right symbol.")
    }

    @Test
    fun getStats() {
        val stats = IexApi.getStats(symbol1)
        assertEquals(symbol1, stats?.symbol, "Should fetch the right symbol.")
    }

    @Test
    fun getMostActive() {
        val mostActive = IexApi.getMostActive()
//        assertEquals(true, mostActive.isNotEmpty(), "Should return a non-empty list of quotes.")
    }

    @Test
    fun getGainers() {
        val gainers = IexApi.getGainers()
    }

    @Test
    fun getLosers() {
        val losers = IexApi.getLosers()
    }

    @Test
    fun getIexVolume() {
        val iexVolume = IexApi.getIexVolume()
    }

    @Test
    fun getIexPercent() {
        val iexPercent = IexApi.getIexPercent()
    }

    @Test
    fun getDayChart() {
        val chartPoints1 = IexApi.getDayChart(symbol2, IexApi.Range.M)
        assertEquals(true, chartPoints1?.isNotEmpty())

        val chartPoints2 = IexApi.getDayChart(badSymbol, IexApi.Range.M)
        assertEquals(null, chartPoints2)
    }

    @Test
    fun getMinuteChart() {
        val chartPoints = IexApi.getMinuteChart(symbol1, "20180129")
        assertEquals(true, chartPoints?.isNotEmpty())

        chartPoints?.let {
            val date = it.first().date
            assertEquals(2018, date.year)
            assertEquals(1, date.monthValue)
            assertEquals(29, date.dayOfMonth)
        }
    }

    @Test
    fun getDividends() {
        val dividends = IexApi.getDividends(symbol1)
    }

    @Test
    fun getEarnings() {
        val earnings = IexApi.getEarnings(symbol3)
    }

    @Test
    fun getPeers() {
        val peers = IexApi.getPeers(symbol3)
    }

    @Test
    fun getVolumeByVenue() {
        val volumeByVenue = IexApi.getVolumeByVenue(symbol2)
    }

    @Test
    fun getLogoData() {
        val logoData = IexApi.getLogoData(symbol3)
    }

    @Test
    fun getFinancials() {
        val financials = IexApi.getFinancials(symbol1)
    }

    @Test
    fun getSpread() {
        val spread = IexApi.getSpread(symbol2)
    }

    @Test
    fun getOHLC() {
        val ohlc = IexApi.getOHLC(symbol3)
    }

    @Test
    fun getSplits() {
        val splits = IexApi.getSplits(symbol1, IexApi.Range.Y5)
    }

    @Test
    fun getSymbols() {
        val symbols = IexApi.getSymbols()
    }

    @Test
    fun getBatchOne() {
        val batch = IexApi.getBatch(symbol1)
    }

    @Test
    fun getBatchMany() {
        val batch = IexApi.getBatch(listOf(symbol1, symbol2, symbol3))
    }

    @Test
    fun getLastTrade() {
        val lastTrades = IexApi.getLastTrade(listOf(symbol1, symbol2))
        assertEquals(2, lastTrades?.size)

        val allLastTrades = IexApi.getLastTrade()
        assertEquals(true, allLastTrades != null && allLastTrades.size > 8000)
    }

    @Test
    fun getTops() {
        if (IexApi.isWeekend()) return

        val tops = IexApi.getTops(listOf(symbol1, symbol2))
        assertEquals(2, tops?.size)

        val allTops = IexApi.getTops()
        assertEquals(true, allTops != null && allTops.size > 8000)
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
        val tops = IexApi.parseTops(data)
    }

    @Test
    fun getDepth() {
        if (IexApi.isWeekend()) return

        val depth = IexApi.getDepth(symbol3)
    }

    @Test
    fun getBook() {
        val book1 = IexApi.getBook(symbol1)
        val book2 = IexApi.getBook(badSymbol)
        assertEquals(null, book2)
    }

    @Test
    fun getTrades() {
        if (IexApi.isWeekend()) return

        val trades = IexApi.getTrades(symbol2, last = 10)
        assertEquals(10, trades?.size)
    }

    @Test
    fun getIntradayStats() {
        val intradayStats = IexApi.getIntradayStats()
    }

    @Test
    fun getRecordsStats() {
        val recordsStats = IexApi.getRecordsStats()
    }
}