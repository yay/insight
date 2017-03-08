package view

import javafx.scene.chart.BarChart
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat
import style.Styles as styles


class ChartView : View("Security Chart") {

    val symbolTableView = find(SymbolTableView::class)
    private lateinit var chart: LineChart<String, Number>
    private lateinit var volumeChart: BarChart<String, Number>

    override fun onDock() {
//        chart.title = symbolTableView.symbol.value
//        val showGridLines = symbolTableView.symbolTable.items.count() < 100
//        chart.isHorizontalGridLinesVisible = showGridLines
//        chart.verticalGridLinesVisible = showGridLines
//        chart.series(symbolTableView.symbol.value) {
//            val dateFormat = SimpleDateFormat("d MMM, yyyy")
//            for (item in symbolTableView.symbolTable.items.reversed()) {
//                data(dateFormat.format(item.date), item.adjClose)
//            }
//        }

//        volumeChart.series("Volume") {
//            val dateFormat = SimpleDateFormat("d MMM, yyyy")
//            for (item in symbolTableView.symbolTable.items.reversed()) {
//                data(dateFormat.format(item.date), item.volume)
//            }
//        }
    }

    override fun onUndock() {
        chart.data.clear()
//        volumeChart.data.clear()
    }

    override val root = vbox()

    init {
        with (root) {
            addClass(styles.wrapper)

            hbox {
                paddingAll = 10.0

                button("Back") {
                    maxWidth = Double.MAX_VALUE
                    setOnAction {
                        replaceWith(
                                SymbolTableView::class
//                                ViewTransition.Slide(
//                                        0.3.seconds,
//                                        ViewTransition.Direction.RIGHT
//                                )
                        )
                    }
                }
            }

            chart = linechart(null, CategoryAxis(), NumberAxis()) {
                animated = false
                createSymbols = false
                isLegendVisible = false
                vgrow = Priority.ALWAYS
            }

//            volumeChart = barchart("Volume", CategoryAxis(), NumberAxis()) {
//                animated = false
//                maxHeight = 200.0
//            }

        }
    }

}