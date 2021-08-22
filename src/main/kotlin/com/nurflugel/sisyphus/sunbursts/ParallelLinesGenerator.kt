package com.nurflugel.sisyphus.sunbursts

import com.nurflugel.sisyphus.domain.LinearSegment
import com.nurflugel.sisyphus.domain.Point.Companion.pointFromRad
import com.nurflugel.sisyphus.gui.GuiController
import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.Math.PI
import kotlin.math.asin


class ParallelLinesGenerator {

    companion object {

        const val numberOfSegments = 100
        const val numberOfLines = 200


        @JvmStatic
        fun main(args: Array<String>) {
            ParallelLinesGenerator().doIt()
        }
    }

    /**
     * this is a little different than most generators, as it' not theta that changes... but the distance between the origin and the perpendicular to the line.
     *
     * Call that "d", and for each d, we generate the corresponding theta for the intersection at rho = 1 (the outside of the circle).
     *
     * From that theta, we get an opposite intersecting angle which is (pi - 2 * theta).  So, we want a straight line between ((pi - 2 * theta), 1) and (theta, 1).
     * But if we put that into Sisyphus, we'll just get an arc at rho-1 between -theta and theta.  So, we then break that segment up into
     * many smaller segments, where the curvature is so small it won't get noticed.
     *
     * For a full line through the middle, that's going to be at least 20 or so.
     *
     * Question - how do we deal with a negative d?  For symmetrical lines, not a problem.  But, change to exponential distribution, and
     * the top/bottom are no longer symmetrical, so we can't reuse the opposite results.
     *
     */
    fun doIt() {
        println("numberOfSegments= $numberOfSegments")
        println("numberOfLines= $numberOfLines")
        val sliceSize = 2.0 / numberOfLines
        var directionFlipper = false

        // first, nice and easy, let's just do numberOfLines lines, constant spacing.  Top to bottom in numberOfLines steps.
        val intProgression = (numberOfLines downTo 0)
            .map { sliceSize * it } // D

        // sample list of whatever we want
        val dList = listOf(1.75, 1.25, 0.75, 0.25) //list of Ds


        //        val coords = intProgression
        //        val coords = dList
        //                    val coords = getLinearD()
        val coords = getLinearDExp()

            .asSequence() // more performant
            .map { D ->
                println("D = $D")
                D - 1
            }

            //            .filter {  != 0.0 } 
            .map { d ->
                println("d = $d")
                calculateThetas(d)
            }
            .map { pair ->
                // reverse direction every line so it's a zig-zag drawing pattern and avoid endless trips around the rim
                directionFlipper = ! directionFlipper
                when {
                    directionFlipper -> LinearSegment(pointFromRad(rho = 1.0, thetaInRads = pair.first),
                                                      pointFromRad(rho = 1.0, thetaInRads = pair.second),
                                                      numberOfSegments
                                                     )
                    else             -> LinearSegment(pointFromRad(rho = 1.0, thetaInRads = pair.second),
                                                      pointFromRad(rho = 1.0, thetaInRads = pair.first),
                                                      numberOfSegments
                                                     )
                }
            }

            // that gave us a pair of coordinate pairs, which we'll have to split into many sub-segments
            .map { linearSegment -> linearSegment.generateSubSegments() }
            .flatten() // break up the list of lists into a single list
            .toList()


        // now generate .thr file
        val output = mutableListOf<String>()
        output.add("// numberOfSegments= $numberOfSegments")
        output.add("// numberOfLines= $numberOfLines")


        // add all the points in the now-fractioned segments
        coords.forEach {
            if (! it.startPoint.rho.isNaN() && ! it.endPoint.rho.isNaN()) {
                output.add("${it.startPoint.thetaInRads()}  ${it.startPoint.rho.formatNicely(4)}")
                output.add("${it.endPoint.thetaInRads()}   ${it.endPoint.rho.formatNicely(4)}")
            } else {
                println("Funky data!  ${it.startPoint} ${it.endPoint}")
            }
        }

        // add a nice ring around the outside
        val lastPoint = coords.last().endPoint
        val ringPoint = pointFromRad(lastPoint.thetaInRads() + 2 * PI, 1.0)

        output.add(" // add a nice ring around the outside")
        output.add("" + ringPoint.thetaInRads() + "    " + ringPoint.rho.formatNicely(4))


        FileUtils.writeLines(File("parallel_lines.thr"), output)

        val plotterGui = GuiController()
        plotterGui.showPreview("parallel_lines.thr", output, false)
        println("Done!")
    }

    /** in the zone between 2 and 0, generate a list of increasing D values */
    private fun getLinearDExp(): List<Double> {
        val list = mutableListOf<Double>()
        var d: Double = 2.0
        val initialSpacing = d / 400.0
        var spacing = initialSpacing


        while (d > 0.0) {
            list.add(d)
            d -= spacing
            spacing *= 1.2
        }

        return list
    }

    /** in the zone between 2 and 0, generate a list of increasing D values */
    private fun getLinearD(): List<Double> {
        val list = mutableListOf<Double>()
        var d: Double = 2.0
        val initialSpacing = d / 400.0
        var spacing = initialSpacing


        while (d > 0.0) {
            list.add(d)
            d -= spacing
            spacing += initialSpacing * 3
        }

        return list
    }

    private fun Double.formatNicely(digits: Int): String {
        val number = when {
            this > 1.0 -> 1.0
            this < 0.0 -> 0.0
            else       -> this
        }
        return "%.${digits}f".format(number
                                    )
    }

    /**
     * Take the given distance from the center, and return a pair of thetas - the intersection angles between the line and the circle of
     * the outside table radius (where r = 1)
     *
     * @param d If positive, we're "above" the center of the circle, in Zone I.  If negative, we're below the center, in Zone II
     */
    private fun calculateThetas(d: Double): Pair<Double, Double> {

        val pair = when {
            d > 0.0 -> {
                val theta = asin(d)
                val otherTheta = PI - 2 * theta + theta
                Pair(theta, otherTheta)

            }
            else    -> {
                val theta = asin(d)
                val otherTheta = - theta - (PI) // + 2 * theta)
                Pair(theta, otherTheta)
            }
        }
        println("d = $d, Theta pair = ${pair.first * 180.0 / PI},${pair.second * 180 / PI}")
        return pair

    }

}
