package com.nurflugel.sisyphus.shapes

import com.nurflugel.sisyphus.domain.LinearSegment
import com.nurflugel.sisyphus.domain.Point
import com.nurflugel.sisyphus.domain.Shape


/**  This class makes a shape that looks like this:
 *
 *    * ---- *
 *    |  .   |
 *    |      }
 *    * ---- *
 *
 */
class Square(
    //    numberOfCopiesPerRev: Int = 18,
    numberOfCopiesPerRev: Int = 2,
    rhoRemainingPerRev: Double = 1 - (.02 / 3),
    //    rhoRemainingPerRev: Double = 1.0,
    numberOfRevs: Int = 150,
    //    numberOfRevs: Int = 1,
    offsetPerRevInDegrees: Int = 10,
    //    offsetPerRevInDegrees: Int = 0,
    fileName: String = "dougsSquare_.thr"
            ) : Shape(
    numberOfCopiesPerRev = numberOfCopiesPerRev,
    rhoRemainingPerRev = rhoRemainingPerRev,
    numberOfRevs = numberOfRevs,
    offsetPerRevInDegrees = offsetPerRevInDegrees,
    fileName = fileName,
    numberOfSubSegments = 4
                     ) {

    override fun defineSegments() { // hmm, pass in delta rho and theta here adn you've got a constructor...
        val point0 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 45.0)
        val point1 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 135.0)
        val point2 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 225.0)
        val point3 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 315.0
                                       ) // notice for this shape we must to to staring point as a positive rotation
        val point4 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 405.0
                                       ) // so we go past 360 and don't go back to 0

        // this makes 2 segments
        segments.addAll(
            mutableListOf(
                LinearSegment(point0, point1, numberOfSubSegments),
                LinearSegment(point1, point2, numberOfSubSegments),
                LinearSegment(point2, point3, numberOfSubSegments),
                LinearSegment(point3, point4, numberOfSubSegments)
                         )
                       )
    }

    override fun withOffset(deltaRho: Double, deltaTheta: Double, iteration: Int): Shape {
        return Square(numberOfCopiesPerRev,
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


