package com.vitalyk.insight.fragment

import com.vitalyk.insight.iex.IexSymbols
import com.vitalyk.insight.yahoo.AssetProfile
import com.vitalyk.insight.yahoo.getAssetProfile
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.control.Alert
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
                    }
                }
            }
        }
    }

    companion object {
        var fragment: AssetProfileFragment? = null
        fun show(symbol: String) {
            runAsync {
                getAssetProfile(symbol)
            } ui {
                it?.let {
                    fragment = fragment ?: tornadofx.find(AssetProfileFragment::class)
                    fragment?.apply {
                        openWindow()
                        titleProperty.value = symbol
                        this.profile.value = it
                    }
                } ?: alert(Alert.AlertType.ERROR, "Asset Profile", "No profile available.")
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