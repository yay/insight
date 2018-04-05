package com.vitalyk.insight.view

import com.vitalyk.insight.ui.toolbox
import javafx.event.EventHandler
import javafx.scene.paint.Color
import tornadofx.*

class CanvasView : View("Canvas") {
    override val root = vbox {
        toolbox {
            button("Back").action {
                replaceWith(SymbolTableView::class)
            }
        }
        stackpane {
            canvas {
                val ctx = this.graphicsContext2D
                width = 400.0
                height = 400.0

                circle {
                    radius = 50.0
                    centerX = 80.0
                    centerY = 120.0
                    fill = Color.YELLOW
                }
            }
            canvas {
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
        }
    }
}