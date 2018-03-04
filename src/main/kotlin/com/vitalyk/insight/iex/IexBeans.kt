package com.vitalyk.insight.iex

import com.vitalyk.insight.iex.IexApi.DayChartPoint
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
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

fun DayChartPoint.toDayChartPointBean() =
    DayChartPointBean().let {
        it.change = this.change
        it.changeOverTime = this.changeOverTime
        it.changePercent = this.changePercent
        it.close = this.close
        it.date = this.date
        it.high = this.high
        it.label = this.label
        it.low = this.low
        it.open = this.open
        it.unadjustedVolume = this.unadjustedVolume
        it.volume = this.volume
        it.vwap = this.vwap
        it
    }