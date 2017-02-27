package controller

import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
import main.DataFrequency
import main.MainHttpClient
import main.YahooData
import main.YahooSummary
import org.codehaus.jackson.map.ObjectMapper
import tornadofx.Controller
import tornadofx.observable
import view.SymbolTableView

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

        view.symbolData.value = request.data()
        view.symbolTable.items = request.list().observable()
    }

    fun fetchSummary(node: Node, symbol: String) {
        val request = YahooSummary(symbol, MainHttpClient.client).execute().parse()
        val tree = request.tree()
        val mapper = ObjectMapper()
        val pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree)

        view.symbolSummary.value = pretty
    }

    fun fetch(node: Node, symbol: String) {
        fetchData(node, symbol)
        fetchSummary(node, symbol)
    }

}