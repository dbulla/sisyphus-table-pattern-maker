package com.nurflugel.sisyphus


/**  This class makes a shape that looks like this:
 *
 *    *      *
 *     \   /
 *      \/
 *      *
 *
 */
class TriangleSawtooth(
    //    numberOfCopiesPerRev: Int = 18,
    numberOfCopiesPerRev: Int = 30,
    rhoRemainingPerRev: Double = 1 - (.05 / 3),
    //    rhoRemainingPerRev: Double = 1.0,
    numberOfRevs: Int = 150,
    //    numberOfRevs: Int = 1,
    offsetPerRevInDegrees: Int = 5,
    //    offsetPerRevInDegrees: Int = 0,
    fileName: String = "dougsTriangleSawtooth2_1.thr"
                      ) : Shape(
    numberOfCopiesPerRev = numberOfCopiesPerRev,
    rhoRemainingPerRev = rhoRemainingPerRev,
    numberOfRevs = numberOfRevs,
    offsetPerRevInDegrees = offsetPerRevInDegrees,
    fileName = fileName,
    numberOfSubSegments = 1
                               ) {


    override fun defineSegments() { // hmm, pass in delta rho and theta here adn you've got a constructor...
        val deltaThetaInDegrees = 360.0 / numberOfCopiesPerRev
        val point1 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = 0.0)
        val point2 = Point.pointFromDeg(rho = 0.95, thetaInDegrees = deltaThetaInDegrees / 2)
        val point3 = Point.pointFromDeg(rho = 1.0, thetaInDegrees = deltaThetaInDegrees)

        // this makes 2 segments
        segments.addAll(mutableListOf(LinearSegment(point1, point2, 1),
                                      LinearSegment(point2, point3, 1)
                                     )
                       )

    }

}