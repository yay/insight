package com.vitalyk.insight.misc

import javafx.scene.paint.Color
import com.vitalyk.insight.dv.LinearScale
import com.vitalyk.insight.dv.LogScale
import tornadofx.*

class TestView : View("Interpolation") {

    override val root = vbox()

    init {

        primaryStage.minWidth = 900.0
        primaryStage.minHeight = 600.0


        with (root) {

            val dataX = mutableListOf<Double>()
            val dataY = mutableListOf<Double>()
            for (x in 1..10) {
                val y = Math.pow(10.0, x.toDouble())
                dataX.add(x.toDouble())
                dataY.add(y)
            }

            val minX = dataX.min() ?: 0.0
            val maxX = dataX.max() ?: 1.0
            val minY = dataY.min() ?: 0.0
            val maxY = dataY.max() ?: 1.0

            val scaleX = LinearScale().apply {
                domain = minX to maxX
                range = 0.0 to primaryStage.minWidth
            }

            val scaleY = LogScale().apply {
                domain = minY to maxY
                range = 0.0 to primaryStage.minHeight
            }

            val count = dataX.count()

            rectangle {

            }

            canvas {
                width = primaryStage.minWidth
                height = primaryStage.minHeight
                val ctx = graphicsContext2D

                ctx.translate(0.0, height)
                ctx.scale(1.0, -1.0)

                ctx.stroke = Color.BLACK
                ctx.fill = Color.RED
                ctx.lineWidth = 3.0

                ctx.beginPath()
                for (i in 0 until count) {
                    val x0 = scaleX.toRange(dataX[i])
                    val y0 = scaleY.toRange(dataY[i])
                    val x1 = scaleX.toRange(dataX[i])
                    val y1 = scaleY.toRange(dataY[i])

                    if (i == 0) {
                        ctx.moveTo(x0, y0)
                    }
                    ctx.fillOval(x1 - 4.0, y1 - 4.0, 8.0, 8.0)
                    ctx.lineTo(x1, y1)
                }

                ctx.stroke()
            }
        }
    }

}

class TestApp : App(TestView::class)

//    val linearScale = LinearScale()
//
//    println(linearScale.toRange(0.0))
//    println(linearScale.toRange(1.0))
//
//    linearScale.range = 20.0 to 40.0
//    println(linearScale.toRange(0.5))
//    println(linearScale.toDomain(30.0))
//    println(linearScale.toDomain(25.0))
//    println(linearScale.toDomain(35.0))

//    val logScale1 = LogScale().apply {
//        base = 10.0
//        domain = 1.0 to 1000_000.0
//        range = 100.0 to 200.0
//    }
//
//    println(logScale1.toRange(1.0))
//    println(logScale1.toRange(1000.0))
//    println(logScale1.toRange(1000_000.0))
//
//    println("")
//
//    println(logScale1.toDomain(100.0))
//    println(logScale1.toDomain(200.0))
//    println(logScale1.toDomain(125.0))
//    println(logScale1.toDomain(150.0))
//    println(logScale1.toDomain(175.0))

//    val logScale2 = LogScale().apply {
//        base = 10.0
//        domain = -1000.0 to -1.0
//        range = 100.0 to 200.0
//    }
//
//    println(logScale2.toRange(-1000.0))
//    println(logScale2.toRange(-100.0))
//    println(logScale2.toRange(-10.0))
//    println(logScale2.toRange(-1.0))
//
//    println("")
//
//    println(logScale2.toDomain(100.0))
//    println(logScale2.toDomain(200.0))
//    println(logScale2.toDomain(125.0))
//    println(logScale2.toDomain(175.0))

//    println(logScale2.toDomain(100.0))
//    println(logScale2.toDomain(200.0))
//    println(logScale2.toDomain(125.0))
//    println(logScale2.toDomain(150.0))
//    println(logScale2.toDomain(175.0))