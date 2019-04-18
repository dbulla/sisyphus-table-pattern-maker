package com.nurflugel.sisyphus

import com.nurflugel.sisyphus.gui.GuiController
import org.apache.commons.io.FileUtils
import java.io.File
import kotlin.math.PI

class Generator {
    fun doIt() {
        // take the basic shape - make X copies for 1 revolution.  With each full resolution, reduce the rho by a %
        //    val template = SharpSawtooth()
        //        val template = TriangleSawtooth()
        val template = Triangle()
        //    val template = Square()

        val numberOfCopiesPerRev = template.numberOfCopiesPerRev // how many copies around the circle?
        val offsetPerRevInDegrees = template.offsetPerRevInDegrees // for each run around the circle, how many degrees will the next run be offset?
        val rhoRemainingPerRev = template.rhoRemainingPerRev // for each run around the circle, how much will rho shrink (0-1)
        val numberOfRevs = template.numberOfRevs  // how many times around the circle?
        val offsetPerRevInRads = offsetPerRevInDegrees * PI / 180 / numberOfCopiesPerRev
        val deltaThetaPerCopy = PI / numberOfCopiesPerRev + offsetPerRevInRads // how much theta varies per copy
        val deltaRhoPerCopy = (1.0 - rhoRemainingPerRev) / numberOfCopiesPerRev // how much rho varies per copy

        template.defineSegments()

        val numberOfCopies = numberOfCopiesPerRev * numberOfRevs
        //    val numberOfCopies = 0

        //             copy the base shape into a new shape, offset by delta theta.
        //             rho varies from previous shape by rhoRemainingPerRev/nu

        val allShapes = (0..numberOfCopies)
            .map { template.withOffset(deltaRhoPerCopy, deltaThetaPerCopy, it) }
        allShapes.forEach { println("allShapes = $it") }
        println()

        val allSegments = allShapes // generate the offset shape
            .flatMap { s -> s.segments }
        allSegments.forEach { println("allSegments = $it") }
        println()

        val subSegments = allSegments
            .flatMap { s -> s.generateSubSegments() }
        subSegments.forEach { println("subSegments = $it") }
        println()

        val allPoints = subSegments // transform the list of shapes into the list of segments
            .flatMap { ss -> ss.points(false) } // for triangle, going back to 0 should be going to 360
        allPoints.forEach { println("allPoints = $it") }
        println()


        val adjustedPoints = allPoints // convert the segments into points
            .map { p -> adjustRho(p) }
        adjustedPoints.forEach { println("adjustedPoints = $it") }
        println()

        //        val adjustedPoints = (0..numberOfCopies)
        //                .map { template.withOffset(deltaRhoPerCopy, deltaThetaPerCopy, it) }// generate the offset shape
        //                .flatMap { s -> s.segments }// transform the list of shapes into the list of segments
        //                .flatMap { ss -> ss.points(true) }// convert the segments into points
        //                .map { p -> adjustRho(p) }

        //    val lines = trimPoints(allPoints)
        //        .map(it->eliminateSuccessiveDupes(it))
        //        .map { "${it.theta}  ${it.rho}" }


        val points = trimPoints(adjustedPoints)
        val dedupedPoints = eliminateSuccessiveDupes(points)

        val finalPoints: List<Point> = ensureLinesEndWithZeroOrOne(dedupedPoints)
        val lines = finalPoints
            .map { "${it.thetaInRads()} \t${it.rho}" }
            .toMutableList()

        template.addDescriptionLines(lines)

        val linesDegrees = finalPoints
            .map {
                "%4.2f".format(it.thetaInDegrees()) + ", \t\t" + "%.3f".format(it.rho) + ",\t\t" + "%.2f".format(it.thetaInRads()
                                                                                                                ) + ",\t\tx=" + "%.2f".format(
                    it.x
                                                                                                                                             ) + ",\t\ty=" + "%.2f".format(
                    it.y
                                                                                                                                                                          )
            }

        getlinesWithCodeFileAsComments(lines, template.javaClass)

        FileUtils.writeLines(File(template.fileName), lines)

        linesDegrees.withIndex().forEach { (i, point) ->
            println("point[$i] = $point")
        }

        val plotterGui = GuiController(lines, template.fileName)
        plotterGui.showGui()
    }

    /** This may seem strange, but since I play with the files all the time, even saving the config might not let
     * me reproduce a track.  Therefore, this serializes the entire shape file as comments in the track!  To recover,
     * just copy/paste the comment into the file name and
     */
    private fun getlinesWithCodeFileAsComments(lines: MutableList<String>, javaClass: Class<Triangle>): List<String> {
        val className = javaClass.name.replace('.', '/')
        val fileName = "src/main/kotlin/$className.kt"
        val classFile = File(fileName)
        val fileLines = FileUtils.readLines(classFile)
            .map { "# $it" }
        lines.add("# source file name: $fileName")
        lines.addAll(fileLines)
        return lines
    }


    /** Go through the list, and if two successive points are identical, then skip the 2nd */
    private fun eliminateSuccessiveDupes(points: List<Point>): MutableList<Point> {
        val filteredPoints = mutableListOf<Point>()
        var previousPoint: Point? = null
        points.forEach { point ->
            if (! point.isEqual(previousPoint)) {
                filteredPoints.add(point)
                previousPoint = point
            }
        }
        return filteredPoints
    }

    private fun ensureLinesEndWithZeroOrOne(points: List<Point>): List<Point> {

        val lastPoint = points.last()
        val list = mutableListOf<Point>()
        list.addAll(points)
        val howCloseToOne = 1.0 - lastPoint.rho
        val howCloseToZero = lastPoint.rho

        if (Point.isRhoPracticallyOne(lastPoint.rho)) return points
        if (Point.isRhoPracticallyZero(lastPoint.rho)) return points

        val finalRho = when {
            howCloseToOne < howCloseToZero -> 1.0
            else                           -> 0.0
        }

        list.add(Point.pointFromRad(rho = finalRho, thetaInRads = lastPoint.thetaInRads(), numberOfTurns = lastPoint.numberOfTurns))
        return list
    }

    // If there are more than 5 zero values for rho, we're done -
    private fun trimPoints(points: List<Point>): List<Point> {
        val numberOfZeroPoints = 5
        return when {
            points.size < 100 -> points
            else              -> (0..points.size - numberOfZeroPoints)
                .takeWhile { allNonZero(points, it, numberOfZeroPoints) }
                .map { points[it] }
        }
    }

    /** If all numberOfZeroPoints points are practically zero, then we're done */
    private fun allNonZero(points: List<Point>, index: Int, numberOfZeroPoints: Int): Boolean {

        val allZero = (index..numberOfZeroPoints + index)
            .all { it -> points[it].isRhoPracticallyZero() }
        return ! allZero
    }

    /** If rho is < .05, call it 0.  Return a new point with the same theta, but 0 rho.
     *
     * Similarly, clip anything beyond 1.0 to 1.0.
     *
     * Else, return the original point.
     */
    private fun adjustRho(p: Point): Point {
        return when {
            p.isRhoPracticallyZero() -> Point.pointFromRad(0.0, p.thetaInRads(), p.numberOfTurns)  // round to 0
            p.isRhoPracticallyOne()  -> Point.pointFromRad(1.0, p.thetaInRads(), p.numberOfTurns)   // round to 1
            else                     -> p
        }
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Generator().doIt()
        }
    }
}