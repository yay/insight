package com.vitalyk.insight.ui

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

internal class HelpersTest {

    data class TestClass(
        val date: Date,
        val open: Double,
        val high: Double,
        val low: Double,
        val close: Double,
        val volume: Long,
        val unadjustedVolume: Long,
        val change: Double,
        val changePercent: Double,
        val vwap: Double,
        val label: String,
        val changeOverTime: Double
    )

    val testClassString = """
    open class TestClassBean(
        change: Double,
        changeOverTime: Double,
        changePercent: Double,
        close: Double,
        date: Date,
        high: Double,
        label: String,
        low: Double,
        open: Double,
        unadjustedVolume: Long,
        volume: Long,
        vwap: Double
    ) {
        var change: Double by property(change)
        fun changeProperty() = getProperty(TestClassBean::change)

        var changeOverTime: Double by property(changeOverTime)
        fun changeOverTimeProperty() = getProperty(TestClassBean::changeOverTime)

        var changePercent: Double by property(changePercent)
        fun changePercentProperty() = getProperty(TestClassBean::changePercent)

        var close: Double by property(close)
        fun closeProperty() = getProperty(TestClassBean::close)

        var date: Date by property(date)
        fun dateProperty() = getProperty(TestClassBean::date)

        var high: Double by property(high)
        fun highProperty() = getProperty(TestClassBean::high)

        var label: String by property(label)
        fun labelProperty() = getProperty(TestClassBean::label)

        var low: Double by property(low)
        fun lowProperty() = getProperty(TestClassBean::low)

        var open: Double by property(open)
        fun openProperty() = getProperty(TestClassBean::open)

        var unadjustedVolume: Long by property(unadjustedVolume)
        fun unadjustedVolumeProperty() = getProperty(TestClassBean::unadjustedVolume)

        var volume: Long by property(volume)
        fun volumeProperty() = getProperty(TestClassBean::volume)

        var vwap: Double by property(vwap)
        fun vwapProperty() = getProperty(TestClassBean::vwap)
    }
    """.trimIndent()

    @Test
    fun dataClassToFxBean() {
        assertEquals(testClassString, dataClassToFxBean(TestClass::class))
    }
}