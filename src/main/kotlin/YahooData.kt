import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.StringReader
import java.text.SimpleDateFormat
import java.time.LocalDate

enum class DataFrequency {
    DAY, WEEK, MONTH
}

class YahooData(var symbol: String, var frequency: DataFrequency = DataFrequency.DAY) {

    /*

    TODO: unit tests

    Example base URL params:

    ?s=AVGO &a=1 &b=10 &c=2016 &d=1 &e=10 &f=2017 &g=d &ignore=.csv
    ?s=AVGO &a=1 &b=10 &c=2016 &d=1 &e=10 &f=2017 &g=w &ignore=.csv
    ?s=AVGO &a=1 &b=10 &c=2016 &d=1 &e=10 &f=2017 &g=m &ignore=.csv

    */

    private val baseUrl: String = "http://chart.finance.yahoo.com/table.csv"
    private val urlBuilder = HttpUrl.parse(baseUrl).newBuilder()
    private val client = OkHttpClient()
    private lateinit var request: Request // TODO: timeout
    private lateinit var response: Response // TODO: async

    private var data: String = ""
    private lateinit var records: Iterable<CSVRecord>

    // In Kotlin, unlike Java or C#, classes do not have static methods.
    // In most cases, it's recommended to simply use package-level functions instead.
    // Or a companion object.
    companion object {
        val header: Array<String> = arrayOf("Date", "Open", "High", "Low", "Close", "Volume", "Adj Close")
    }

    private val symbolParam = "s"

    private val format = ".csv"
    private val formatParam = "ignore"

    private val startYearParam = "c"
    private val endYearParam = "f"

    private val startMonthParam = "a"
    private val endMonthParam = "d"

    private val startDateParam = "b"
    private val endDateParam = "e"

    var endDate: LocalDate = LocalDate.now()
    var startDate: LocalDate = endDate.minusYears(1)

    private val frequencyParam = "g"
    private val frequencyMap = mapOf<DataFrequency, String>(
            DataFrequency.DAY to "d",
            DataFrequency.WEEK to "w",
            DataFrequency.MONTH to "m"
    )

    init {
        endDate.year
    }

    fun startDate(date: LocalDate): YahooData {
        urlBuilder
                .addQueryParameter(startYearParam, date.year.toString())
                .addQueryParameter(startMonthParam, date.monthValue.toString())
                .addQueryParameter(startDateParam, date.dayOfMonth.toString())

        return this
    }

    fun endDate(date: LocalDate): YahooData {
        urlBuilder
                .addQueryParameter(endYearParam, date.year.toString())
                .addQueryParameter(endMonthParam, date.monthValue.toString())
                .addQueryParameter(endDateParam, date.dayOfMonth.toString())

        return this
    }

    fun execute(): YahooData {
        urlBuilder.addQueryParameter(symbolParam, symbol)

        startDate(startDate)
        endDate(endDate)

        urlBuilder
                .addQueryParameter(frequencyParam, frequencyMap[frequency])
                .addQueryParameter(formatParam, format)

        val url = urlBuilder.build().toString()

        println("Sending historical data request for $symbol:")
        println(url)

        request = Request.Builder().url(url).build()
        response = client.newCall(request).execute()

        if (response.code() == 200) {
            data = response.body().string()
        }

        return this
    }

    fun parse(): YahooData {
//        CSVFormat.DEFAULT.withHeader(*header).parse(StringReader(data))
        records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(StringReader(data))

        return this
    }

    fun data(): String { return data }
    fun records(): Iterable<CSVRecord> { return records }

    fun list(): List<StockSymbol> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        return records.map { it -> StockSymbol(
                dateFormat.parse(it.get(header[0])),
                it.get(header[1]).toFloat(),
                it.get(header[2]).toFloat(),
                it.get(header[3]).toFloat(),
                it.get(header[4]).toFloat(),
                it.get(header[5]).toInt(),
                it.get(header[6]).toFloat()
        ) }
    }

}
