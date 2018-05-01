package com.vitalyk.insight.fragment

import com.vitalyk.insight.iex.Iex
import javafx.beans.property.*
import javafx.geometry.Orientation
import tornadofx.*
import java.util.*

class AssetStats : Fragment() {
    val stats = SimpleObjectProperty<Iex.AssetStats>()

    val symbolProperty = SimpleStringProperty()
    val companyProperty = SimpleStringProperty()
    val marketCapProperty = SimpleLongProperty()
    val betaProperty = SimpleDoubleProperty()
    val shortInterestProperty = SimpleLongProperty()
    val shortDateProperty = SimpleObjectProperty<Date>()
    val dividendRateProperty = SimpleDoubleProperty()
    val dividendYieldProperty = SimpleDoubleProperty()
    val consensusEpsProperty = SimpleDoubleProperty()
    val latestEpsProperty = SimpleDoubleProperty()
    val latestEpsDateProperty = SimpleObjectProperty<Date>()
    val ttmEpsProperty = SimpleDoubleProperty()
    val sharesOutstandingProperty = SimpleLongProperty()
    val floatProperty = SimpleLongProperty()
    val roeProperty = SimpleDoubleProperty()
    val ebitdaProperty = SimpleLongProperty()
    val revenueProperty = SimpleLongProperty()
    val grossProfitProperty = SimpleLongProperty()
    val cashProperty = SimpleLongProperty()
    val debtProperty = SimpleLongProperty()
    val revenuePerShareProperty = SimpleDoubleProperty()
    val revenuePerEmployeeProperty = SimpleDoubleProperty()
    val peRatioLowProperty = SimpleDoubleProperty()

    override val root = form {
        fieldset("Key Statistics", labelPosition = Orientation.VERTICAL) {
            hbox(20) {
                label()
            }
        }
    }

}