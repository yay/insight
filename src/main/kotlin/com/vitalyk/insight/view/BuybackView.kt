package com.vitalyk.insight.view

import com.vitalyk.insight.iex.calculateNewPrice
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.scene.control.TextFormatter
import javafx.scene.layout.Priority
import kotlinx.coroutines.experimental.async
import tornadofx.*

class BuybackView : View("Share Buyback") {
    override val root = vbox {
        form {
            val symbolProperty = SimpleStringProperty("")
            val amountProperty = SimpleLongProperty(0)
            val newPriceProperty = SimpleDoubleProperty(0.0)
            hbox(20) {
                fieldset("Price after buyback calculator") {
                    vbox {
                        field("Company ticker") {
                            textfield(symbolProperty) {
                                hgrow = Priority.ALWAYS
                                textFormatter = TextFormatter<String> {
                                    it.text = it.text.toUpperCase()
                                    it
                                }
                            }
                        }
                        field("Dollar buyback amount") { textfield(amountProperty) { hgrow = Priority.ALWAYS } }
                        field("Result") { textfield(newPriceProperty) { hgrow = Priority.ALWAYS } }
                        hbox {
                            padding = Insets(10.0, 0.0, 0.0, 0.0)
                            spacer {}
                            button("Calculate").action {
                                async {
                                    calculateNewPrice(symbolProperty.get(), amountProperty.get())?.let {
                                        runLater {
                                            newPriceProperty.set(it.newPrice)
                                            println(it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}