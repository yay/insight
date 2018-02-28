package com.vitalyk.insight.style

import javafx.scene.paint.Color
import tornadofx.*

class Styles : Stylesheet() {

    companion object {
        val wrapper by cssclass()
    }

    init {
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
    }

}