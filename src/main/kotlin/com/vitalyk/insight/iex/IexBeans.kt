package com.vitalyk.insight.iex

import com.vitalyk.insight.iex.Iex.DayChartPoint
import javafx.beans.property.*
import tornadofx.*
import java.util.*

class DayChartPointBean {
    val changeOverTimeProperty = SimpleDoubleProperty()
    var changeOverTime by changeOverTimeProperty
    val labelProperty = SimpleStringProperty()
    var label by labelProperty
    val vwapProperty = SimpleDoubleProperty()
    var vwap by vwapProperty
    val changePercentProperty = SimpleDoubleProperty()
    var changePercent by changePercentProperty
    val changeProperty = SimpleDoubleProperty()
    var change by changeProperty
    val unadjustedVolumeProperty = SimpleLongProperty()
    var unadjustedVolume by unadjustedVolumeProperty
    val volumeProperty = SimpleLongProperty()
    var volume by volumeProperty
    val closeProperty = SimpleDoubleProperty()
    var close by closeProperty
    val lowProperty = SimpleDoubleProperty()
    var low by lowProperty
    val highProperty = SimpleDoubleProperty()
    var high by highProperty
    val openProperty = SimpleDoubleProperty()
    var open by openProperty
    val dateProperty = SimpleObjectProperty<Date>()
    var date by dateProperty
}

fun DayChartPoint.toBean() =
    DayChartPointBean().let {
        it.change = change
        it.changeOverTime = changeOverTime
        it.changePercent = changePercent
        it.close = close
        it.date = date
        it.high = high
        it.label = label
        it.low = low
        it.open = open
        it.unadjustedVolume = unadjustedVolume
        it.volume = volume
        it.vwap = vwap
        it
    }

fun DayChartPoint.toBean(bean: DayChartPointBean) = bean.let {
    it.change = change
    it.changeOverTime = changeOverTime
    it.changePercent = changePercent
    it.close = close
    it.date = date
    it.high = high
    it.label = label
    it.low = low
    it.open = open
    it.unadjustedVolume = unadjustedVolume
    it.volume = volume
    it.vwap = vwap
}


class TopsBean {
    val securityTypeProperty = SimpleStringProperty()
    var securityType by securityTypeProperty
    val sectorProperty = SimpleStringProperty()
    var sector by sectorProperty
    val lastUpdatedProperty = SimpleObjectProperty<Date>()
    var lastUpdated by lastUpdatedProperty
    val lastSaleTimeProperty = SimpleObjectProperty<Date>()
    var lastSaleTime by lastSaleTimeProperty
    val lastSaleSizeProperty = SimpleIntegerProperty()
    var lastSaleSize by lastSaleSizeProperty
    val lastSalePriceProperty = SimpleDoubleProperty()
    var lastSalePrice by lastSalePriceProperty
    val volumeProperty = SimpleLongProperty()
    var volume by volumeProperty
    val askPriceProperty = SimpleDoubleProperty()
    var askPrice by askPriceProperty
    val askSizeProperty = SimpleIntegerProperty()
    var askSize by askSizeProperty
    val bidPriceProperty = SimpleDoubleProperty()
    var bidPrice by bidPriceProperty
    val bidSizeProperty = SimpleIntegerProperty()
    var bidSize by bidSizeProperty
    val marketPercentProperty = SimpleDoubleProperty()
    var marketPercent by marketPercentProperty
    val symbolProperty = SimpleStringProperty()
    var symbol by symbolProperty
}

fun Iex.Tops.toBean() =
    TopsBean().let {
        it.askPrice = this.askPrice
        it.askSize = this.askSize
        it.bidPrice = this.bidPrice
        it.bidSize = this.bidSize
        it.lastSalePrice = this.lastSalePrice
        it.lastSaleSize = this.lastSaleSize
        it.lastSaleTime = this.lastSaleTime
        it.lastUpdated = this.lastUpdated
        it.marketPercent = this.marketPercent
        it.sector = this.sector
        it.securityType = this.securityType
        it.symbol = this.symbol
        it.volume = this.volume
        it
    }