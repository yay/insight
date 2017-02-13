import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*

class SymbolCsv : View() {

    val symbolTable = find(SymbolTable::class)

    override fun onDock() {
    }

    override fun onUndock() {
    }

    override val root = vbox()

    init {
        with (root) {

            hbox {
                spacing = 10.0
                padding = Insets(10.0)
                alignment = Pos.CENTER_LEFT

                button("Show Table") {
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

            textarea(symbolTable.symbolData()) {
                vgrow = Priority.ALWAYS
            }

//            piechart("Imported Fruits") {
//                data("Grapefruit", 12.0)
//                data("Oranges", 25.0)
//                data("Plums", 10.0)
//                data("Pears", 22.0)
//                data("Apples", 30.0)
//
//                vgrow = Priority.ALWAYS
//            }
        }
    }

}