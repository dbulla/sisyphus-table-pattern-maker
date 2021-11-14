package com.nurflugel.sisyphus

import java.time.Duration
import java.time.Instant
import java.util.*

class PerformanceTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val start = Instant.now()
            val n = 10000000
//            val n = 1000
            val primeNumbers = sieveOfEratosthenes(n)
            val end = Instant.now()
            println("Calculating ${primeNumbers.size} prime numbers up to $n took ${Duration.between(start, end)}")
        }

        private fun sieveOfEratosthenes(n: Int): List<Int> {
            val prime = BooleanArray(n + 1)
            Arrays.fill(prime, true)
            var possibleDivisor = 2
            while (possibleDivisor * possibleDivisor <= n) {
                if (prime[possibleDivisor]) {
                    var i = possibleDivisor * 2
                    while (i <= n) {
                        prime[i] = false
                        i += possibleDivisor
                    }
                }
                possibleDivisor++
            }
            val primeNumbers: MutableList<Int> = LinkedList()
            for (i in 2..n) {
                if (prime[i]) {
                    primeNumbers.add(i)
//                    println(i)
                }
            }
            return primeNumbers
        }
    }
}