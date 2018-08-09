package com.vitalyk.insight.misc

import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.viz.scale.scaleLinear
import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.Scene
import javafx.scene.SubScene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Path
import javafx.stage.Stage
import tornadofx.*
import kotlin.math.roundToInt

data class MinMax(
    val minX: Double,
    val maxX: Double,
    val minY: Double,
    val maxY: Double
)

inline fun GraphicsContext.group(op: GraphicsContext.() -> Unit) {
    save()
    op(this)
    restore()
}

inline fun GraphicsContext.path(op: GraphicsContext.() -> Unit) {
    beginPath()
    op(this)
    closePath()
}

open class Series<T> {
}

data class OHLC(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double
)

data class MyOhMy(
    val o: Double,
    val h: Double,
    val l: Double,
    val c: Double
)

class CandleStick<T>(
    var getFields: (T) -> OHLC
) : Series<T>() {
}

class Axis<T>(position: Position) {
    enum class Position {
        TOP, RIGHT, BOTTOM, LEFT
    }

    var position: Position = position
}

class Chart<T> {
    var store: List<T> = listOf()

    var series: List<Series<T>> = listOf()

    var axes: List<Axis<T>> = listOf(
        Axis(Axis.Position.LEFT),
        Axis(Axis.Position.BOTTOM)
    )

    init {
        val myCandle = CandleStick<MyOhMy> { OHLC(it.o, it.h, it.l, it.c) }
    }
}

class BasicApp: Application() {

//    companion object {
//        @JvmStatic
//        fun main(args: Array<String>) {
//            Application.launch(BasicApp::class.java, *args)
//        }
//    }

    override fun start(primaryStage: Stage) {
        val stackPane = StackPane()
        val scene = Scene(stackPane)
        primaryStage.scene = scene
        primaryStage.title = "Basic JavaFx app"

        val canvas = Canvas().apply {
            val ctx = this.graphicsContext2D
            width = 800.0
            height = 800.0
        }

        val overlayScene = object : SubScene(Pane(Button("Vitaly").apply {
            layoutX = 100.0
            layoutY = 200.0
        }, Button("Kravchenko").apply { layoutX = 200.0; layoutY = 400.0 }).apply {
            //            background = Background(BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY))
            style {
                backgroundColor = multi(Color.TRANSPARENT)
            }
//            isPickOnBounds = false
        }, 0.0, 0.0) {
//            override fun contains(p: Point2D?): Boolean {
////                return root.getChildList()!!.any { it.contains(p) }
//                return false
//            }
        }.apply {
            fill = Color.TRANSPARENT
            isPickOnBounds = false
//            isMouseTransparent = true
        }

        val iex = Iex(HttpClients.main)
        iex.getDayChart("NFLX", Iex.Range.M3)?.let {
            val minX = 0.0
            val maxX = it.size.toDouble()
            val minY = it.minBy { it.low }?.low ?: 0.0
            val maxY = it.maxBy { it.high }?.high ?: 1.0
            val minMax = MinMax(minX, maxX, minY, maxY)

            canvas.apply {
                val ctx = graphicsContext2D
                renderChart(ctx, it, minMax)

                primaryStage.scene.widthProperty().addListener { _, _, value ->
                    width = value.toDouble()
                    renderChart(ctx, it, minMax)
                }
                primaryStage.scene.heightProperty().addListener { _, _, value ->
                    height = value.toDouble()
                    renderChart(ctx, it, minMax)
                }
            }

            overlayScene.apply {
                val scene = this
                style {
                    borderWidth = multi(box(1.px))
                    borderColor = multi(box(Color.RED))
                }
                primaryStage.scene.widthProperty().addListener { _, _, value ->
                    width = value.toDouble()
                }
                primaryStage.scene.heightProperty().addListener { _, _, value ->
                    height = value.toDouble()
                }

                var startX = 0.0
                var startY = 0.0
                onMousePressed = EventHandler { e ->
                    startX = e.x
                    startY = e.y
                }
                val path = scene.root.path {
                    onHover {
                        fill = if (it) Color.RED else Color.BLACK
                        stroke = if (it) Color.RED else Color.BLACK
                    }
                }
                onMouseDragged = EventHandler { e ->
                    val ctx = canvas.graphicsContext2D
                    renderChart(ctx, it, minMax)

//                    ctx.clearRect(0.0, 0.0, ctx.canvas.width, ctx.canvas.height)

                    fun Point2D.move(distance: Double, angle: Double) =
                        Point2D(x + distance * Math.cos(angle), y + distance * Math.sin(angle))

                    ctx.group {
                        setLineDashes(4.0, 4.0)
                        stroke = Color.RED
                        val offset = -.5 * lineWidth

                        when {
                            e.isMetaDown -> path.apply {
                                fill = Color.BLACK
                                strokeWidth = 2.0
                                elements.clear()
                                val angle = Math.atan2(e.y - startY, e.x - startX)
                                val rightAngle = angle + .5 * Math.PI
                                moveTo(startX, startY)
                                lineTo(e.x, e.y)
                                closepath()
                                val bottom = Point2D(e.x, e.y).move(-10.0, angle)
                                val left = bottom.move(-3.0, rightAngle)
                                val right = bottom.move(3.0, rightAngle)
                                moveTo(left.x, left.y)
//                                moveTo(e.x + 10.0 * Math.cos(angle), e.y + 10.0 * Math.sin(angle))
//                                lineTo(e.x - 5.0 * Math.cos(rightAngle), e.y - 5.0 * Math.sin(rightAngle))
                                lineTo(right.x, right.y)
                                lineTo(e.x, e.y)
                                closepath()
                            }
                            // MoveTo(startX, startY)
                            // LineTo(e.x, e.y)
                            // beginPath()
                            // moveTo(startX, startY)
                            // lineTo(e.x, e.y)
                            // stroke()
                            e.isAltDown -> {
//                                println(e.pickResult)
                                e.pickResult.intersectedNode?.apply {
                                    (this as? Path)?.moveTo(e.x, e.y)
                                }
                            }
                            else -> {
                                beginPath()
                                moveTo(e.x + offset, 0.0)
                                lineTo(e.x + offset, ctx.canvas.height)

                                moveTo(0.0, e.y + offset)
                                lineTo(ctx.canvas.width, e.y + offset)
                                stroke()
                            }
                        }
                    }
                }
            }
        }

        stackPane.children.addAll(canvas, overlayScene)

        primaryStage.show()
    }
}

