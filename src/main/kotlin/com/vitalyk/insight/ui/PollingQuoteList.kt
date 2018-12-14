package com.vitalyk.insight.ui

import com.vitalyk.insight.fragment.AssetProfileFragment
import com.vitalyk.insight.fragment.DayChartFragment
import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.Iex.Quote
import com.vitalyk.insight.main.HttpClients
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Popup
import kotlinx.coroutines.*
import tornadofx.*

class QuoteChart : Fragment() {

    val chart: LineChart<String, Number> = linechart(null, CategoryAxis(), NumberAxis()) {
        animated = false
        createSymbols = false
        isLegendVisible = false
        vgrow = Priority.ALWAYS
    }

    override val root = region {
        setMinSize(400.0, 300.0)
        this += chart
    }
}

class PollingQuoteList(title: String, private val getQuotes: () -> List<Quote>?) : Fragment(title) {
    private var updateJob: Job? = null
    private val iex = Iex(HttpClients.main)

    val titleLabel = label(title) {
        alignment = Pos.CENTER
        maxWidth = Double.MAX_VALUE
        minHeight = 30.0
        style {
            font = Font.font(null, FontWeight.BOLD, 15.0)
        }
    }

    val listView = listview<Quote> {
        val labelFont = Font.font(15.0)

        vgrow = Priority.ALWAYS

        contextmenu {
            item("Profile").action {
                selectedItem?.apply {
                    AssetProfileFragment.show(symbol)
                }
            }
            menu("Chart") {
                Iex.Range.values().forEach { range ->
                    item(range.value.name).action {
                        selectedItem?.let { selectedItem ->
                            runAsync {
                                iex.getDayChart(selectedItem.symbol, range) ?: emptyList()
                            } ui { points ->
                                find(DayChartFragment::class).let {
                                    it.updateChart(selectedItem.symbol, points)
                                    it.openModal()
                                }
                            }
                        }
                    }
                }
            }
        }

        cellFormat {
            val change = it.change
            val symbol = it.symbol

//            tooltip(it.companyName, Circle().apply { radius = 30.0; fill = Color.RED }) {
//
//            }

//            cell.setOnMouseEntered(::onCellMouseEntered)
//            cell.setOnMouseExited(::onCellMouseExited)
//            cell.setOnMouseMoved(::onCellMouseMoved)

//            this.setOnMouseMoved { e ->
//                println("${e.target::class}, ${e.source::class}")
//            }

            graphic = vbox {
                val changeColor = when {
                    change > 0.0 -> Color.GREEN
                    change < 0.0 -> Color.RED
                    else -> Color.GRAY
                }
                hbox {
                    label(symbol) {
                        textFill = Color.DODGERBLUE
                        font = Font.font("Verdana", FontWeight.BOLD, 15.0)
                        minWidth = 80.0
                    }
                    if (change == 0.0) {
                        circle {
                            radius = 6.0
                        }
                    } else {
                        path {
                            moveTo(0.0, 0.0)
                            lineTo(12.0, 0.0)
                            lineTo(6.0, 10.0 * if (change > 0) -1.0 else 1.0)
                            closepath()
                        }
                    }.apply {
                        translateX = -8.0
                        translateY = 3.0
                        fill = changeColor
                        stroke = Color.WHITE
                        strokeWidth = 1.0
                    }
                    label("%.2f".format(change)) {
                        textFill = changeColor
                        font = labelFont
                    }
                    val changePercent = " (%.2f%%)".format(it.changePercent * 100)
                    label(changePercent) {
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
    }

    override val root = vbox {
        hgrow = Priority.ALWAYS
        this += titleLabel
        this += listView
    }

    val quoteChart: Popup by lazy {
//        find(QuoteChart::class).openWindow(
//            stageStyle = StageStyle.UNDECORATED,
//            escapeClosesWindow = false
//        )!!

//        https://github.com/edvin/tornadofx/wiki/Components
            Popup().apply {
                setWindowMinSize(400.0, 300.0)
                add(QuoteChart())
                content.add(Circle().apply {
                    radius = 50.0
                    isFocusTraversable = false
                    fill = Color.RED
                })
            }
    }

    private var chartHideJob: Job? = null

    fun onCellMouseEntered(e: MouseEvent) {
//        println("enter")
        val cell = e.source as SmartListCell<Quote>
        val quote = cell.item

//        println(e.target::class.java)
//        println(e.source::class.java)

        chartHideJob?.cancel()

        quoteChart.show(primaryStage)
//        quoteChart.let {
//            it.x = 0.0 //e.sceneX + 100.0
//            it.y = 0.0 //e.sceneY + 100.0
//        }
    }

    fun onCellMouseExited(e: MouseEvent) {
        val cell = e.source as SmartListCell<Quote>
//        println(cell.parent::class)
//        println(cell.item.latestPrice)
        if (cell.parent != listView) {
            quoteChart.hide()
        }
//        chartHideJob = launch(JavaFx) {
//            Thread.sleep(200)
//            if (isActive) {
//                quoteChart.hide()
//            }
//        }
//        println("exit")
    }

    fun onCellMouseMoved(e: MouseEvent) {
//        println("moved")
//        val cell = e.target as ListCell<Quote>
        quoteChart.let {
            it.x = e.sceneX + 100.0
            it.y = e.sceneY + 100.0
        }
//        quoteChart.move(0.millis, Point2D(e.sceneX, e.sceneY))
    }

    fun updateQuotes(quotes: List<Quote>?) {
        val selectedSymbol = listView.selectedItem?.symbol

        quotes?.let {
            listView.items.setAll(it)
        }

        selectedSymbol?.let {
            val index = listView.items.indexOfFirst { item ->
                item.symbol == selectedSymbol
            }
            listView.selectionModel.select(index)
        }
    }

    fun startUpdating() {
        updateJob.apply {
            if (this == null || !this.isActive || (this.isActive.apply { cancel() })) {
                updateJob = GlobalScope.launch {
                    while (isActive && root.isVisible && root.parent != null) {
                        val quotes = async { getQuotes() }.await()
                        runLater {
                            updateQuotes(quotes)
                        }
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
