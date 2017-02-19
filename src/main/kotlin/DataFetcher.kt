import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.codehaus.jackson.map.ObjectMapper
import java.io.File
import java.time.LocalDate

object YahooFetcher {
    val stockDataDir = "stock_data"
    val stockSummaryDir = "stock_summary"

    fun fetchData(symbols: List<String>) {

        val now = LocalDate.now()
        val then = now.minusYears(1)

//        async(CommonPool) {
//        }.await()
        for (symbol in symbols) {
            val request = YahooData(symbol, DataFrequency.DAY)
                    .startDate(then)
                    .endDate(now)
                    .execute()
//                .parse()

            request.data()

            // http://stackoverflow.com/questions/35444264/how-do-i-write-to-a-file-in-kotlin/35445704
            val file = File("$stockDataDir/$symbol.csv")
            file.parentFile.mkdirs()
            file.writeText(request.data())
        }
    }

    fun fetchSummary(symbols: List<String>) {
        for (symbol in symbols) {
//            async(CommonPool) {
//
//            }.await()
            val request = YahooSummary(symbol)
                    .execute()
                    .parse()

            val tree = request.tree()
            val mapper = ObjectMapper()
            val prettyData = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(tree)


            val file = File("$stockSummaryDir/$symbol.json")
            file.parentFile.mkdirs();
            file.writeText(prettyData)
        }
    }

    fun asyncFetchData(symbols: List<String>) = async(CommonPool) {
        fetchData(symbols)
    }

    fun asyncFetchSummary(symbols: List<String>) = async(CommonPool) {
        fetchSummary(symbols)
    }
}