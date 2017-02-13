// http://square.github.io/okhttp/
// http://jdbi.github.io/

import javafx.application.Application
import tornadofx.App
import tornadofx.Controller
import tornadofx.importStylesheet
import java.text.DecimalFormat


//class MainView: View() {
//    override val root = vbox {
//        button("Download Quotes") {
//            setOnAction {
//                println("Button pressed!")
//
//                // https://github.com/kittinunf/Fuel
//                "http://httpbin.org/get".httpGet().responseString { request, response, result ->
//                    //do something with response
//                    when (result) {
//                        is Result.Failure -> {
//                            error = result.getAs()
//                        }
//                        is Result.Success -> {
//                            data = result.getAs()
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

class SymbolTableController : Controller() {
    fun fetch(symbol: String) {
        println("Fetching $symbol...")
    }
}

class InsightApp : App(SymbolTable::class) {

    init {
        importStylesheet(Styles::class)
    }

}


fun main(args: Array<String>) {
//    val tree = YahooSummaryRequest("MSFT").execute().parse().tree()
//    println(tree["quoteSummary"]["result"][0]["defaultKeyStatistics"]["enterpriseValue"]["fmt"])
    Application.launch(InsightApp::class.java, *args)
}