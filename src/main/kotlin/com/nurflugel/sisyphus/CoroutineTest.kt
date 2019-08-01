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
            CoroutineTest().doIt()
        }
    }

    fun doIt() {
        val start = Instant.now()
        println("Starting test")
        val sum = runBlocking {
            println("in sum2")
            var sum = 0

            println("In launch scope")
            // these run in the order they're declared
            val jobA = GlobalScope.async { funC(3) }
            val jobB = GlobalScope.async { funC(5) }
            val jobC = GlobalScope.async { funC(2) }
            sum = jobB.await() + jobA.await() + jobC.await()
            println("done with launch scope")
            sum
        }
        println("sum is $sum, total duration was " + Duration.between(start, Instant.now()))

    }

    private suspend fun funC(delay: Int): Int {
        val start = Instant.now()
        println("fun $delay start")
        delay(delay * 1000L)
        println("fun $delay end, duration was " + Duration.between(start, Instant.now()))
        return delay
    }
}