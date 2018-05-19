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
    val symbolProperty = SimpleStringProperty()
    var symbol by symbolProperty

    val marketPercentProperty = SimpleDoubleProperty()
    var marketPercent by marketPercentProperty

    val bidSizeProperty = SimpleIntegerProperty()
    var bidSize by bidSizeProperty

    val bidPriceProperty = SimpleDoubleProperty()
    var bidPrice by bidPriceProperty

    val askSizeProperty = SimpleIntegerProperty()
    var askSize by askSizeProperty

    val askPriceProperty = SimpleDoubleProperty()
    var askPrice by askPriceProperty

    val volumeProperty = SimpleLongProperty()
    var volume by volumeProperty

    val lastSalePriceProperty = SimpleDoubleProperty()
    var lastSalePrice by lastSalePriceProperty

    val lastSaleSizeProperty = SimpleIntegerProperty()
    var lastSaleSize by lastSaleSizeProperty

    val lastSaleTimeProperty = SimpleObjectProperty<Date>()
    var lastSaleTime by lastSaleTimeProperty

    val lastUpdatedProperty = SimpleObjectProperty<Date>()
    var lastUpdated by lastUpdatedProperty

    val sectorProperty = SimpleStringProperty()
    var sector by sectorProperty

    val securityTypeProperty = SimpleStringProperty()
    var securityType by securityTypeProperty
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