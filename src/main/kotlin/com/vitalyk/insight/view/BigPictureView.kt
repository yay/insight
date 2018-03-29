package com.vitalyk.insight.view

import com.vitalyk.insight.yahoo.getDistributionDays
import javafx.scene.Group
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.shape.Line
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.ArrayList
import javafx.animation.FadeTransition
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.chart.Axis
import javafx.scene.shape.Path
import javafx.util.Duration


class Candle internal constructor(private var seriesStyleClass: String?, private var dataStyleClass: String?) : Group() {
    private val line = Line()
    private val bar = Region()
    private var openAboveClose = true
//    private val tooltip = Tooltip()

    init {
        isAutoSizeChildren = false
        children.addAll(line, bar)
//        updateStyleClasses()
//        tooltip.graphic = TooltipContent()
//        Tooltip.install(bar, tooltip)
    }

    fun setSeriesAndDataStyleClasses(seriesStyleClass: String,
                                     dataStyleClass: String) {
//        this.seriesStyleClass = seriesStyleClass
//        this.dataStyleClass = dataStyleClass
//        updateStyleClasses()
    }

    fun update(closeOffset: Double, highOffset: Double,
               lowOffset: Double, candleWidth: Double) {
        var candleWidth = candleWidth
        openAboveClose = closeOffset > 0
//        updateStyleClasses()
        line.startY = highOffset
        line.endY = lowOffset
        if (candleWidth == -1.0) {
            candleWidth = bar.prefWidth(-1.0)
        }
        if (openAboveClose) {
            bar.resizeRelocate(-candleWidth / 2, 0.0,
                candleWidth, closeOffset)
        } else {
            bar.resizeRelocate(-candleWidth / 2, closeOffset,
                candleWidth, -closeOffset)
        }
    }

//    fun updateTooltip(open: Double, close: Double, high: Double, low: Double) {
//        val tooltipContent = tooltip.graphic as TooltipContent
//        tooltipContent.update(open, close, high, low)
//    }

//    private fun updateStyleClasses() {
//        val aboveClose = if (openAboveClose) "open-above-close" else "close-above-open"
//        styleClass.setAll("candlestick-candle",
//            seriesStyleClass, dataStyleClass)
//        line.styleClass.setAll("candlestick-line",
//            seriesStyleClass, dataStyleClass,
//            aboveClose)
//        bar.styleClass.setAll("candlestick-bar",
//            seriesStyleClass, dataStyleClass,
//            aboveClose)
//    }
}

/**
 * A candlestick chart is a style of bar-chart used primarily to describe
 * price movements of a security, derivative, or currency over time.
 *
 * The Data Y value is used for the opening price and then the
 * close, high and low values are stored in the Data's
 * extra value property using a CandleStickExtraValues object.
 */
class CandleStickChart(xAxis: Axis<Number>, yAxis: Axis<Number>) : XYChart<Number, Number>(xAxis, yAxis) {

    init {
//        val candleStickChartCss = javaClass.getResource("CandleStickChart.css").toExternalForm()
//        stylesheets.add(candleStickChartCss)
        animated = false
        xAxis.animated = false
        yAxis.animated = false
    }

    /**
     * Construct a new CandleStickChart with the given axis and data.
     * reflected in the chart.
     */
    constructor(xAxis: Axis<Number>, yAxis: Axis<Number>,
                data: ObservableList<XYChart.Series<Number, Number>>) : this(xAxis, yAxis) {
        setData(data)
    }

