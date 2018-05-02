package com.vitalyk.insight.fragment

import com.vitalyk.insight.helpers.browseTo
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.yahoo.AssetProfile
import com.vitalyk.insight.yahoo.getAssetProfile
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.control.Alert
import javafx.scene.control.TextArea
import tornadofx.*

class AssetProfileFragment : Fragment() {
    val profile = SimpleObjectProperty<AssetProfile>()

    val countryProperty = SimpleStringProperty()
    val stateProperty = SimpleStringProperty()
    val cityProperty = SimpleStringProperty()
    val websiteProperty = SimpleStringProperty()
    val industryProperty = SimpleStringProperty()
    val sectorProperty = SimpleStringProperty()
    val longBusinessSummaryProperty = SimpleStringProperty()
    val fullTimeEmployeesProperty = SimpleIntegerProperty()

    override val root = vbox {
        form {
            fieldset(labelPosition = Orientation.VERTICAL) {
                hbox(20) {
                    hbox(5) {
                        label("Country:")
                        label(countryProperty)
                    }
                    hbox(5) {
                        label("City:")
                        label(cityProperty)
                    }
                    hbox(5) {
                        label("State:")
                        label(stateProperty)
                    }
                }
                hbox(5) {
                    label("Website:")
                    label(websiteProperty) {
                        setOnMouseClicked {
                            browseTo(websiteProperty.value)
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
                hbox(5) {
                    label("Employees:")
                    label(fullTimeEmployeesProperty)
                }

                field("Business Summary:") {
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
                } ?: alert(Alert.AlertType.ERROR, "$symbol Asset Profile", "No profile available.")

                runAsync {
                    Iex.getEarnings(symbol)
                } ui {
                    fragment?.root?.children?.add(TextArea(it?.toString()))
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
                fullTimeEmployeesProperty.value = fullTimeEmployees
            }
        }
    }
}