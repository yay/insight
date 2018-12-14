package com.vitalyk.insight.view

import com.vitalyk.insight.helpers.toPercentString
import com.vitalyk.insight.iex.calculateNewPrice
import com.vitalyk.insight.ui.symbolfield
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import tornadofx.*

class BuybackView : View("Share Buyback") {

    override val root = vbox {
        form {
            val symbolProperty = SimpleStringProperty("")
            val amountProperty = SimpleLongProperty(0)
            val prevCloseProperty = SimpleDoubleProperty(0.0)
            val latestPriceProperty = SimpleDoubleProperty(0.0)
            val expectedPriceProperty = SimpleDoubleProperty(0.0)
            val upsideFromClose = SimpleStringProperty("")
            val upsideFromLatest = SimpleStringProperty("")

            hbox(20) {
                fieldset("Price after buyback calculator") {
                    vbox {
                        field("Company ticker") { symbolfield(symbolProperty) }
                        field("Dollar buyback amount") { textfield(amountProperty) }
                        field("Previous close") { textfield(prevCloseProperty) { isEditable = false } }
                        field("Latest price") { textfield(latestPriceProperty) { isEditable = false } }
                        field("Expected price after buyback") {
                            label.style {
                                textFill = Color.CHOCOLATE
                            }
                            textfield(expectedPriceProperty) { isEditable = false }
                        }
                        field("Upside from close") { textfield(upsideFromClose) { isEditable = false } }
                        field("Upside from latest") { textfield(upsideFromLatest) { isEditable = false } }

                        children.forEach {
                            hgrow = Priority.ALWAYS
                        }

                        hbox {
                            padding = Insets(10.0, 0.0, 0.0, 0.0)
                            spacer {}
                            button("Calculate").action {
                                GlobalScope.async {
                                    calculateNewPrice(symbolProperty.get(), amountProperty.get())?.let {
                                        runLater {
                                            prevCloseProperty.set(it.previousClose)
                                            latestPriceProperty.set(it.latestPrice)
                                            expectedPriceProperty.set(it.expectedPrice)

                                            upsideFromClose.set((it.expectedPrice / it.previousClose).toPercentString())
                                            upsideFromLatest.set((it.expectedPrice / it.latestPrice).toPercentString())

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