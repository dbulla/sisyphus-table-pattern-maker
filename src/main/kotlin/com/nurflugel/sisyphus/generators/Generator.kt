package com.nurflugel.sisyphus.generators

import com.nurflugel.sisyphus.generators.domain.Point
import com.nurflugel.sisyphus.generators.domain.Point.Companion.isRhoPracticallyOne
import com.nurflugel.sisyphus.generators.domain.Point.Companion.isRhoPracticallyZero
import com.nurflugel.sisyphus.generators.domain.Shape
import com.nurflugel.sisyphus.generators.shapes.*
import com.nurflugel.sisyphus.output.gui.GuiPreviewer
import org.apache.commons.io.FileUtils
import java.io.File
import java.time.LocalDateTime
import kotlin.math.PI

class Generator {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Generator().doIt()
        }
    }

    fun doIt() {
        // take the basic shape - make X copies for 1 revolution.  With each full resolution, reduce the rho by a %
        //            val template = SharpSawtooth()
        //            val template = TriangleSawtooth()
//        val template = Triangle()
//        val template = NarrowTriangle()
        val template = Diamond()
//        val template = Hexagon()
//        val template = Pentagon()
//        val template = Septagon()
//        val template = Nonagon()
        //        val template = Rectangle()
        //        val template = Square()

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

        //copy the base shape into a new shape, offset by delta theta.
        //rho varies from previous shape by rhoRemainingPerRev/nu

        // todo - make this able to have
        val adjustedPoints = (0..numberOfCopies)
            .map { template.withOffset(deltaRhoPerCopy, deltaThetaPerCopy, it) } // generate the offset shape
            .flatMap { it.segments } // flatten the shapes into their segments
            .flatMap { it.generateSubSegments() } // transform the list of shapes into the list of segments
            .flatMap { it.points(false) } // convert the segments into points
            .map { adjustRho(it) } // round rho up or down if it's really close to 0 or 1
        /////////////////////
        val numberOfSwitches = 4
        val offsetShapes = (0..numberOfCopies).map { copyNumber ->
            val modDeltaTheta = deltaThetaPerCopy * computeSwitchFactor(copyNumber, numberOfCopies, numberOfSwitches)
            template.withOffset(deltaRhoPerCopy, modDeltaTheta, copyNumber)
        }

        val flattenedShapes = offsetShapes.flatMap { it.segments }
        val generatedSubSegments = flattenedShapes.flatMap { it.generateSubSegments() }
        val convertedSegmentsIntoPoints = generatedSubSegments.flatMap { it.points(false) }
        val adjustedPoints2 = convertedSegmentsIntoPoints.map { adjustRho(it) }

        /////////////
//        val trimmedPoints = trimPoints(adjustedPoints)
        val trimmedPoints = trimPoints(adjustedPoints2)
        val dedupedPoints = eliminateSuccessiveDupes(trimmedPoints)
//        val dedupedPoints = eliminateSuccessiveDupes(adjustedPoints2)

        val finalPoints: List<Point> = ensureLinesEndWithZeroOrOne(dedupedPoints)
        val lines = finalPoints
            .map { "${Math.abs(it.thetaInRads())} \t${it.rho}" }
            .toMutableList()

        template.addDescriptionLines(lines)

        getLinesWithCodeFileAsComments(lines, template)

        val fullFileName = template.fileName + LocalDateTime.now()+".thr"
        println("fullFileName = $fullFileName")
        FileUtils.writeLines(File(fullFileName), lines)

//        val plotterGui = GuiPreviewer()
//        plotterGui.initialize()
//        plotterGui.showPreview(template.fileName, lines, false)
//        plotterGui.showPreview(template.fileName, lines, false)
    }

    private fun computeSwitchFactor(copyNumber: Int, numberOfCopies: Int, numberOfSwitches: Int): Int {
        return -1
//        val value = copyNumber / (numberOfCopies / numberOfSwitches)
//        return if (value % 2 == 0) 1 else -1
    }

    /**
     * This may seem strange, but since I play with the files all the time, even saving the config might not let
     * me reproduce a track.  Therefore, this serializes the entire shape file as comments in the track!  To recover,
     * just copy/paste the comment into the file name and
     */
    private fun getLinesWithCodeFileAsComments(
        lines: MutableList<String>, shape: Shape,
    ): List<String> {
        val className = shape.javaClass.name.replace('.', '/')
        val fileName = "src/main/kotlin/$className.kt"
        val classFile = File(fileName)
        val fileLines = FileUtils.readLines(classFile, "UTF-8")
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
            if (!point.isEqual(previousPoint)) {
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

        if (isRhoPracticallyOne(lastPoint.rho)) return points
        if (isRhoPracticallyZero(lastPoint.rho)) return points

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
            .all { points[it].isRhoPracticallyZero() }
        return !allZero
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


}