import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.SimpleDateFormat


class SymbolChart : View() {

    val symbolTable = find(SymbolTable::class)
    private lateinit var chart: LineChart<String, Number>

    override fun onDock() {
        chart.title = "${symbolTable.symbolProperty.value} stock price"
        var showGridLines = symbolTable.symbolView.items.count() < 100
        chart.isHorizontalGridLinesVisible = showGridLines
        chart.verticalGridLinesVisible = showGridLines
        chart.series(symbolTable.symbolProperty.value) {
            val dateFormat = SimpleDateFormat("d MMM, yyyy")
            for (item in symbolTable.symbolView.items.reversed()) {
                data(dateFormat.format(item.date), item.close)
            }
        }
    }

    override fun onUndock() {
        chart.data.clear()
    }

    override val root = vbox()

    init {
        with (root) {
            addClass(Styles.wrapper)

            hbox {
                paddingAll = 10.0

                button("Show Table") {
                    maxWidth = Double.MAX_VALUE
                    setOnAction {
                        replaceWith(
                                SymbolTable::class
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
        }
    }

}