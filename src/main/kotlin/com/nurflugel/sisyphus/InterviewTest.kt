package com.nurflugel.sisyphus

import java.time.Duration
import java.time.Instant
import kotlin.random.Random

class InterviewTest {

    companion object {

        val leftData = List(200000) { Random.nextInt(0, 1000) }.sorted().toIntArray()
        val rightData = List(500000) { Random.nextInt(0, 1000) }.sorted().toIntArray()

        @JvmStatic
        fun main(args: Array<String>) {
            println("Starting 1...")
            val start = Instant.now()
            try {
                InterviewTest().doIt()
            } catch (e: Exception) {
                println("e = ${e}")
            }
            val end = Instant.now()

            println("Starting 2...")
            val start2 = Instant.now()
            InterviewTest().doItWithPointers()
            val end2 = Instant.now()

            println("With lists = ${Duration.between(start, end)}")
            println("With Int Arrays = ${Duration.between(start2, end2)}")
        }
    }

    /**
     * Quick and dirty, but low chance of error in implementation
     */
    fun doIt() {
        // take the test data, split it up into two already-sorted arrays.
        val leftArray = leftData.asList().toMutableList()
        val rightArray = rightData.asList().toMutableList()

        val newArray = mutableListOf<Int>()

        // take the lowest of each side, and put that into the new array
        while (leftArray.isNotEmpty() && rightArray.isNotEmpty()) {
            if (leftArray[0] > rightArray[0]) {
                newArray.add(rightArray[0])
                rightArray.removeAt(0)
            } else {
                newArray.add(leftArray[0])
                leftArray.removeAt(0)
            }
        }
        // continue until one array is empty - then put the rest of the remaining elements into the new array
        if (leftArray.isNotEmpty()) {
            newArray.addAll(leftArray)
        } else {
            newArray.addAll(rightArray)
        }

        //        val output = " New array is: " + newArray.joinToString(", ")

        //        println(output)

    }

    /**
     * An alternate way to do this w/o disturbing the original data would be to use arrays with pointers
     */

    fun doItWithPointers() {
        // take the test data, split it up into two already-sorted arrays.

        val leftArray = leftData
        val rightArray = rightData

        val newArray = IntArray(leftArray.size + rightArray.size) { 0 }

        var leftPointer = 0
        var rightPointer = 0
        var newArrayPointer = 0

        // take the lowest of each side, and put that into the new array
        while (leftPointer < leftArray.size && rightPointer < rightArray.size) {
            if (leftArray[leftPointer] > rightArray[rightPointer]) {
                newArray[newArrayPointer] = rightArray[rightPointer]
                rightPointer ++
            } else {
                newArray[newArrayPointer] = leftArray[leftPointer]
                leftPointer ++
            }
            newArrayPointer ++
        }
        // continue until one array is empty - then put the rest of the remaining elements into the new array
        if (leftPointer < leftArray.size) {
            leftArray.copyInto(newArray, newArrayPointer, leftPointer, leftArray.size)
        } else {
            rightArray.copyInto(newArray, newArrayPointer, rightPointer, rightArray.size)
        }

        //        val output = " New array is: " + newArray.joinToString(", ")
        //
        //        println(output)

    }


}