    /**
     * Called to update and layout the content for the plot.
     */
    override fun layoutPlotChildren() {
        // we have nothing to layout if no data is present
        if (data == null) {
            return
        }
        // update candle positions
        data.forEach { series ->
            val iter = getDisplayedDataIterator(series)

            var seriesPath: Path? = null
            if (series.node is Path) {
                seriesPath = series.node as Path
                seriesPath.elements.clear()
            }

            val yAxis = yAxis
            val xAxis = xAxis

            while (iter.hasNext()) {
                val data = iter.next()
                val xVal = getCurrentDisplayedXValue(data)
                val yVal = getCurrentDisplayedYValue(data)
                val xPos = xAxis.getDisplayPosition(xVal)
                val yPos = yAxis.getDisplayPosition(yVal)
                val node = data.node
                val extra = data.extraValue as? CandleStickExtraValues

                if (node is Candle && extra != null) {
                    val close = yAxis.getDisplayPosition(extra.close)
                    val high = yAxis.getDisplayPosition(extra.high)
                    val low = yAxis.getDisplayPosition(extra.low)
                    // calculate candle width
                    var candleWidth = -1.0
                    if (xAxis is NumberAxis) {
                        // use 90% width between ticks
                        val unit = xAxis.getDisplayPosition(xAxis.tickUnit)
                        candleWidth = unit * 0.90
                    }
                    // update candle
                    node.update(close - yPos, high - yPos, low - yPos, candleWidth)
//                    itemNode.updateTooltip(item.yValue.toDouble(),
//                        extra!!.getClose(), extra!!.getHigh(),
//                        extra!!.getLow())

                    // position the candle
                    node.layoutX = xPos
                    node.layoutY = yPos

                    seriesPath?.let {
                        val ave = yAxis.getDisplayPosition(extra.average)
                        if (it.elements.isEmpty()) {
                            it.elements.add(MoveTo(xPos, ave))
                        } else {
                            it.elements.add(LineTo(xPos, ave))
                        }
                    }
                }
            }
        }
    }

    override fun dataItemChanged(item: XYChart.Data<Number, Number>) {}

    override fun dataItemAdded(series: XYChart.Series<Number, Number>,
                               itemIndex: Int,
                               item: XYChart.Data<Number, Number>) {
        val candle = createCandle(data.indexOf(series), item, itemIndex)
        if (shouldAnimate()) {
            candle.opacity = 0.0
            plotChildren.add(candle)
            // fade in new candle
            val ft = FadeTransition(Duration.millis(500.0), candle)
            ft.toValue = 1.0
            ft.play()
        } else {
            plotChildren.add(candle)
        }
        // always draw average line on top
        if (series.node != null) {
            series.node.toFront()
        }
    }

    override fun dataItemRemoved(item: XYChart.Data<Number, Number>,
                                 series: XYChart.Series<Number, Number>) {
        val candle = item.node
        if (shouldAnimate()) {
            // fade out old candle
            val ft = FadeTransition(Duration.millis(500.0), candle)
            ft.toValue = 0.0
            ft.setOnFinished { actionEvent: ActionEvent -> plotChildren.remove(candle) }
            ft.play()
        } else {
            plotChildren.remove(candle)
        }
    }

    override fun seriesAdded(series: XYChart.Series<Number, Number>, seriesIndex: Int) {
        // Handle any data already in series.
        series.data.forEachIndexed { index, datum ->
            val candle = createCandle(seriesIndex, datum, index)
            if (shouldAnimate()) {
                candle.opacity = 0.0
                plotChildren.add(candle)
                // Fade in new candle.
                FadeTransition(Duration.millis(500.0), candle).let {
                    it.toValue = 1.0
                    it.play()
                }
            } else {
                plotChildren.add(candle)
            }
        }
        // Create series path.
        Path().let {
            it.getStyleClass().setAll("candlestick-average-line", "series$seriesIndex")
            series.node = it
            plotChildren.add(it)
        }
    }

    override fun seriesRemoved(series: XYChart.Series<Number, Number>) {
        // Remove all candle nodes.
        for (datum in series.data) {
            val candle = datum.node
            if (shouldAnimate()) {
                // Fade out old candle.
                val fade = FadeTransition(Duration.millis(500.0), candle)
                fade.toValue = 0.0
                fade.setOnFinished { plotChildren.remove(candle) }
                fade.play()
            } else {
                plotChildren.remove(candle)
            }
        }
    }

    /**
     * Create a new Candle node to represent a single data item.
     */
    private fun createCandle(seriesIndex: Int, item: XYChart.Data<*, *>, itemIndex: Int): Node {
        var candle = item.node
        // Check if candle has already been created.
        if (candle is Candle) {
            candle.setSeriesAndDataStyleClasses("series$seriesIndex", "data$itemIndex")
        } else {
            candle = Candle("series$seriesIndex", "data$itemIndex")
            item.setNode(candle)
        }
        return candle
    }

