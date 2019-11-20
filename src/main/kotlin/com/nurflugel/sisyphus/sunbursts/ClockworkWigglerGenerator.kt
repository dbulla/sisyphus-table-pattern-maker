package com.nurflugel.sisyphus.sunbursts

import com.nurflugel.sisyphus.domain.Point
import com.nurflugel.sisyphus.domain.Point.Companion.pointFromRad
import com.nurflugel.sisyphus.gui.GuiController
import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.Math.PI
import kotlin.math.cos


class ClockworkWigglerGenerator {

    companion object {

        const val numberOfCounts = 100000
        const val numberOfCountsPerTurn = 1000
        const val numberOfRhos = 1
        const val r0 = 0.985
        const val r1 = 1.0 - r0
        const val w0 = 2 * PI / 1000
        const val w1 = w0 * 100
        const val deltaRhoPerTurn = .01
        const val fileName = "clockworkSwirl.thr"


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
|   numberOfCounts = $numberOfCounts
|   numberOfRhos = $numberOfRhos
|   r0 = $r0
|   r1 = $r1
|   w0 = $w0
|   w1 = $w1""".trimMargin()
               )

        val points = mutableListOf<Point>()

        val deltaRhoPerCount = deltaRhoPerTurn / numberOfCountsPerTurn
        var rho = r0

        //                val rho = r0 * rhoNumber / numberOfRhos.toDouble() // get the working rho for the iteration
        println("   rho = $rho")
        for (t in 0..numberOfCounts) {
            val thetaInRads = w0 * t * 1.1  // the second hand angle
            val thetaOneInRads = w1 * t                                      // controls the height of the wiggle
            println(
                "t = $t   Initial point: rho = $rho, thetaInRads = $thetaInRads, thetaInDegrees = ${thetaInRads * 180.0 / PI}"
                   )
            val deltaRho = r1 * cos(thetaOneInRads)

            println(
                "t = $t   Adjusted point: rho = ${rho + deltaRho}, thetaInRads = $thetaInRads, thetaInDegrees = ${thetaInRads * 180.0 / PI}"
                   )
            points.add(pointFromRad(rho = rho + deltaRho, thetaInRads = thetaInRads))
            rho -= deltaRhoPerCount
            if (rho < 0.0) break
        }


        val output = mutableListOf<String>()
        //        output.add("// numberOfLines= $numberOfLines")

        points.forEach {

            if (! it.thetaInRads().isNaN()) {
                output.add("${it.thetaInRads()}  ${it.rho.formatNicely(4)}")
            }
        }

        //        output.add("${points.last().thetaInRads()}  0.0 ")
        output.add("${points.last().thetaInRads()}  0.0 ")


        FileUtils.writeLines(File(fileName), output)

        val plotterGui = GuiController(output, fileName)
        plotterGui.showGui()
        println("Done!")
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

