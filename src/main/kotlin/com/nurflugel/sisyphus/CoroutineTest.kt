package com.nurflugel.sisyphus

//import kotlin.coroutines.GlobalScope
//import kotlin.coroutines.
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.time.Duration
import java.time.Instant

class CoroutineTest {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val numIterations = listOf(1L, 10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 100_000_000L)
//            numIterations.forEach(CoroutineTest()::doItWithThreads)
            numIterations.forEach(CoroutineTest()::doIt)
        }
    }

    //    val numberIterations = 1L
    //    val numberIterations = 10L                // 6 s
    //    val numberIterations = 100L               // 7.122S s
    //        val numberIterations = 1_000L             // 7.19 s
    //        val numberIterations = 10_000L            // 7.32 s     7.14
    //        val numberIterations = 100_000L           // 8.15 s     7.27
    //    val numberIterations = 1_000_000L         // 16.9 s     10.52

    fun doIt(numberIterations: Long) {
        val start = Instant.now()
        val formattedNumber = "%,d".format(numberIterations)
        println("\nStarting test with $formattedNumber coroutines")
        val sum = runBlocking {
            //            println("in sum2")
            val sum: Int = (0L until numberIterations)
                .map { async { doRandomSleep() } }
                .sumOf { it.await() }

            //            println("In launch scope")
            // these run in the order they're declared
            //            val jobA = GlobalScope.async { funC(3) }
            //            val jobB = GlobalScope.async { funC(5) }
            //            val jobC = GlobalScope.async { funC(2) }
            //            sum = jobB.await() + jobA.await() + jobC.await()
            //            println("done with launch scope")

            // which we will sum

            sum
        }
        val df = DecimalFormat("#.##")
//        df.roundingMode = RoundingMode.DOWN

        val between: Double = Duration.between(start, Instant.now()).toMillis()/1000.0
        val formattedSum = "%,d".format(sum)
        val formattedDuration = df.format(between)
        println("sum is $formattedSum, total duration for $formattedNumber coroutines was $formattedDuration")

    }

    /** Delay function which sleeps and returns the delay in sec */
    private suspend fun doRandomSleep(): Int {
        val delay = Math.random() * 5 + 2
        val timeMillis = (delay * 1000L).toLong()
        delay(timeMillis)
        return delay.toInt()
    }
// todo need to create the threaded version of above
//    fun doItWithThreads(numberIterations: Long) {
//        val start = Instant.now()
//
//        println("Starting test with $numberIterations iterations")
//        val sum = runBlocking {
//            //            println("in sum2T")
//            var sum = 0
//
//            //            println("In launch scope")
//            // these run in the order they're declared
//            //            val jobA = GlobalScope.async { funC(3) }
//            //            val jobB = GlobalScope.async { funC(5) }
//            //            val jobC = GlobalScope.async { funC(2) }
//            //            sum = jobB.await() + jobA.await() + jobC.await()
//            //            println("done with launch scope")
//
//            sum = (0 until numberIterations)
//                .map { async { funC() } }
//                .sumOf { it.await() }
//
//            sum
//        }
//        println("sum is $sum, total duration for $numberIterations threads was " + Duration.between(start, Instant.now()))
//
//    }


}