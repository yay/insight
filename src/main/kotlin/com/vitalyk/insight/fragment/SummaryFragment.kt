package com.vitalyk.insight.fragment

import com.vitalyk.insight.yahoo.AssetProfile
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import tornadofx.*

class SummaryFragment : Fragment("Company Summary") {
    val assetProfile = AssetProfileFragment()

    override val root = vbox {
        this += assetProfile
    }
}

class AssetProfileFragment : Fragment("Asset Profile") {
    val profile = SimpleObjectProperty<AssetProfile>()

    val longBusinessSummaryProperty = SimpleStringProperty()

    override val root = vbox {
        textarea(longBusinessSummaryProperty) {
            isEditable = false
            isWrapText = true
            vgrow = Priority.ALWAYS
        }
    }

    init {
        profile.onChange {
            it?.apply {
                longBusinessSummaryProperty.value = longBusinessSummary
            }
        }
    }
}