package com.vitalyk.insight.fragment

import com.vitalyk.insight.iex.Iex
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.util.*

class EarningsFragment : Fragment() {
    val earnings = SimpleObjectProperty<Iex.Earnings>()

    val text = SimpleStringProperty()

    val actualEpsProperty = SimpleDoubleProperty()
    val consensusEpsProperty = SimpleDoubleProperty()
    val estimatedEpsProperty = SimpleDoubleProperty()
    val announceTimeProperty = SimpleStringProperty()
    val numberOfEstimatesProperty = SimpleIntegerProperty()
    val epsSurpriseDollarProperty = SimpleDoubleProperty()
    val epsReportDateProperty = SimpleObjectProperty<Date>()
    val fiscalPeriodProperty = SimpleStringProperty()
    val fiscalEndDateProperty = SimpleObjectProperty<Date>()

    override val root = vbox {
        textarea(text)
    }

    init {
        earnings.onChange {
            it?.apply {
                actualEpsProperty.value = actualEps
                consensusEpsProperty.value = consensusEps
                estimatedEpsProperty.value = estimatedEps
                announceTimeProperty.value = announceTime
                numberOfEstimatesProperty.value = numberOfEstimates
                epsSurpriseDollarProperty.value = epsSurpriseDollar
                epsReportDateProperty.value = epsReportDate
                fiscalPeriodProperty.value = fiscalPeriod
                fiscalEndDateProperty.value = fiscalEndDate
            }
        }
        text.onChange {
            if (it != null) {

            }
        }
    }
}