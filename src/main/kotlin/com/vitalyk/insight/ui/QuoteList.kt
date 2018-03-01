package com.vitalyk.insight.ui

import com.vitalyk.insight.iex.IexApi.Quote
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.javafx.JavaFx
import tornadofx.*

class QuoteListStyles : Stylesheet() {
    init {
        label {
            and(selected) {
                textFill = Color.WHITE
            }
        }
    }
}

class QuoteList(title: String, private val getQuotes: () -> List<Quote>) : Fragment(title) {
    private lateinit var listView: ListView<Quote>
    private var updateJob: Job? = null

    override val root = vbox {
        val root = this
        hgrow = Priority.ALWAYS
        label(title) {
            alignment = Pos.CENTER
            maxWidth = Double.MAX_VALUE
            minHeight = 30.0
            style {
                font = Font.font(null, FontWeight.BOLD, 15.0)
            }
        }
        listView = listview {
            val labelFont = Font.font(15.0)

            vgrow = Priority.ALWAYS

            cellCache {
                vbox {
                    val changeColor = when {
                        it.change > 0 -> Color.GREEN
                        it.change < 0 -> Color.RED
                        else -> Color.GRAY
                    }
                    hbox {
                        label(it.symbol) {
                            textFill = Color.DODGERBLUE
                            font = Font.font("Verdana", FontWeight.BOLD, 15.0)
                            minWidth = 80.0
                        }
                        path {
                            moveTo(0.0, 0.0)
                            lineTo(12.0, 0.0)
                            lineTo(6.0, 10.0 * if (it.change > 0) 1.0 else -1.0)
                            closepath()
                            translateX = -8.0
                            translateY = 5.0
                            fill = changeColor
                            stroke = Color.WHITE
                            strokeWidth = 1.0
                        }
                        label("%.2f".format(it.change)) {
                            textFill = changeColor
                            font = labelFont
                        }
                        val changePercent = "%.2f".format(it.changePercent)
                        label(" ($changePercent%)") {
                            textFill = changeColor
                            font = labelFont
                        }
                        region {
                            hgrow = Priority.ALWAYS
                        }
                        label("${it.latestPrice}") {
                            font = labelFont
                            padding = Insets(0.0, 0.0, 0.0, 20.0)
                        }
                    }
                    label(it.companyName) {
                        textFill = Color.GRAY
                    }
                }
            }

            startUpdating()
        }
    }

    fun updateQuotes(quotes: List<Quote>) {
        val selectedSymbol = listView.selectedItem?.symbol

        listView.items.setAll(quotes)

        selectedSymbol?.let {
            val index = listView.items.indexOfFirst { item ->
                item.symbol == selectedSymbol
            }
            listView.selectionModel.select(index)
        }
    }

    fun startUpdating() {
        updateJob.apply {
            if (this == null || !this.isActive || (this.isActive && this.cancel())) {
                updateJob = launch(JavaFx) {
                    while (this.isActive && root.isVisible && root.parent != null) {
                        val quotes = async { getQuotes() }.await()
                        updateQuotes(quotes)
                        delay(1000L)
                    }
                }
            }
        }
    }

    fun stopUpdating() {
        updateJob?.cancel()
    }

    val isUpdating: Boolean
        get() = updateJob?.isActive ?: false
}
