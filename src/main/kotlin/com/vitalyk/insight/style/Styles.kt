package com.vitalyk.insight.style

import javafx.scene.paint.Color
import tornadofx.*

class Styles : Stylesheet() {

    companion object {
        val wrapper by cssclass()
    }

    init {
        // use addClass(styles.wrapper) in target code
        s(wrapper) {
            //            s(chartHorizontalGridLines) {
//                visibility = FXVisibility.HIDDEN
//            }
        }

        listCell {
            and(selected) {
                label {
                    textFill = Color.WHITE
                }
            }
        }
        splitPane {
            backgroundInsets += box(0.px)
            padding = box(0.px)
        }

        // -fx-accent: #0096C9;
        // -fx-selection-bar: -fx-accent;
        // -fx-selection-bar-non-focused: lightgrey;

        // https://gist.github.com/maxd/63691840fc372f22f470
        // https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html#cell

        tableView {
            tableRowCell {
                and(filled) {
                    and(selected) {
                        backgroundColor += Color(0.99, 0.76, 0.18, 1.00)
                        tableCell {
                            borderColor += box(Color.TRANSPARENT)
//                            borderColor += box(Color.WHITE)
//                            backgroundInsets += box(0.px, 1.px, 0.px, 0.px)
//                            textFill = Color.BLACK
                        }
                    }
                }
            }
        }
    }

}