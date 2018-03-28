package com.vitalyk.insight.iex

import javafx.beans.property.*
import java.util.*
import tornadofx.getValue
import tornadofx.setValue


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

fun IexApi.Tops.toBean() =
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

