package com.vitalyk.insight.iex

import com.vitalyk.insight.helpers.getLastWorkDay
import com.vitalyk.insight.main.HttpClients
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


internal class IexTest {

    val symbol1 = "AAPL"
    val symbol2 = "AMZN"
    val symbol3 = "NFLX"
    val badSymbol = "ABRACADABRA"

    val iex = Iex(HttpClients.main)

    fun isWeekend(): Boolean {
        val day = LocalDate.now(ZoneId.of("America/New_York")).dayOfWeek
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
    }

    fun isMarketHours(): Boolean {
        val datetime = LocalDateTime.now(ZoneId.of("America/New_York"))
        val day = datetime.dayOfWeek
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && datetime.hour in 8..18
    }

    @Test
    fun getCompany() {
        val company = iex.getCompany(symbol1)
        assertEquals(symbol1, company?.symbol, "Should fetch the right symbol.")
    }

    @Test
    fun getAssetStats() {
        val stats = iex.getAssetStats(symbol1)
        assertEquals(symbol1, stats?.symbol, "Should fetch the right symbol.")
    }

    @Test
    fun getMostActive() {
        iex.getMostActive()
    }

    @Test
    fun getGainers() {
        iex.getGainers()
    }

    @Test
    fun getLosers() {
        iex.getLosers()
    }

    @Test
    fun getIexVolume() {
        iex.getIexVolume()
    }

    @Test
    fun getIexPercent() {
        iex.getIexPercent()
    }

    @Test
    fun getDayChart() {
        val chartPoints1 = iex.getDayChart(symbol2, Iex.Range.M)
        assertEquals(true, chartPoints1?.isNotEmpty())

        val chartPoints2 = iex.getDayChart(badSymbol, Iex.Range.M)
        assertEquals(null, chartPoints2)
    }

    @Test
    fun getMinuteChart() {
        // Note: doesn't account for holidays.
        val lastWorkDay = getLastWorkDay()
        val dateString = lastWorkDay.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val chartPoints = iex.getMinuteChart(symbol1, dateString)
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
        iex.getDividends(symbol1)
    }

    @Test
    fun getEarnings() {
        iex.getEarnings(symbol3)
    }

    @Test
    fun getPeers() {
        iex.getPeers(symbol3)
    }

    @Test
    fun getVolumeByVenue() {
        iex.getVolumeByVenue(symbol2)
    }

    @Test
    fun getLogoData() {
        iex.getLogoData(symbol3)
    }

    @Test
    fun getFinancials() {
        iex.getFinancials(symbol1)
    }

    @Test
    fun getSpread() {
        iex.getSpread(symbol2)
    }

    @Test
    fun getOHLC() {
        iex.getOHLC(symbol3)
    }

    @Test
    fun getSplits() {
        iex.getSplits(symbol1, Iex.Range.Y5)
    }

    @Test
    fun getSymbols() {
        iex.getSymbols()
    }

    @Test
    fun getBatchOne() {
        iex.getBatch(symbol1)
    }

    @Test
    fun getBatchMany() {
        iex.getBatch(listOf(symbol1, symbol2, symbol3))
    }

    @Test
    fun getLastTrade() {
        val lastTrades = iex.getLastTrade(listOf(symbol1, symbol2))
        assertEquals(2, lastTrades?.size)

        val allLastTrades = iex.getLastTrade()
        assertEquals(true, allLastTrades != null && allLastTrades.size > 8000)
    }

    @Test
    fun getTops() {
        if (isWeekend()) return

        val tops = iex.getTops(listOf(symbol1, symbol2))
        assertEquals(2, tops?.size)

        val allTops = iex.getTops()
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
        Iex.parseTops(data)
    }

    @Test
    fun getDepth() {
        if (!isMarketHours()) return

        iex.getDepth(symbol3)
    }

    @Test
    fun getBook() {
        iex.getBook(symbol1)
        assertEquals(null, iex.getBook(badSymbol))
    }

    @Test
    fun getTrades() {
        if (isWeekend()) return

        val trades = iex.getTrades(symbol2, last = 10)
        assertEquals(10, trades?.size)
    }

    @Test
    fun getIntradayStats() {
        iex.getIntradayStats()
    }

    @Test
    fun getRecordsStats() {
        iex.getRecordsStats()
    }
}