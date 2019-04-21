package com.nurflugel.sisyphus.shapes

import com.nurflugel.sisyphus.domain.LinearSegment
import com.nurflugel.sisyphus.domain.Point
import com.nurflugel.sisyphus.domain.Shape


/**  This class makes a shape that looks like this:
 *
 *    *    *
 *    |  /
 *    |/
 *    *
 *
 *
 *
 *
 *
 *    .
 */
class SharpSawtooth(
    //        numberOfCopiesPerRev: Int = 18,
    //        rhoRemainingPerRev: Double = 1 - (.05 / 3),
    //        numberOfRevs: Int = 150,
    //        offsetPerRevInDegrees: Int = 5,
    numberOfCopiesPerRev: Int = 30,
    rhoRemainingPerRev: Double = 1 - (.08 / 3), // todo split this up??
    numberOfRevs: Int = 250,
    offsetPerRevInDegrees: Int = 3,
    fileName: String = "sharpSawtooth.thr"
                   ) : Shape(
    numberOfCopiesPerRev = numberOfCopiesPerRev,
    rhoRemainingPerRev = rhoRemainingPerRev,
    numberOfRevs = numberOfRevs,
    offsetPerRevInDegrees = offsetPerRevInDegrees,
    fileName = fileName,
    numberOfSubSegments = 1
                            ) {

    constructor(numberOfCopiesPerRev: Int,
                rhoRemainingPerRev: Double,
                numberOfRevs: Int,
                offsetPerRevInDegrees: Int,
                fileName: String,
                segments: MutableList<LinearSegment>
               ) : this(numberOfCopiesPerRev, rhoRemainingPerRev, numberOfRevs, offsetPerRevInDegrees, fileName) {
        this.segments.addAll(segments)
    }

    override fun defineSegments() { // hmm, pass in delta rho and theta here and you've got a constructor...
        val deltaThetaInDegrees = 360.0 / numberOfCopiesPerRev
        val point1 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 0.0)
        val point2 = Point.pointFromDeg(rho = 0.95, thetaInDegrees = 0.0)
        val point3 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = deltaThetaInDegrees)

        // this makes 2 segments, of only 1 sub-segment each
        segments.addAll(mutableListOf(LinearSegment(point1, point2, 1),
                                      LinearSegment(point2, point3, 1)
                                     )
                       )
    }
}