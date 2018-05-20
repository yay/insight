package com.vitalyk.insight.controller

import com.vitalyk.insight.iex.Iex
import com.vitalyk.insight.iex.toBean
import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
import com.vitalyk.insight.yahoo.DataFrequency
import com.vitalyk.insight.yahoo.YahooData
import tornadofx.*

class SymbolTableController : Controller() {

//    val view: SymbolTableView by inject()

    fun fetchData(node: Node, symbol: String) {

        val startDate = node.lookup("#startDate") as DatePicker
        val endDate = node.lookup("#endDate") as DatePicker
        val period = node.lookup("#period") as ComboBox<String>

        val request = YahooData(symbol, DataFrequency.valueOf(period.value.toUpperCase()))
            .startDate(startDate.value)
            .endDate(endDate.value)
            .execute()
            .parse()

        Iex.getDayChart("AAPL")?.let {
            val points = it.map { point ->
                point.toBean()
            }
//            view.symbolTable.items = points.observable()
        }
    }

    fun fetchSummary(node: Node, symbol: String) {
//        val request = YahooSummary(symbol, HttpClients.yahoo).execute().parse()
//        val tree = request.tree()
//        val mapper = ObjectMapper()
//        val pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree)
//
//        view.symbolSummary.value = pretty
    }

    fun fetch(node: Node, symbol: String) {
        fetchData(node, symbol)
        fetchSummary(node, symbol)
    }

}