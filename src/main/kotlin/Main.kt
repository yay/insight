import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javafx.application.Application
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.BatchPoints
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import org.quartz.Job
import org.quartz.JobExecutionContext
import tornadofx.App
import tornadofx.importStylesheet
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class InsightApp : App(SymbolTableView::class) {

    init {
        importStylesheet(Styles::class)
    }

}

fun main(args: Array<String>) {
//    Settings.load()
//    Settings.saveOnShutdown()
//    Application.launch(InsightApp::class.java, *args)

//    YahooCompanyNews("AVGO").fetch().print()
//    YahooCompanyNews("NVDA").fetch().print()
//    YahooCompanyNews("MSFT").fetch().print()

//    YahooSummary("NVDA").execute().parse().print()
//    val list = YahooData("NVDA").execute().parse().ohlc()
//    saveToDb("NVDA", list)

//    val mapper = jacksonObjectMapper()
//    for (li in list) {
//        //println(mapper.writeValueAsString(li))
//        println(li)
//    }

//    connectToDb()
    loadFromDb("NVDA")
}

fun loadFromDb(symbol: String) { //: List<OHLC> {
    val influx = InfluxDBFactory.connect("http://localhost:8086", "root", "root")
    val dbName = "StockSeries"
    val query = Query("SELECT open, high, low, close FROM price", dbName)
    val result = influx.query(query)
    println(result)
}

fun saveToDb(symbol: String, data: List<OHLC>) {
    val influx = InfluxDBFactory.connect("http://localhost:8086", "root", "root")
    val dbName = "StockSeries"

    influx.createDatabase(dbName)

    val batchPoints = BatchPoints.database(dbName)
            .tag("symbol", symbol)
            .tag("period", "day")
            .retentionPolicy("autogen")
            .consistency(InfluxDB.ConsistencyLevel.ALL)
            .build()

    for (ohlc in data) {
        val pricePoint = Point.measurement("price")
//            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//            .time(ZonedDateTime.now().toInstant().toEpochMilli(), TimeUnit.MILLISECONDS)
                .time(ohlc.time, TimeUnit.MILLISECONDS)
                .addField("open", ohlc.open)
                .addField("high", ohlc.high)
                .addField("low", ohlc.low)
                .addField("close", ohlc.close)
                .addField("adjClose", ohlc.adjClose)
                .build()

        val volumePoint = Point.measurement("volume")
                .time(ohlc.time, TimeUnit.MILLISECONDS)
                .addField("volume", ohlc.volume)
                .build()

        batchPoints.point(pricePoint)
        batchPoints.point(volumePoint)
    }

    influx.write(batchPoints)

//    influx.deleteDatabase(dbName)
}

fun connectToDb() {
    val influx = InfluxDBFactory.connect("http://localhost:8086", "root", "root")
    val dbName = "StockSeries"

    influx.createDatabase(dbName)

    val batchPoints = BatchPoints.database(dbName)
//            .tag("async", "true")
            .retentionPolicy("autogen")
            .consistency(InfluxDB.ConsistencyLevel.ALL)
            .build()

    val point1 = Point.measurement("cpu")
//            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .time(ZonedDateTime.now().toInstant().toEpochMilli(), TimeUnit.MILLISECONDS)
            .addField("idle", 90L)
            .addField("user", 9L)
            .addField("system", 1L)
            .build()

    val point2 = Point.measurement("disk")
            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .addField("used", 80L)
            .addField("free", 1L)
            .build()

    batchPoints.point(point1)
    batchPoints.point(point2)
    influx.write(batchPoints)
    val query = Query("SELECT idle FROM cpu", dbName)
    influx.query(query)

    influx.deleteDatabase(dbName)
}






fun run() {
    // http://stackoverflow.com/questions/20387881/how-to-run-certain-task-every-day-at-a-particular-time-using-scheduledexecutorse
    val localNow = LocalDateTime.now()
    val currentZone = ZoneId.of("America/Los_Angeles")
    val zonedNow = ZonedDateTime.of(localNow, currentZone)
    var zonedNext5: ZonedDateTime
    zonedNext5 = zonedNow.withHour(5).withMinute(0).withSecond(0)
    if (zonedNow > zonedNext5)
        zonedNext5 = zonedNext5.plusDays(1)

    val duration = Duration.between(zonedNow, zonedNext5)
    val initialDelay = duration.getSeconds()

    val scheduler = Executors.newScheduledThreadPool(1)
    scheduler.scheduleAtFixedRate(object: TimerTask() {
        override fun run() {

        }
    }, initialDelay, (24 * 60 * 60).toLong(), TimeUnit.SECONDS)
}

