package view

import style.Styles
import tornadofx.App
import tornadofx.importStylesheet

class InsightApp : App(SymbolTableView::class) {

    init {
        importStylesheet(Styles::class)
    }

}

// Application.launch(InsightApp::class.java, *args)