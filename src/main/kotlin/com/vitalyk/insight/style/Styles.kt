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
        toolBar {
        }
        splitPane {
            backgroundInsets += box(0.px)
            padding = box(0.px)
        }
    }

}