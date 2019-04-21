package com.nurflugel.sisyphus.shapes

import com.nurflugel.sisyphus.domain.LinearSegment
import com.nurflugel.sisyphus.domain.Point
import com.nurflugel.sisyphus.domain.Shape


/**  This class makes a shape that spans the table and looks like this (the dot is the table center):
 *                         |
 *    * ---- *             |
 *     \ . /               |
 *      \/                 |
 *      *
 *
 */
class Triangle(
    numberOfCopiesPerRev: Int = 2,
    rhoRemainingPerRev: Double = 1 - (.03 / 3),
    numberOfRevs: Int = 100,
    offsetPerRevInDegrees: Int = 3,
    fileName: String = "spiralTriangleDeleteme.thr"
              ) : Shape(
    numberOfCopiesPerRev = numberOfCopiesPerRev,
    rhoRemainingPerRev = rhoRemainingPerRev,
    numberOfRevs = numberOfRevs,
    offsetPerRevInDegrees = offsetPerRevInDegrees,
    fileName = fileName,
    numberOfSubSegments = 20
                       ) {

    override fun defineSegments() { // hmm, pass in delta rho and theta here adn you've got a constructor...
        val point1 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 0.0)
        val point2 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 120.0)
        val point3 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 240.0)
        val point4 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 0.0, numberOfTurns = 1
                                       ) // notice for this shape we must end at 360, not 0 (wrong direction)

        // this makes 2 segments
        segments.addAll(mutableListOf(
            LinearSegment(point1, point2, numberOfSubSegments),
            LinearSegment(point2, point3, numberOfSubSegments),
            LinearSegment(point3, point4, numberOfSubSegments)
                                     )
                       )

    }

    override fun withOffset(deltaRho: Double, deltaTheta: Double, iteration: Int): Shape {
        return Triangle(numberOfCopiesPerRev,
                        rhoRemainingPerRev,
                        numberOfRevs,
                        offsetPerRevInDegrees,
                        fileName,
                        segments
                            .map { s -> s.withOffset(deltaRho, deltaTheta, iteration) }
                            .toMutableList())
    }

    constructor(numberOfCopiesPerRev: Int,
                rhoRemainingPerRev: Double,
                numberOfRevs: Int,
                offsetPerRevInDegrees: Int,
                fileName: String,
                segments: MutableList<LinearSegment>
               ) : this(
        numberOfCopiesPerRev, rhoRemainingPerRev, numberOfRevs, offsetPerRevInDegrees, fileName
                       ) {
        this.segments.addAll(segments)
    }
}


