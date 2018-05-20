package com.vitalyk.insight.view

import tornadofx.*

class NewsView : Fragment("Headlines") {

    override val root = vbox {
        toolbar {
            button("Main").action { replaceWith(MainView::class) }
        }
        this += NewsFragment()
    }
}