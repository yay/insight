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
import java.util.concurrent.atomic.AtomicInteger

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

            onMouseDragged = EventHandler { e ->
                ctx.clearRect(0.0, 0.0, ctx.canvas.width, ctx.canvas.height)

                ctx.stroke = Color.RED
                ctx.setLineDashes(4.0, 2.0)

                ctx.beginPath()
                ctx.moveTo(e.x, 0.0)
                ctx.lineTo(e.x, ctx.canvas.height)

                ctx.moveTo(0.0, e.y)
                ctx.lineTo(ctx.canvas.width, e.y)

                ctx.stroke()
            }

            val iex = Iex(HttpClients.main)
            iex.getDayChart("NFLX", Iex.Range.M3)?.let {
                renderChart(ctx, it)

                primaryStage.widthProperty().addListener { observable, oldValue, newValue ->
                    this.width = newValue.toDouble()
                    renderChart(ctx, it)
                }
                primaryStage.heightProperty().addListener { observable, oldValue, newValue ->
                    this.height = newValue.toDouble()
                    renderChart(ctx, it)
                }
            }
        }

        val scene = Scene(Pane(canvas))
        primaryStage.scene = scene
        primaryStage.title = "Basic JavaFx app"
        primaryStage.show()
    }
}

fun renderChart(ctx: GraphicsContext, points: List<Iex.DayChartPoint>) {
    val minX = 0
    val maxX = points.size
    val minY = points.minBy { it.low }?.low ?: 0.0
    val maxY = points.maxBy { it.high }?.high ?: 1.0

    ctx.clearRect(0.0, 0.0, ctx.canvas.width, ctx.canvas.height)

//    ctx.fill = Color.WHITE
//    ctx.fillRect(0.0, 0.0, ctx.canvas.width, ctx.canvas.height)
//    ctx.clearRect(0.0, 0.0, ctx.canvas.width, ctx.canvas.height)

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

    ctx.stroke = Color.BLACK
    points.forEachIndexed { i, p ->
        val x = xScale(i)
        ctx.moveTo(x, yScale(p.high))
        ctx.lineTo(x, yScale(p.low))
    }
    ctx.stroke()

    points.forEachIndexed { i, p ->
        val x = xScale(i)
        val fill = if (p.close > p.open) Color.GREEN else Color.RED
        ctx.fill = fill
        val rectX = x - halfBarWidth
        val rectY1 = yScale(p.close)
        val rectY2 = yScale(p.open)
        ctx.fillRect(rectX, Math.min(rectY1, rectY2), barWidth, Math.abs(rectY2 - rectY1))
    }
}

fun main(args: Array<String>) {
    Application.launch(BasicApp::class.java, *args)
}