class HelloJob : Job {
    override fun execute(context: JobExecutionContext?) {
        println("I'M DOING MY JOB HERE! JOB JOB JOB... DONE!")
    }
}

fun schedule() {
    //    val jobDetail = JobBuilder.newJob(HelloJob::class.java)
//            .withIdentity("dummyJobName", "group1")
//            .build()
//
//    val trigger = TriggerBuilder.newTrigger()
//            .withIdentity("dummyTriggerName", "group1")
//            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
//                    .withIntervalInSeconds(5)
//                    .repeatForever())
//            .build()
//
//    val scheduler = StdSchedulerFactory().getScheduler()
//    scheduler.start()
//    scheduler.scheduleJob(jobDetail, trigger)

    //    val cronTrigger = TriggerBuilder.newTrigger()
//            .withIdentity("dummyCronTrigger", "group1")
//            .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
//            .build()
}

fun _main(args: Array<String>) = runBlocking {
    val symbols = listOf("AVGO", "MSFT", "C", "SCHW", "BAC")

    val dataJob = async(CommonPool) {
        YahooFetcher.fetchData(symbols)
    }
    val summaryJob = async(CommonPool) {
        YahooFetcher.fetchSummary(symbols)
    }

    dataJob.await()
    summaryJob.await()
}

//class GreeterImpl : GreeterGrpc.GreeterImplBase {
//
//}
//
//class HelloServer {
//    private val log by lazy { Logger.getLogger(this::class.java.name) }
//
//    private val port: Int = 50051
//    private var server: Server? = null
//
//    fun start() {
//        server = ServerBuilder.forPort(port)
//                .addService { GreeterImpl() }
//                .build()
//                .start()
//
//        log.info { "Server started, listening on $port." }
//
//        Runtime.getRuntime().addShutdownHook(Thread {
//            System.err.println("Shutting down server.")
//            this.stop()
//        })
//    }
//
//    fun stop() {
//        server?.shutdown()
//    }
//
//    fun blockUntilShutdown() {
//        server?.awaitTermination()
//    }
//}

fun Mmain(args: Array<String>) = runBlocking<Unit> {
//    val tree = insight.YahooSummary("MSFT").execute().parse().tree()
//    println(tree["quoteSummary"]["result"][0]["defaultKeyStatistics"]["enterpriseValue"]["fmt"])
    Application.launch(InsightApp::class.java, *args)




//    val job = launch(CommonPool) { doWorld() }
//    println("Hello")
//    job.join()

//    val jobs = List(100_000) { // create a lot of coroutines and list their jobs
//        launch(CommonPool) {
//            delay(1000L)
//            print(".")
//        }
//    }
//    jobs.forEach { it.join() } // wait for all jobs to complete


//    val job = launch(CommonPool) {
//        repeat(1000) { i ->
//            println("I'm sleeping $i ...")
//            delay(500L)
//        }
//    }
//    delay(5000L) // just quit after delay
//    job.cancel()
//    delay(1500)

//    val job = launch(CommonPool) {
//        try {
//            repeat(1000) { i ->
//                println("I'm sleeping $i ...")
//                delay(500L)
//            }
//        } finally {
//            println("I'm running finally")
//        }
//    }
//    delay(1300L) // delay a bit
//    println("main: I'm tired of waiting!")
//    job.cancel() // cancels the job
//    delay(1300L) // delay a bit to ensure it was cancelled indeed
//    println("main: Now I can quit.")

//    withTimeout(1300L) {
//        repeat(1000) { i ->
//            println("I'm sleeping $i ...")
//            delay(500L)
//        }
//    }

//    thread {
//        Thread.sleep(1000L)
//        println("World")
//    }
//    println("Hello")
//    Thread.sleep(2000L)

//    Flowable.just("Hello world").subscribe(System.out::println);
//
//    Flowable.fromCallable {
//        Thread.sleep(1000) //  imitate expensive computation
//        "Done"
//    }
//    .subscribeOn(Schedulers.io())
//    .observeOn(Schedulers.single())
//    .subscribe(Consumer { println(it) }, Consumer { })
//
//    Thread.sleep(2000)
}