package com.nurflugel.sisyphus.generators.shapes

import com.nurflugel.sisyphus.generators.domain.LinearSegment
import com.nurflugel.sisyphus.generators.domain.Point
import com.nurflugel.sisyphus.generators.domain.Shape


/**  This class makes a shape that spans the table and looks like this (the dot is the table center):
 *                         |
 *    * ---- *             |
 *     \ . /               |
 *      \/                 |
 *      *
 *
 */
class Septagon(
    numberOfCopiesPerRev: Int = 2,
    rhoRemainingPerRev: Double = 1 - (.03 / 3),
    numberOfRevs: Int = 100,
    offsetPerRevInDegrees: Int = 3,
    fileName: String = "spiralSeptagon.thr",
) : Shape(
    numberOfCopiesPerRev = numberOfCopiesPerRev,
    rhoRemainingPerRev = rhoRemainingPerRev,
    numberOfRevs = numberOfRevs,
    offsetPerRevInDegrees = offsetPerRevInDegrees,
    fileName = fileName,
    numberOfSubSegments = 20
) {

    override fun defineSegments() { // hmm, pass in delta rho and theta here, and you've got a constructor...
        val point1 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 0 * 360.0/7)
        val point2 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 1 * 360.0/7)
        val point3 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 2 * 360.0/7)
        val point4 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 3 * 360.0/7)
        val point5 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 4 * 360.0/7)
        val point6 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 5 * 360.0/7)
        val point7 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 6 * 360.0/7)
        val point8 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 0.0, numberOfTurns = 1) // notice for this shape we must end at 360, not 0 (wrong direction)

        // this makes 2 segments
        segments.addAll(
            mutableListOf(
                LinearSegment(point1, point2, numberOfSubSegments),
                LinearSegment(point2, point3, numberOfSubSegments),
                LinearSegment(point3, point4, numberOfSubSegments),
                LinearSegment(point4, point5, numberOfSubSegments),
                LinearSegment(point5, point6, numberOfSubSegments),
                LinearSegment(point6, point7, numberOfSubSegments),
                LinearSegment(point7, point8, numberOfSubSegments),
            )
        )
    }

    /** Create a new share at the given coordinates */
    override fun withOffset(deltaRho: Double, deltaTheta: Double, iteration: Int): Shape {
        return Septagon(numberOfCopiesPerRev,
                        rhoRemainingPerRev,
                        numberOfRevs,
                        offsetPerRevInDegrees,
                        fileName,
                        segments
                            .map { s -> s.withOffset(deltaRho, deltaTheta, iteration) }
                            .toMutableList())
    }

    constructor(
        numberOfCopiesPerRev: Int,
        rhoRemainingPerRev: Double,
        numberOfRevs: Int,
        offsetPerRevInDegrees: Int,
        fileName: String,
        segments: MutableList<LinearSegment>,
    ) : this(
        numberOfCopiesPerRev, rhoRemainingPerRev, numberOfRevs, offsetPerRevInDegrees, fileName
    ) {
        this.segments.addAll(segments)
    }
}


