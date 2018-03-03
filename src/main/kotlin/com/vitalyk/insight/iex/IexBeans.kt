package com.vitalyk.insight.iex

import com.vitalyk.insight.iex.IexApi.DayChartPoint
import tornadofx.*
import java.util.*

open class DayChartPointBean(
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
    fun changeProperty() = getProperty(DayChartPointBean::change)

    var changeOverTime: Double by property(changeOverTime)
    fun changeOverTimeProperty() = getProperty(DayChartPointBean::changeOverTime)

    var changePercent: Double by property(changePercent)
    fun changePercentProperty() = getProperty(DayChartPointBean::changePercent)

    var close: Double by property(close)
    fun closeProperty() = getProperty(DayChartPointBean::close)

    var date: Date by property(date)
    fun dateProperty() = getProperty(DayChartPointBean::date)

    var high: Double by property(high)
    fun highProperty() = getProperty(DayChartPointBean::high)

    var label: String by property(label)
    fun labelProperty() = getProperty(DayChartPointBean::label)

    var low: Double by property(low)
    fun lowProperty() = getProperty(DayChartPointBean::low)

    var open: Double by property(open)
    fun openProperty() = getProperty(DayChartPointBean::open)

    var unadjustedVolume: Long by property(unadjustedVolume)
    fun unadjustedVolumeProperty() = getProperty(DayChartPointBean::unadjustedVolume)

    var volume: Long by property(volume)
    fun volumeProperty() = getProperty(DayChartPointBean::volume)

    var vwap: Double by property(vwap)
    fun vwapProperty() = getProperty(DayChartPointBean::vwap)
}