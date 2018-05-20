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

    init {
        Iex.setOkHttpClient(HttpClients.main)
    }

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
        val company = Iex.getCompany(symbol1)
        assertEquals(symbol1, company?.symbol, "Should fetch the right symbol.")
    }

    @Test
    fun getAssetStats() {
        val stats = Iex.getAssetStats(symbol1)
        assertEquals(symbol1, stats?.symbol, "Should fetch the right symbol.")
    }

    @Test
    fun getMostActive() {
        Iex.getMostActive()
    }

    @Test
    fun getGainers() {
        Iex.getGainers()
    }

    @Test
    fun getLosers() {
        Iex.getLosers()
    }

    @Test
    fun getIexVolume() {
        Iex.getIexVolume()
    }

    @Test
    fun getIexPercent() {
        Iex.getIexPercent()
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
        Iex.getDividends(symbol1)
    }

    @Test
    fun getEarnings() {
        Iex.getEarnings(symbol3)
    }

    @Test
    fun getPeers() {
        Iex.getPeers(symbol3)
    }

    @Test
    fun getVolumeByVenue() {
        Iex.getVolumeByVenue(symbol2)
    }

    @Test
    fun getLogoData() {
        Iex.getLogoData(symbol3)
    }

    @Test
    fun getFinancials() {
        Iex.getFinancials(symbol1)
    }

    @Test
    fun getSpread() {
        Iex.getSpread(symbol2)
    }

    @Test
    fun getOHLC() {
        Iex.getOHLC(symbol3)
    }

    @Test
    fun getSplits() {
        Iex.getSplits(symbol1, Iex.Range.Y5)
    }

    @Test
    fun getSymbols() {
        Iex.getSymbols()
    }

    @Test
    fun getBatchOne() {
        Iex.getBatch(symbol1)
    }

    @Test
    fun getBatchMany() {
        Iex.getBatch(listOf(symbol1, symbol2, symbol3))
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
        if (isWeekend()) return

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
        Iex.parseTops(data)
    }

    @Test
    fun getDepth() {
        if (!isMarketHours()) return

        Iex.getDepth(symbol3)
    }

    @Test
    fun getBook() {
        Iex.getBook(symbol1)
        assertEquals(null, Iex.getBook(badSymbol))
    }

    @Test
    fun getTrades() {
        if (isWeekend()) return

        val trades = Iex.getTrades(symbol2, last = 10)
        assertEquals(10, trades?.size)
    }

    @Test
    fun getIntradayStats() {
        Iex.getIntradayStats()
    }

    @Test
    fun getRecordsStats() {
        Iex.getRecordsStats()
    }
}