fun renderChart(ctx: GraphicsContext, points: List<Iex.DayChartPoint>, minMax: MinMax) {
//    val minX = 0
//    val maxX = points.size
//    val minY = points.minBy { it.low }?.low ?: 0.0
//    val maxY = points.maxBy { it.high }?.high ?: 1.0
    val (minX, maxX, minY, maxY) = minMax
//    ctx.fill = Color.WHITE
//    ctx.fillRect(0.0, 0.0, ctx.canvas.width, ctx.canvas.height)

//    ctx.fill = Color.WHITE
//    ctx.fillRect(0.0, 0.0, ctx.canvas.width, ctx.canvas.height)
    ctx.group {
        clearRect(0.0, 0.0, ctx.canvas.width, ctx.canvas.height)

        val xScale = scaleLinear<Double> {
            domain(minX, maxX)
            range(0.0, ctx.canvas.width)
        }
        val yScale = scaleLinear<Double> {
            domain(minY, maxY)
            range(ctx.canvas.height, 0.0)
        }

        val barWidth = 3.0
        val halfBarWidth = barWidth / 2.0

        stroke = Color.BLACK
        val offset = -.5 * lineWidth
        beginPath()
            points.forEachIndexed { i, p ->
                val x = xScale(i).roundToInt() + offset
                moveTo(x, yScale(p.high))
                lineTo(x, yScale(p.low))
            }
        stroke()

        points.forEachIndexed { i, p ->
            val x = xScale(i).roundToInt() + offset
            val fill = if (p.close > p.open) Color.GREEN else Color.RED
            ctx.fill = fill
            val rectX = x - halfBarWidth
            val rectY1 = yScale(p.close)
            val rectY2 = yScale(p.open)
            fillRect(rectX, Math.min(rectY1, rectY2), barWidth, Math.abs(rectY2 - rectY1))
        }
    }
}

fun main(args: Array<String>) {
    Application.launch(BasicApp::class.java, *args)
}