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
    val _symbolProperty = SimpleStringProperty()
    fun symbolProperty() = _symbolProperty
    var symbol: String
        get() = _symbolProperty.get()
        set(value) = _symbolProperty.set(value)


    val _marketPercentProperty = SimpleDoubleProperty()
    fun marketPercentProperty() = _marketPercentProperty
    var marketPercent: Double
        get() = _marketPercentProperty.get()
        set(value) = _marketPercentProperty.set(value)

    val _bidSizeProperty = SimpleIntegerProperty()
    fun bidSizeProperty() = _bidSizeProperty
    var bidSize: Int
        get() = _bidSizeProperty.get()
        set(value) = _bidSizeProperty.set(value)

    val _bidPriceProperty = SimpleDoubleProperty()
    fun bidPriceProperty() = _bidPriceProperty
    var bidPrice: Double
        get() = _bidPriceProperty.get()
        set(value) = _bidPriceProperty.set(value)

    val _askSizeProperty = SimpleIntegerProperty()
    fun askSizeProperty() = _askSizeProperty
    var askSize: Int
        get() = _askSizeProperty.get()
        set(value) = _askSizeProperty.set(value)

    val _askPriceProperty = SimpleDoubleProperty()
    fun askPriceProperty() = _askPriceProperty
    var askPrice: Double
        get() = _askPriceProperty.get()
        set(value) = _askPriceProperty.set(value)

    val _volumeProperty = SimpleLongProperty()
    fun volumeProperty() = _volumeProperty
    var volume: Long
        get() = _volumeProperty.get()
        set(value) = _volumeProperty.set(value)

    val _lastSalePriceProperty = SimpleDoubleProperty()
    fun lastSalePriceProperty() = _lastSalePriceProperty
    var lastSalePrice: Double
        get() = _lastSalePriceProperty.get()
        set(value) = _lastSalePriceProperty.set(value)

    val _lastSaleSizeProperty = SimpleIntegerProperty()
    fun lastSaleSizeProperty() = _lastSaleSizeProperty
    var lastSaleSize: Int
        get() = _lastSaleSizeProperty.get()
        set(value) = _lastSaleSizeProperty.set(value)

    val _lastSaleTimeProperty = SimpleObjectProperty<Date>()
    fun lastSaleTimeProperty() = _lastSaleTimeProperty
    var lastSaleTime: Date
        get() = _lastSaleTimeProperty.get()
        set(value) = _lastSaleTimeProperty.set(value)

    val _lastUpdatedProperty = SimpleObjectProperty<Date>()
    fun lastUpdatedProperty() = _lastUpdatedProperty
    var lastUpdated: Date
        get() = _lastUpdatedProperty.get()
        set(value) = _lastUpdatedProperty.set(value)

    val _sectorProperty = SimpleStringProperty()
    fun sectorProperty() = _sectorProperty
    var sector: String
        get() = _sectorProperty.get()
        set(value) = _sectorProperty.set(value)

    val _securityTypeProperty = SimpleStringProperty()
    fun securityTypeProperty() = _securityTypeProperty
    var securityType: String
        get() = _securityTypeProperty.get()
        set(value) = _securityTypeProperty.set(value)
}

fun Iex.Tops.toBean(bean: TopsBean): TopsBean {
    bean.symbol = symbol
    bean.marketPercent = marketPercent
    bean.bidSize = bidSize
    bean.bidPrice = bidPrice
    bean.askSize = askSize
    bean.askPrice = askPrice
    bean.volume = volume
    bean.lastSalePrice = lastSalePrice
    bean.lastSaleSize = lastSaleSize
    bean.lastSaleTime = lastSaleTime
    bean.lastUpdated = lastUpdated
    bean.sector = sector
    bean.securityType = securityType
    return bean
}

fun Iex.Tops.toBean(): TopsBean = this.toBean(TopsBean())