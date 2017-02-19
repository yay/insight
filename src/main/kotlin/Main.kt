import javafx.application.Application
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import tornadofx.App
import tornadofx.importStylesheet


class InsightApp : App(SymbolTableView::class) {

    init {
        importStylesheet(Styles::class)
    }

}

fun main(args: Array<String>) {
    Application.launch(InsightApp::class.java, *args)
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