package com.vitalyk.insight.misc

import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.main.HttpClients
import com.vitalyk.viz.scale.scaleLinear
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.math.roundToInt

data class MinMax(
    val minX: Double,
    val maxX: Double,
    val minY: Double,
    val maxY: Double
)

fun GraphicsContext.group(op: GraphicsContext.() -> Unit) {
    save()
    op(this)
    restore()
}

fun GraphicsContext.path(op: GraphicsContext.() -> Unit) {
    beginPath()
    op(this)
    closePath()
}

fun GraphicsContext.openPath(op: GraphicsContext.() -> Unit) {
    beginPath()
    op(this)
}

class BasicApp: Application() {

//    companion object {
//        @JvmStatic
//        fun main(args: Array<String>) {
//            Application.launch(BasicApp::class.java, *args)
//        }
//    }

    override fun start(primaryStage: Stage) {
        val canvas = Canvas().apply {
            val ctx = this.graphicsContext2D
            width = 800.0
            height = 800.0

            val iex = Iex(HttpClients.main)
            iex.getDayChart("NFLX", Iex.Range.M3)?.let {
                val minX = 0.0
                val maxX = it.size.toDouble()
                val minY = it.minBy { it.low }?.low ?: 0.0
                val maxY = it.maxBy { it.high }?.high ?: 1.0
                val minMax = MinMax(minX, maxX, minY, maxY)
                renderChart(ctx, it, minMax)

                primaryStage.widthProperty().addListener { _, _, value ->
                    width = value.toDouble()
                    renderChart(ctx, it, minMax)
                }
                primaryStage.heightProperty().addListener { _, _, value ->
                    height = value.toDouble()
                    renderChart(ctx, it, minMax)
                }

                onMouseDragged = EventHandler { e ->
                    renderChart(ctx, it, minMax)

//                    ctx.clearRect(0.0, 0.0, ctx.canvas.width, ctx.canvas.height)

                    ctx.group {
                        setLineDashes(4.0, 4.0)
                        stroke = Color.RED
                        val offset = -.5 * lineWidth
                        openPath {
                            moveTo(e.x + offset, 0.0)
                            lineTo(e.x + offset, ctx.canvas.height)

                            moveTo(0.0, e.y + offset)
                            lineTo(ctx.canvas.width, e.y + offset)
                        }
                        stroke()
                    }
                }
            }
        }

        val scene = Scene(Pane(canvas))
        primaryStage.scene = scene
        primaryStage.title = "Basic JavaFx app"
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
        openPath {
            points.forEachIndexed { i, p ->
                val x = xScale(i).roundToInt() + offset
                moveTo(x, yScale(p.high))
                lineTo(x, yScale(p.low))
            }
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