package com.vitalyk.insight.controller

import com.vitalyk.insight.iex.IexApi
import com.vitalyk.insight.iex.toDayChartPointBean
import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
import com.vitalyk.insight.yahoo.DataFrequency
import com.vitalyk.insight.yahoo.YahooData
import com.vitalyk.insight.view.SymbolTableView
import tornadofx.*

class SymbolTableController : Controller() {

    val view: SymbolTableView by inject()

    fun fetchData(node: Node, symbol: String) {

        val startDate = node.lookup("#startDate") as DatePicker
        val endDate = node.lookup("#endDate") as DatePicker
        val period = node.lookup("#period") as ComboBox<String>

        val request = YahooData(symbol, DataFrequency.valueOf(period.value.toUpperCase()))
            .startDate(startDate.value)
            .endDate(endDate.value)
            .execute()
            .parse()

        val points = IexApi.getDayChart("AAPL").map { point ->
            point.toDayChartPointBean()
        }

//        view.symbolData.value = request.data()
        view.symbolTable.items = points.observable()
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