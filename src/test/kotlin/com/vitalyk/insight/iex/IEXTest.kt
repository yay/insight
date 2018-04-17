package com.vitalyk.insight.iex

import com.vitalyk.insight.main.getLastWorkDay
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.format.DateTimeFormatter


internal class IexTest {

    val symbol1 = "AAPL"
    val symbol2 = "AMZN"
    val symbol3 = "NFLX"
    val badSymbol = "ABRACADABRA"

    @Test
    fun getCompany() {
        val company = Iex.getCompany(symbol1)
        assertEquals(symbol1, company?.symbol, "Should fetch the right symbol.")
    }

    @Test
    fun getStats() {
        val stats = Iex.getStats(symbol1)
        assertEquals(symbol1, stats?.symbol, "Should fetch the right symbol.")
    }

    @Test
    fun getMostActive() {
        val mostActive = Iex.getMostActive()
//        assertEquals(true, mostActive.isNotEmpty(), "Should return a non-empty list of quotes.")
    }

    @Test
    fun getGainers() {
        val gainers = Iex.getGainers()
    }

    @Test
    fun getLosers() {
        val losers = Iex.getLosers()
    }

    @Test
    fun getIexVolume() {
        val iexVolume = Iex.getIexVolume()
    }

    @Test
    fun getIexPercent() {
        val iexPercent = Iex.getIexPercent()
    }

    @Test
    fun getDayChart() {
        val chartPoints1 = Iex.getDayChart(symbol2, Iex.Range.M)
        assertEquals(true, chartPoints1?.isNotEmpty())

        val chartPoints2 = Iex.getDayChart(badSymbol, Iex.Range.M)
        assertEquals(null, chartPoints2)
    }

    @Test
    fun getMinuteChart() {
        // Note: doesn't account for holidays.
        val lastWorkDay = getLastWorkDay()
        val dateString = lastWorkDay.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val chartPoints = Iex.getMinuteChart(symbol1, dateString)
        assertEquals(true, chartPoints?.isNotEmpty())

        chartPoints?.let {
            val date = it.first().date
            assertEquals(lastWorkDay.year, date.year)
            assertEquals(lastWorkDay.monthValue, date.monthValue)
            assertEquals(lastWorkDay.dayOfMonth, date.dayOfMonth)
        }
    }

    @Test
    fun getDividends() {
        val dividends = Iex.getDividends(symbol1)
    }

    @Test
    fun getEarnings() {
        val earnings = Iex.getEarnings(symbol3)
    }

    @Test
    fun getPeers() {
        val peers = Iex.getPeers(symbol3)
    }

    @Test
    fun getVolumeByVenue() {
        val volumeByVenue = Iex.getVolumeByVenue(symbol2)
    }

    @Test
    fun getLogoData() {
        val logoData = Iex.getLogoData(symbol3)
    }

    @Test
    fun getFinancials() {
        val financials = Iex.getFinancials(symbol1)
    }

    @Test
    fun getSpread() {
        val spread = Iex.getSpread(symbol2)
    }

    @Test
    fun getOHLC() {
        val ohlc = Iex.getOHLC(symbol3)
    }

    @Test
    fun getSplits() {
        val splits = Iex.getSplits(symbol1, Iex.Range.Y5)
    }

    @Test
    fun getSymbols() {
        val symbols = Iex.getSymbols()
    }

    @Test
    fun getBatchOne() {
        val batch = Iex.getBatch(symbol1)
    }

    @Test
    fun getBatchMany() {
        val batch = Iex.getBatch(listOf(symbol1, symbol2, symbol3))
    }

    @Test
    fun getLastTrade() {
        val lastTrades = Iex.getLastTrade(listOf(symbol1, symbol2))
        assertEquals(2, lastTrades?.size)

        val allLastTrades = Iex.getLastTrade()
        assertEquals(true, allLastTrades != null && allLastTrades.size > 8000)
    }

    @Test
    fun getTops() {
        if (Iex.isWeekend()) return

        val tops = Iex.getTops(listOf(symbol1, symbol2))
        assertEquals(2, tops?.size)

        val allTops = Iex.getTops()
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
        val tops = Iex.parseTops(data)
    }

    @Test
    fun getDepth() {
        if (!Iex.isMarketHours()) return

        val depth = Iex.getDepth(symbol3)
    }

    @Test
    fun getBook() {
        val book1 = Iex.getBook(symbol1)
        val book2 = Iex.getBook(badSymbol)
        assertEquals(null, book2)
    }

    @Test
    fun getTrades() {
        if (Iex.isWeekend()) return

        val trades = Iex.getTrades(symbol2, last = 10)
        assertEquals(10, trades?.size)
    }

    @Test
    fun getIntradayStats() {
        val intradayStats = Iex.getIntradayStats()
    }

    @Test
    fun getRecordsStats() {
        val recordsStats = Iex.getRecordsStats()
    }
}