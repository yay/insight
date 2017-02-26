package sandbox

import javafx.application.Application
import kotlinx.coroutines.experimental.runBlocking
import main.InsightApp

fun Mmain(args: Array<String>) = runBlocking<Unit> {
    //    val tree = insight.main.YahooSummary("MSFT").execute().parse().tree()
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
//    println("main.main: I'm tired of waiting!")
//    job.cancel() // cancels the job
//    delay(1300L) // delay a bit to ensure it was cancelled indeed
//    println("main.main: Now I can quit.")

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