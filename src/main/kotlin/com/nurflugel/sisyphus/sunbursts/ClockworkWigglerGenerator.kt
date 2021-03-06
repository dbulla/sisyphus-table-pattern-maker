package com.nurflugel.sisyphus.sunbursts

import com.nurflugel.sisyphus.domain.Point
import com.nurflugel.sisyphus.domain.Point.Companion.pointFromRad
import com.nurflugel.sisyphus.gui.GuiController
import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.Math.PI
import kotlin.math.acos
import kotlin.math.cos


class ClockworkWigglerGenerator {

    companion object {

        const val numberOfTicks = 100000
        const val numberOfTicksPerTurn = 1000
        const val numberOfRhos = 1
        const val r0 = 1.2
        const val r1 = 0.2 //1.0 - r0
        const val w0 = 2 * PI / numberOfTicksPerTurn // second hand speed -  1000 ticks per rev
        const val w1 = w0 * 20 // waviness of the tip of the secondhand - 33 revs per rev
        const val deltaRhoPerTurn = .01
        const val thetaAdvance = 1.3
        const val fileName = "clockworkSwirl5WithClippping.thr"


        @JvmStatic
        fun main(args: Array<String>) {
            ClockworkWigglerGenerator().doIt()
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
|   numberOfCounts = $numberOfTicks
|   numberOfRhos = $numberOfRhos
|   r0 = $r0
|   r1 = $r1
|   w0 = $w0
|   w1 = $w1""".trimMargin()
               )

        val points = mutableListOf<Point>()

        val deltaRhoPerCount = deltaRhoPerTurn / numberOfTicksPerTurn
        var rho = r0

        //                val rho = r0 * rhoNumber / numberOfRhos.toDouble() // get the working rho for the iteration
        println("   rho = $rho")
        for (t in 0..numberOfTicks) {
            val thetaInRads = w0 * t * thetaAdvance  // the second hand angle
            val thetaOneInRads = w1 * t                                      // controls the height of the wiggle
            println("t = $t   Initial point: rho = $rho, thetaInDegrees = ${thetaInRads.radsToDegrees()}")
            val deltaRho = r1 * cos(thetaOneInRads)

            println("t = $t   Adjusted point: rho = ${rho + deltaRho}, thetaInDegrees = ${thetaInRads.radsToDegrees()}")

            if (isValid(thetaOneInRads))
                points.add(pointFromRad(rho = rho + deltaRho, thetaInRads = thetaInRads))

            rho -= deltaRhoPerCount
            if (rho < 0.0) break
        }


        val output = mutableListOf<String>()
        //        output.add("// numberOfLines= $numberOfLines")
        output.add("816.6588952562589 0.0")

        points
            .reversed()
            .forEach {

                if (! it.thetaInRads().isNaN()) {
                    output.add("${it.thetaInRads()}  ${it.rho.formatNicely(4)}")
                }
            }

        //        output.add("${points.last().thetaInRads()}  1.0 ")
        //        output.add("${points.last().thetaInRads()}  0.0 ")

        // add this file so we can remember how to get it back!
        val programLines = FileUtils.readLines(File("src/main/kotlin/com/nurflugel/sisyphus/sunbursts/ClockworkWigglerGenerator.kt"))
        output.add("//")
        output.add("// add this file so we can remember how to get it back!")
        output.add("//")
        programLines.forEach { output.add("// $it") }


        FileUtils.writeLines(File(fileName), output)

        val plotterGui = GuiController(output, fileName)
        plotterGui.showGui()
        println("Done!")
    }

    /** Determine how to clip the delta Rho */
    private fun isValid(thetaOneInRads: Double): Boolean {
        val cosTheta = cos(thetaOneInRads)
        val trimmedTheta = acos(cosTheta)
        // what the above does is strip away any theta greater than 2 PI.

        val thetaInDegrees = trimmedTheta.radsToDegrees()
        println("Initial theta: ${thetaOneInRads.radsToDegrees()} trimmedThetaInDegrees = $thetaInDegrees")
        val angleSpread = 90
        val startAngle = (180 - angleSpread / 2.0)
        val isValid = thetaInDegrees > startAngle && thetaInDegrees <= 180
        return isValid
    }


    private fun Double.formatNicely(digits: Int): String {
        val number = when {
            this > 1.0 -> 1.0
            this < 0.0 -> 0.0
            else       -> this
        }
        return "%.${digits}f".format(number)
    }

    private fun Double.radsToDegrees(): Double = this * 180.0 / PI
    private fun Double.degreesToRads(): Double = this / 180.0 * PI
}

