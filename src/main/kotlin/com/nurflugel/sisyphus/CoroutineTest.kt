package com.nurflugel.sisyphus

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.Instant

class CoroutineTest {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val numIterations = listOf(1L, 10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L)
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

        println("\nStarting test with $numberIterations iterations")
        val sum = runBlocking {
            //            println("in sum2")
            val sum: Int = (0L until numberIterations)
                .map { GlobalScope.async { funC(it) } }
                .map { it.await() } // this returns a value which we will sum
                .sum()

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
        println("sum is $sum, total duration for $numberIterations coroutines was " + Duration.between(start, Instant.now()))

    }

    /** Delay function which sleeps and returns the delay in sec */
    private suspend fun funC(index: Long): Int {
        val start = Instant.now()
        //        println("fun $index start $start")
        val delay = Math.random() * 5 + 2
        val timeMillis = (delay * 1000L).toLong()
        delay(timeMillis)
        //        println("fun $index end, duration was " + Duration.between(start, Instant.now()))
        return delay.toInt()
    }

    fun doItWithThreads(numberIterations: Long) {
        val start = Instant.now()

        println("Starting test with $numberIterations iterations")
        val sum = runBlocking {
            //            println("in sum2T")
            var sum = 0

            //            println("In launch scope")
            // these run in the order they're declared
            //            val jobA = GlobalScope.async { funC(3) }
            //            val jobB = GlobalScope.async { funC(5) }
            //            val jobC = GlobalScope.async { funC(2) }
            //            sum = jobB.await() + jobA.await() + jobC.await()
            //            println("done with launch scope")

            sum = (0 until numberIterations)
                .map { GlobalScope.async { funC(it) } }
                .map { it.await() }
                .sum()

            sum
        }
        println("sum is $sum, total duration for $numberIterations threads was " + Duration.between(start, Instant.now()))

    }


}