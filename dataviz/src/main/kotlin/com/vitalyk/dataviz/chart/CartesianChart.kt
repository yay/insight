package com.vitalyk.dataviz.chart

import javafx.event.EventHandler
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import java.time.LocalDateTime

open class Surface : Canvas() {
    override fun resize(width: Double, height: Double) {
        super.resize(width, height)
        this.width = width
        this.height = height
    }
}

data class OHLC(
    val time: LocalDateTime,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

class CrosshairSurface : Surface() {
    init {
        val ctx = graphicsContext2D
//        maxWidth(Double.MAX_VALUE)
//        maxHeight(Double.MAX_VALUE)
//        hgrow = Priority.ALWAYS
//        vgrow = Priority.ALWAYS

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
    }
}

class CartesianChart : Pane() {

}