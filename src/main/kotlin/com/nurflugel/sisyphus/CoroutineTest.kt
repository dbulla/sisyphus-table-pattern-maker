package com.nurflugel.sisyphus

import kotlinx.coroutines.*
import java.time.Instant
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class CoroutineTest {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            CoroutineTest().doIt()
        }
    }

    private val tripletsPool = ThreadPoolExecutor(3, 3, 5L, TimeUnit.SECONDS, LinkedBlockingQueue())

    private suspend fun sum(scheduler: ThreadPoolExecutor): Int = coroutineScope {

        withContext(scheduler.asCoroutineDispatcher()) {
            val a = async { funA() }
            val b = async { funB() }
            val c = async { funC() }

            a.await() + b.await() + c.await()
        }
    }

    private fun funA(): Int {
        println("funA")
        Thread.sleep(2000L)

        return 1

    }

    private fun funB(): Int {
        println("funB " + Instant.now())
        Thread.sleep(3000L)
        return 2
    }

    private fun funC(): Int {
        println("funC")
        Thread.sleep(5000L)
        return 3
    }

    private fun sum2(): Int {
        var sum = 0
        runBlocking {
            val jobA = async { funAA() }
            val jobB = async { funBB() }
            runBlocking {
                sum = jobB.await() + jobA.await()
            }
        }
        return sum
    }

    private fun funAA(): Int {
        println("funAA start " + Instant.now().nano)
        Thread.sleep(5000L)
        println("funAA end " + Instant.now().nano)
        return 1
    }

    private fun funBB(): Int {
        println("funBB start " + Instant.now().nano)
        Thread.sleep(3000L)
        println("funBB end " + Instant.now().nano)
        return 2
    }

    fun doIt() {

        //        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {


        println("here")
        val sum2 = sum2()
        println(">>>>sum2>>$sum2<<<<<<<")
        GlobalScope.launch {
            val sum = sum(tripletsPool)
            println(">>>sum>>>$sum<<<<<<<")
        }

        //        }

    }
}