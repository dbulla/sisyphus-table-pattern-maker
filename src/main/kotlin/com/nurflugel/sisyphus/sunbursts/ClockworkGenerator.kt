package com.nurflugel.sisyphus.sunbursts

import com.nurflugel.sisyphus.domain.Point
import com.nurflugel.sisyphus.domain.Point.Companion.pointFromRad
import com.nurflugel.sisyphus.gui.GuiController
import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.Math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sin
import kotlin.math.sqrt


class ClockworkGenerator {

    companion object {

        const val numberOfCounts = 4000
        const val numberOfRhos = 1
        const val r0 = 0.85
        const val r1 = 0.15
        const val w0 = 2 * PI / numberOfCounts
        const val w1 = w0 * - 20


        @JvmStatic
        fun main(args: Array<String>) {
            ClockworkGenerator().doIt()
        }
    }

    /**
     * Think of a secondhand on a clock as the end traces out a circle as it turns.
     *
     * Now, add another, smaller clock at the end of the second hand - as it's second hand turns at a different rate from the main.
     *
     * What sort of pattern does it trace?  What if that one also had an even smaller second hand at it's end?
     *
     * Requirements - all r0 through rn MUST equal 1 for the table to start nicely.  Else, add an extra start point at rho=0 or 1 to appease the table gods.
     *
     */
    fun doIt() {

        println("""
|   numberOfCounts = $numberOfCounts
|   numberOfRhos = $numberOfRhos
|   r0 = $r0
|   r1 = $r1
|   w0 = $w0
|   w1 = $w1""".trimMargin()
               )

        val points = (numberOfRhos downTo 1)
            .map {
                println("rho number $it")
                it
            }
            .map { r0 * it / numberOfRhos.toDouble() }
            .map { rho ->
                println("   rho = $rho")
                (0..(numberOfCounts))
                    .map { t ->
                        val thetaInRads = w0 * t
                        println(
                            "t = $t   Initial point: rho = $rho, thetaInRads = $thetaInRads, thetaInDegrees = ${thetaInRads * 180.0 / PI}"
                               )
                        val point0 = pointFromRad(rho = rho, thetaInRads = thetaInRads)
                        calculateClockworkPoint(point0, r1, w1 * t)
                    }
            }.flatten()


        val output = mutableListOf<String>()
        //        output.add("// numberOfLines= $numberOfLines")

        points.forEach {
            if (! it.thetaInRads().isNaN()) {
                output.add("${it.thetaInRads()}  ${it.rho.formatNicely(4)}")
            }
        }

        //        output.add("${points.last().thetaInRads()}  0.0 ")
        output.add("${points.last().thetaInRads()}  1.0 ")

        FileUtils.writeLines(File("clockwork.thr"), output)

        val plotterGui = GuiController()
        plotterGui.showPreview("clockwork.thr", output, false, false)
        println("Done!")
    }


    /** Take the initial point's rho/theta and add the extra turns for this */
    private fun calculateClockworkPoint(startPoint: Point, r: Double, theta: Double): Point {
        val e = r * sin(PI / 2 - theta)
        val d = sqrt(r * r - e * e)
        val rPrime = e + sqrt(startPoint.rho * startPoint.rho + d * d)
        val d1 = rPrime - e

        val alpha = when { // deal with division by 0
            abs(d1) > .000000001 -> {
                acos((startPoint.rho / d1).cleanAcosValue())
            }
            else                 -> 0.0
        }

        val thetaPrime = startPoint.thetaInRads() + alpha
        val newPoint = pointFromRad(rho = rPrime, thetaInRads = thetaPrime)

        println("newPoint rho = ${newPoint.rho}, thetaInRads = ${newPoint.thetaInRads()}, thetaInDeg = ${newPoint.thetaInDegrees()}")
        return newPoint
    }

    private fun Double.formatNicely(digits: Int): String {
        val number = when {
            this > 1.0 -> 1.0
            this < 0.0 -> 0.0
            else       -> this
        }
        return "%.${digits}f".format(number)
    }
}

/** Asin, Acos, etc - they don't like values > 1 - so, if due to arithmatic errors, clean it to 1.  Same with 0? */
private fun Double.cleanAcosValue(): Double {
    return when {
        this > 1.0 -> 1.0
        else       -> this
    }
}
