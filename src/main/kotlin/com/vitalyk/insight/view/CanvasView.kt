package com.vitalyk.insight.view

import com.vitalyk.insight.chart.CartesianChart
import com.vitalyk.insight.ui.toolbox
import javafx.event.EventHandler
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import tornadofx.*

class MyElem : Region() {

    val seriesCanvas = Canvas().apply {
        width = 400.0
        height = 400.0

        children.add(Circle().apply {
            radius = 50.0
            centerX = 80.0
            centerY = 120.0
            fill = Color.YELLOW
        })
    }

    val overlayCanvas = Canvas().apply {
        val ctx = this.graphicsContext2D
        width = 400.0
        height = 400.0

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

    override fun resize(width: Double, height: Double) {
        super.resize(width, height)
        seriesCanvas.apply {
            this.width = width
            this.height = height
            graphicsContext2D.clearRect(0.0, 0.0, width, height)
        }
        overlayCanvas.apply {
            this.width = width
            this.height = height
            graphicsContext2D.clearRect(0.0, 0.0, width, height)
        }
    }

    init {
        children.add(StackPane().apply {
            children.apply {
                add(seriesCanvas)
                add(overlayCanvas)
            }
        })
    }
}

class CanvasView : View("Canvas") {
    override val root = vbox {
        toolbox {
            button("Back").action {
                replaceWith(SymbolTableView::class)
            }
        }
//        this += MyElem().apply {
//            vgrow = Priority.ALWAYS
//        }
        hbox {
            this += CartesianChart().apply {
                hgrow = Priority.ALWAYS
            }
            this += CartesianChart().apply {
                hgrow = Priority.ALWAYS
            }
            this += CartesianChart().apply {
                hgrow = Priority.ALWAYS
            }
            vgrow = Priority.ALWAYS
        }
        hbox {
            this += CartesianChart().apply {
                hgrow = Priority.ALWAYS
            }
            this += CartesianChart().apply {
                hgrow = Priority.ALWAYS
            }
            this += CartesianChart().apply {
                hgrow = Priority.ALWAYS
            }
            vgrow = Priority.ALWAYS
        }
//        stackpane {
//            canvas {
//                val ctx = this.graphicsContext2D
//                width = 400.0
//                height = 400.0
//
//                circle {
//                    radius = 50.0
//                    centerX = 80.0
//                    centerY = 120.0
//                    fill = Color.YELLOW
//                }
//            }
//            canvas {
//                val ctx = this.graphicsContext2D
//                width = 400.0
//                height = 400.0
//
//                onMouseDragged = EventHandler { e ->
//                    ctx.clearRect(0.0, 0.0, ctx.canvas.width, ctx.canvas.height)
//
//                    ctx.stroke = Color.RED
//                    ctx.setLineDashes(4.0, 2.0)
//
//                    ctx.beginPath()
//                    ctx.moveTo(e.x, 0.0)
//                    ctx.lineTo(e.x, ctx.canvas.height)
//
//                    ctx.moveTo(0.0, e.y)
//                    ctx.lineTo(ctx.canvas.width, e.y)
//
//                    ctx.stroke()
//                }
//            }
//        }
    }
}