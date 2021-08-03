package com.nurflugel.sisyphus.sunbursts

import com.nurflugel.sisyphus.domain.Point
import com.nurflugel.sisyphus.domain.Point.Companion.pointFromRad
import com.nurflugel.sisyphus.gui.GuiController
import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.Math.PI
import java.nio.charset.Charset
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
        var waviness: Double = 20.0
        var w1 = w0 * waviness // waviness of the tip of the secondhand - 33 revs per rev
        const val deltaRhoPerTurn = .01
        const val thetaAdvance = 1.3
        var fileName = "clockworkSwirl6_${numberOfTicksPerTurn}_$waviness.thr"

        @JvmStatic
        fun main(args: Array<String>) {
            val values = listOf(
                0,
                .001,
                .01,
                .02,
                .03,
                .04,
                .05,
                .06,
                .07,
                .08,
                .09,
                .1, .2, .3, .4, .5, .6, .7, .8, .9,
                //                1,
                //                2,
                //                3,
                //                4,
                //                5,
                //                6,
                //                7,
                //                8,
                //                9,
                //                10,
                //                11,
                //                12,
                //                13,
                //                14,
                //                15,
                //                16,
                //                17,
                //                18,
                //                19,
                //                20,
                //                30,
                //                40,
                //                60,
                //                80,
                //                100,
                //                120,
                //                180,
                //                200,
                //                240,
                //                300,
                //                330,
                //                400,
                //                1000,
                //                9999
                               )
            values.forEach {
                waviness = it.toDouble()
                fileName = "clockworkSwirl6_${numberOfTicksPerTurn}_$waviness.thr"

                w1 = w0 * waviness // waviness of the tip of the secondhand - 33 revs per rev

                ClockworkWigglerGenerator().doIt()
            }
            //            for (i in values) {
            //                waviness = i as Double
            //                ClockworkWigglerGenerator().doIt()
            //            }
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

        println(
            """
|   numberOfCounts = $numberOfTicks
|   numberOfRhos =   $numberOfRhos
|   waviness =       $waviness
|   r0 =             $r0
|   r1 =             $r1
|   w0 =             $w0
|   w1 =             $w1""".trimMargin()
               )

        val points = mutableListOf<Point>()

        val deltaRhoPerCount = deltaRhoPerTurn / numberOfTicksPerTurn
        var rho = r0

        println("   rho = $rho")
        for (t in 0..numberOfTicks) {
            val thetaInRads = w0 * t * thetaAdvance  // the second hand angle
            val thetaOneInRads = w1 * t              // controls the height of the wiggle
            val deltaRho = r1 * cos(thetaOneInRads)

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
            .filter { ! it.thetaInRads().isNaN() }
            .forEach {
                output.add("${it.thetaInRads()}  ${it.rho.formatNicely(4)}")
            }

        // add the contents of this file, so we can remember how to get it back!
        val programLines = FileUtils.readLines(File("src/main/kotlin/com/nurflugel/sisyphus/sunbursts/ClockworkWigglerGenerator.kt"), Charset.defaultCharset())
        output.add("//")
        output.add("// add this file so we can remember how to get it back!")
        output.add("//")
        programLines.forEach { output.add("// $it") }

        FileUtils.writeLines(File(fileName), output)

        val plotterGui = GuiController(output, fileName)
        plotterGui.showPreview(fileName) // show the preview
        println("Done!")
    }

    /** Determine how to clip the delta Rho */
    private fun isValid(thetaOneInRads: Double): Boolean {
        val cosTheta = cos(thetaOneInRads)
        val trimmedTheta = acos(cosTheta)
        // what the above does is strip away any theta greater than 2 PI.

        val thetaInDegrees = trimmedTheta.radsToDegrees()
        //        println("Initial theta: ${thetaOneInRads.radsToDegrees()} trimmedThetaInDegrees = $thetaInDegrees")
        val angleSpread = 90
        val startAngle = (180 - angleSpread / 2.0)
        return thetaInDegrees > startAngle && thetaInDegrees <= 180
    }


    /**
     * Take the number and, if it's > 1, replace it with 1, if
     * it's < 0, replace it with 0.  Else, trim to the number of digits
     * @param digits Number of digits after the decimal
     */
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

