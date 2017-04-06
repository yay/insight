package main

// https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md
// https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md
// https://github.com/Kotlin/kotlinx.coroutines

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import style.Styles
import tornadofx.App
import tornadofx.importStylesheet
import view.SymbolTableView
import java.io.File
import java.math.BigDecimal
import java.sql.Timestamp
import java.text.SimpleDateFormat

import org.quartz.JobBuilder.*
import org.quartz.SimpleScheduleBuilder.*
import org.quartz.DateBuilder.*
import org.quartz.CronScheduleBuilder.*
import org.quartz.CalendarIntervalScheduleBuilder.*
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.TriggerBuilder.*
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import java.time.LocalDate


class InsightApp : App(SymbolTableView::class) {

    init {
        importStylesheet(Styles::class)
    }

}

fun csvDailyQuotesToDb(db: DBI) {

    // TODO: https://www.google.nl/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#newwindow=1&q=ERROR:+numeric+field+overflow&*

    val exchangeToMarketMap = mapOf(
            "nasdaq" to "XNAS",
            "nyse" to "XNYS",
            "amex" to "XASE"
    )
    val basePath = "${AppSettings.paths.storage}/${AppSettings.paths.dailyData}/24-02-2017/"

    fun BigDecimal.isValidPrice(): Boolean = this.precision() <= 8 && this.scale() <= 6
    fun String.isValidSymbol(): Boolean = this.length <= 6
    fun String.isValidMarket(): Boolean = this.length <= 4

    fun writeRecords(handle: Handle, records: CSVParser,
                     market: String, symbol: String, dateFormat: SimpleDateFormat): Boolean {
        records.forEach {
            val rec = it
            val date = DateTime(dateFormat.parse(rec.get(YahooDataColumns.date)))
            val open = BigDecimal(rec.get(YahooDataColumns.open))
            val high = BigDecimal(rec.get(YahooDataColumns.high))
            val low = BigDecimal(rec.get(YahooDataColumns.low))
            val close = BigDecimal(rec.get(YahooDataColumns.close))
            val adjClose = BigDecimal(rec.get(YahooDataColumns.adjClose))
            val volume = rec.get(YahooDataColumns.volume).toLong()

            if (adjClose.isValidPrice() && // adjClose check is most likely to fail
                open.isValidPrice() &&
                high.isValidPrice() &&
                low.isValidPrice() &&
                close.isValidPrice() &&
                symbol.isValidSymbol() &&
                market.isValidMarket()) {

                try {
                    handle.createStatement("insert into dailyquotes (quote_date, symbol, market, \"open\", high, low, \"close\", adj_close, volume) values (:quote_date, :symbol, :market, :open, :high, :low, :close, :adj_close, :volume)")
                            .bind("quote_date", Timestamp(date.millis))
                            .bind("symbol", symbol)
                            .bind("market", market)
                            .bind("open", open)
                            .bind("high", high)
                            .bind("low", low)
                            .bind("close", close)
                            .bind("adj_close", adjClose)
                            .bind("volume", volume)
                            .execute()
                } catch (e: Exception) {
                    val logger = getAppLogger()
                    logger.error("Exception on ($date, $symbol, $market): $e\n" +
                            "$symbol data will not be added to the database.")
                    handle.execute("rollback")
                    return false
                }

            }

        }

        return true
    }

    db.useHandle {

        // IMPORTANT:
        // https://www.postgresql.org/docs/9.1/static/populate.html

        val handle = it
        var index = 0

        for ((exchange, market) in exchangeToMarketMap) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val path = basePath + exchange
            val walker = File(path).walk().maxDepth(1)

            for (file in walker) {
                val symbol = file.nameWithoutExtension.trim()
                if (symbol != exchange) {
                    getAppLogger().debug("${index++} - $market:$symbol")
                    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(file.reader())

                    handle.execute("begin")

                    if (writeRecords(handle, records, market, symbol, dateFormat)) {
                        handle.execute("commit")
                    } else {
                        handle.execute("rollback")
                    }
                }
            }
        }

    }
}

fun runDbMigration(db: DBI) {
    val runner = MigrationRunner(db)
}

fun createTableIndex(db: DBI) {
    db.useHandle {
        it.execute("begin")
        it.execute("alter table dailyquotes add primary key (quote_date, market, symbol)")
        it.execute("commit")
    }
}

class IntradayFetcher : Job {
    override fun execute(context: JobExecutionContext?) {
        println("Hi there!")
    }
}

fun function() {
    val schedulerFactory = StdSchedulerFactory()
    val scheduler = schedulerFactory.getScheduler()
    scheduler.start()

    val job = newJob(IntradayFetcher::class.java)
            .withIdentity("myJob", "group1")
            .build()

    val trigger = newTrigger()
            .withIdentity("myTrigger", "group1")
            .startNow()
            .withSchedule(simpleSchedule()
                    .withIntervalInSeconds(10)
                    .repeatForever())
            .build()

    scheduler.scheduleJob(job, trigger)

    scheduler.shutdown()
}

fun main(args: Array<String>) {

//    function()
//    val db = DBI("jdbc:postgresql://localhost:5432/insight")

//    async(CommonPool) {
//        var map = mutableMapOf<String, MutableMap<String, String>>()
//        StockFetcherUS.forAll { exchange, companies ->
//            val symbolNames = mutableMapOf<String, String>()
//            companies.forEach { symbolNames[it.symbol] = it.name }
//            map[exchange] = symbolNames
//        }
//        Settings.save(map, "exchanges.json")
//    }

//    Settings.load(AppSettings)
//    Settings.saveOnShutdown(AppSettings)
//    Application.launch(TestApp::class.java, *args)

    fetchIntradayDataUsa()
    fetchSummaryUsa()

    // http://mailman.qos.ch/pipermail/logback-user/2007-June/000247.html
//    MarkerFactory.getMarker("flush_mail")

//    val ySteps = Nd4j.linspace(-100_000, 100_100, 101)

//    println(ySteps)

//    val xData = doubleArrayOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0)
//    val yData = doubleArrayOf(0.0, 10.0, 100.0, 1000.0, 10_000.0, 100_000.0)
//
//    // Create Chart
//    val chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData)
//
//    // Show it
//    SwingWrapper(chart).displayChart()

}