    /**
     * This is called when the range has been invalidated and we need to
     * update it. If the axis are auto ranging then we compile a list of
     * all data that the given axis has to plot and call invalidateRange()
     * on the axis passing it that data.
     */
    override fun updateAxisRange() {
        // For candle stick chart we need to override this method as we need
        // to let the axis know that they need to be able to cover the area
        // occupied by the high to low range not just its center data value.
        val xAxis = xAxis
        val yAxis = yAxis

        var xData: MutableList<Number>? = null
        var yData: MutableList<Number>? = null

        if (xAxis.isAutoRanging) {
            xData = mutableListOf()
        }
        if (yAxis.isAutoRanging) {
            yData = mutableListOf()
        }

        if (xData != null || yData != null) {
            for (series in data) {
                for (datum in series.data) {
                    if (xData != null) {
                        xData.add(datum.xValue)
                    }
                    if (yData != null) {
                        val extras = datum.extraValue as? CandleStickExtraValues
                        if (extras != null) {
                            yData.add(extras.high)
                            yData.add(extras.low)
                        } else {
                            yData.add(datum.yValue)
                        }
                    }
                }
            }
            if (xData != null) {
                xAxis.invalidateRange(xData)
            }
            if (yData != null) {
                yAxis.invalidateRange(yData)
            }
        }
    }
}

data class CandleStickExtraValues(
    val close: Double,
    val high: Double,
    val low: Double,
    val average: Double
)

class DistributionChart(val symbol: String, title: String? = null) : Fragment(title) {
    lateinit var series:  XYChart.Series<String, Number>

    override val root = scatterchart(title, CategoryAxis(), NumberAxis()) {
        isLegendVisible = false
        animated = false
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS

        val dd = getDistributionDays(symbol)
        val dateFormat = SimpleDateFormat("MMM d")

        val chart = this
        val xAxis = this.xAxis
        val yAxis = this.yAxis
        xAxis.animated = false
        yAxis.animated = false
        (yAxis as NumberAxis).isForceZeroInRange = false
        series("min/max") {
            dd.first?.let {
                val min = it.minBy { it.low }
                val max = it.maxBy { it.high }
                data(dateFormat.format(it.first().date), min!!.low)
                data(dateFormat.format(it.first().date), max!!.high)
            }
        }
        series = series("Distribution days") {
            dd.first?.let {
                it.forEachIndexed { index, point ->
                    data(dateFormat.format(point.date), point.close) {
//                        extraValue = point
                        val xCoord = chart.xAxis.toNumericValue(xValue)
                        node = group {
                            line {
                                startX = xCoord
                                endX = xCoord

                                startY = yAxis.toNumericValue(point.high)
                                endY = yAxis.toNumericValue(point.low)
                            }
                            rectangle {
                                x = xCoord - 3.0
                                width = 6.0

                                val openCoord = yAxis.toNumericValue(point.open)
                                val closeCoord = yAxis.toNumericValue(point.close)
                                y = Math.min(openCoord, closeCoord)
                                height = Math.abs(closeCoord - openCoord)
                            }
                        }
                    }
                }
            }
//            dd.first?.let {
//                val points = it
//                dd.second.forEach {
//                    val point = points[it]
////                    data(points[it], point.close)
//                    data(dateFormat.format(point.date), point.close) {
//                        val xCoord = chart.xAxis.toNumericValue(xValue)
//                        node = group {
//                            line {
//                                startX = xCoord
//                                endX = xCoord
//
//                                startY = chart.yAxis.toNumericValue(point.low)
//                                endY = chart.yAxis.toNumericValue(point.high)
//                            }
//                            rectangle {
//                                x = xCoord - 5.0
//                                width = 10.0
//
//                                y = chart.yAxis.toNumericValue(point.open)
//                                height = Math.abs(y - chart.yAxis.toNumericValue(point.close))
//                            }
//                        }
//                    }
//                }
//            }
        }
    }
}

class BigPictureView : View("Big Picture") {
    override val root = vbox {
        hbox {
            vgrow = Priority.ALWAYS
            this += DistributionChart("^GSPC", "S&P 500")
            this += DistributionChart("^IXIC", "NASDAQ Composite")
        }
        hbox {
            vgrow = Priority.ALWAYS
            this += DistributionChart("^RUT", "Russel 2000")
            this += DistributionChart("^DJI", "Dow Jones Industrial Average")
        }
    }

    init {
        setWindowMinSize(800, 600)
    }
}