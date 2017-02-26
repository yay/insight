package sandbox

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import main.YahooFetcher
import org.quartz.Job
import org.quartz.JobExecutionContext

class HelloJob : Job {
    override fun execute(context: JobExecutionContext?) {
        println("I'M DOING MY JOB HERE! JOB JOB JOB... DONE!")
    }
}

fun schedule() {
    //    val jobDetail = JobBuilder.newJob(sandbox.HelloJob::class.java)
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



// ---------------------------------------------------

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