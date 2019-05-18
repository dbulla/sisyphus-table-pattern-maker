package com.nurflugel.sisyphus.domain

import java.lang.Math.PI

/**
 * A bunch of linear segments in a group.  Not used in anything yet...
 */
class Polygon(val points: List<Point>,
              numberOfCopiesPerRev: Int,
              rhoRemainingPerRev: Double,
              numberOfRevs: Int,
              offsetPerRevInDegrees: Int
             ) : Shape(numberOfCopiesPerRev = numberOfCopiesPerRev,
                       rhoRemainingPerRev = rhoRemainingPerRev,
                       numberOfRevs = numberOfRevs,
                       offsetPerRevInDegrees = offsetPerRevInDegrees,
                       fileName = "polygon",
                       numberOfSubSegments = 1
                      ) {
    override fun defineSegments() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun withOffset(deltaRho: Double, deltaTheta: Double, it: Int): Shape {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getdfdSegments(): List<LinearSegment> {
        segments.addAll((0 until points.size)
                            .map { it -> buildSegment(points[it], points[it + 1]) }
                            .toMutableList())
        // now deal with the last and the first
        segments.add(buildSegment(points[points.size - 1], points[0]))
        return segments
    }

    private fun buildSegment(point1: Point, point2: Point): LinearSegment {
        val deltaTheta = point2.thetaNoTurns() - point1.thetaNoTurns()
        // say 180 deg = 100 div.  This is a COMPLETE fudge factor!
        val numberOfSegments = deltaTheta / PI * 100
        return LinearSegment(point1, point2, numberOfSegments.toInt())
    }

}