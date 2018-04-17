package com.vitalyk.insight.chart

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*

data class Data<out X, out Y>(
    val x: X,
    val y: Y
)

class CartesianChart : GridPane() {
    fun Node.stretch(): Node {
//        hgrow = Priority.ALWAYS
//        vgrow = Priority.ALWAYS
//        maxWidth = Double.MAX_VALUE
//        maxHeight = Double.MAX_VALUE
        return this
    }

//    var data = FXCollections.emptyObservableList<Data<X, Y>>()

    init {
        val b1 = Button("b1").apply {
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            style {
//                val ccc = c("f00")
//                val cc = Color.GREEN
                backgroundColor += c("#f00")
//                backgroundColor += Color.GREEN as Paint
            }
        }
//        val b2 = Button("b2").apply {
//            maxWidth = Double.MAX_VALUE
//            maxHeight = Double.MAX_VALUE
//            style {
//                backgroundColor += c("#0f0")
//            }
//        }
        val b2 = object : Canvas() {
            override fun resize(width: Double, height: Double) {
                super.resize(width, height)
                this.width = width
                this.height = height
            }

            init {
                val ctx = this.graphicsContext2D
                maxWidth = Double.MAX_VALUE
                maxHeight = Double.MAX_VALUE
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS

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
        val b3 = Button("b3").apply {
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            style {
                backgroundColor += c("#ff0")
            }
        }
        val b4 = Button("b4").apply {
            maxWidth = Double.MAX_VALUE
            opacity = 0.5
            style {
                backgroundColor += c("#00f")
            }
        }
//
        isGridLinesVisible = true

        children.apply {
            add(b1, 0, 0, 1, REMAINING)
            add(b3, 1, 0, 1, REMAINING)
            add(b2, 2, 0)
            add(b4, 0, 1, REMAINING, 1)
        }

//        setConstraints(b1, 0, 0)
//        setHgrow(b1, Priority.ALWAYS)
//        setVgrow(b1, Priority.ALWAYS)
//        setConstraints(b2, 1, 0)
        setHgrow(b2, Priority.ALWAYS)
        setVgrow(b2, Priority.ALWAYS)
//        setConstraints(b3, 2, 0)
//        setHgrow(b3, Priority.ALWAYS)

    }
}