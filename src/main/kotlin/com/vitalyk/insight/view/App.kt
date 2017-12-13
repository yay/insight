package com.vitalyk.insight.view

import com.vitalyk.insight.style.Styles
import tornadofx.*

class InsightApp : App(SymbolTableView::class) {

    init {
        importStylesheet(Styles::class)
    }

}

// Application.launch(InsightApp::class.java, *args)