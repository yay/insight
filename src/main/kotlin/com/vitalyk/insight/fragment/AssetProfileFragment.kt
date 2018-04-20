package com.vitalyk.insight.fragment

import com.vitalyk.insight.yahoo.AssetProfile
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.*
import java.awt.Desktop
import java.net.URI

class AssetProfileFragment : Fragment() {
    val profile = SimpleObjectProperty<AssetProfile>()

    val countryProperty = SimpleStringProperty()
    val stateProperty = SimpleStringProperty()
    val cityProperty = SimpleStringProperty()
    val websiteProperty = SimpleStringProperty()
    val industryProperty = SimpleStringProperty()
    val sectorProperty = SimpleStringProperty()
    val longBusinessSummaryProperty = SimpleStringProperty()

    override val root = vbox {
        form {
            fieldset(labelPosition = Orientation.VERTICAL) {
                hbox(20) {
                    hbox(5) {
                        label("Country:")
                        label(countryProperty)
                    }
                    hbox(5) {
                        label("State:")
                        label(stateProperty)
                    }
                    hbox(5) {
                        label("City:")
                        label(cityProperty)
                    }
                }
                hbox(5) {
                    label("Website:")
                    label(websiteProperty) {
                        setOnMouseClicked {
                            Desktop.getDesktop().browse(URI(websiteProperty.value))
                        }
                        onHover {
                            isUnderline = it
                        }
                    }
                }
                hbox(5) {
                    label("Industry:")
                    label(industryProperty)
                }
                hbox(5) {
                    label("Sector:")
                    label(sectorProperty)
                }

                field("Business Summary") {
                    textarea(longBusinessSummaryProperty) {
                        isEditable = false
                        isWrapText = true
                        vgrow = Priority.ALWAYS
                    }
                }
            }
        }
    }

    init {
        profile.onChange {
            it?.apply {
                countryProperty.value = country
                stateProperty.value = state
                cityProperty.value = city
                websiteProperty.value = website
                industryProperty.value = industry
                sectorProperty.value = sector
                longBusinessSummaryProperty.value = longBusinessSummary
            }
        }
    }
}