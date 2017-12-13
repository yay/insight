package com.vitalyk.insight.style

import tornadofx.Stylesheet
import tornadofx.cssclass

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
    }

}