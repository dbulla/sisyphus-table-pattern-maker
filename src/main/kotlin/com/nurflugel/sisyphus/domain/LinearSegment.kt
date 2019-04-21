package com.nurflugel.sisyphus.domain

import java.lang.Math.cos
import java.lang.Math.sin


/** A LinearSegment is part of a Shape - think of one side of a rectangle.  It is one intrinsic, STRAIGHT stroke which can
 * (and must sometimes) be drawn differently than it is.
 *
 * For example, a line (represented by two points), must be drawn as a series of arcs between the points, since
 * Sisyphus is a rho/theta system.  Makes drawing circles REALLY easy, but makes lines much harder.
 *
 * Any shape can be represented by a number of linear segments.  When it comes time
 * to draw, we take each segment, and decompose it into a list of sub-segments.  A sub-segment is still a segment, but
 * but the numberOfSubSegments parameter is always 1.  Make enough sub-segments, and the series of arcs drawn will approximate the straight linear segment.
 *
 * In general, the greater the delta theta (like a flat line), then the greater number of sub-segments are needed
 * to draw it properly.
 *
 * In contrast, a circle/arc has fixed rho, and needs NO sub-segments in this system
 *
 */
data class LinearSegment(val startPoint: Point, val endPoint: Point, val numberOfSubSegments: Int) {

    /**
     * Decompose the LinearSegment into smaller sub-segments, each of which have no sub-segments.
     */
    fun generateSubSegments(): List<LinearSegment> {

        when (numberOfSubSegments) {
            1    -> return mutableListOf(this) // only 1 sub-segment?  Then we exit early and return this

            // in order to draw a "straight" line, we need to do this math with Cartesian coordinates (a straight line won't have a
            // constant rho
            else -> {
                val startTheta = startPoint.thetaNoTurns()
                val endTheta = endPoint.thetaNoTurns()
                val deltaTheta = (endTheta - startTheta) / numberOfSubSegments

                val slope = (endPoint.y - startPoint.y) / (endPoint.x - startPoint.x)
                val yIntercept = startPoint.y - slope * startPoint.x
                //todo special treatment for slope == infinity
                return (0 until numberOfSubSegments)
                    //            .map { it -> createSubSegment(it, deltaX, deltaY) }
                    .map { createSubSegmentFromTheta(i = it, deltaTheta = deltaTheta, slope = slope, yIntercept = yIntercept) }
                    .toList()

            }
        }
    }


    /**
     * Rather than subdivide via delta x and delta y, use delta theta.  This should get around the theta issues
     */
    private fun createSubSegmentFromTheta(i: Int, deltaTheta: Double, slope: Double, yIntercept: Double): LinearSegment {
        val startTheta = startPoint.thetaInRads() + i * deltaTheta
        val endTheta = startPoint.thetaInRads() + (i + 1) * deltaTheta

        // deal with those pesky vertical lines - in these special cases, x is fixed
        val startRho: Double
        val endRho: Double
        if (slope.isInfinite()) {
            startRho = startPoint.x / cos(startTheta)
            endRho = startPoint.x / cos(endTheta)
        } else {
            startRho = findRho(yIntercept, slope, startTheta)
            endRho = findRho(yIntercept, slope, endTheta)
        }
        val subStartPoint = Point.pointFromRad(startRho, startTheta)
        val subEndPoint = Point.pointFromRad(endRho, endTheta)

        return LinearSegment(subStartPoint, subEndPoint, 1)
    }

    private fun findRho(yIntercept: Double, slope: Double, theta: Double): Double {
        val divisor = slope * cos(theta) - sin(theta)
        val rho = - yIntercept / divisor

        return rho
    }

    /** make a copy of itself offset by the given amount for each point */
    fun withOffset(deltaRho: Double, deltaTheta: Double, iteration: Int): LinearSegment {
        val thetaOffset = deltaTheta * iteration
        val rhoShrinkage = deltaRho * iteration
        val newStartPoint = Point.pointFromRad(startPoint.rho - rhoShrinkage,
                                               startPoint.thetaNoTurns() + thetaOffset * 2
                                              )
        val newEndPoint = Point.pointFromRad(endPoint.rho - rhoShrinkage,
                                             endPoint.thetaNoTurns() + thetaOffset * 2
                                            )

        return LinearSegment(newStartPoint, newEndPoint, numberOfSubSegments)
    }


    /**
     * get the list of points for this segment
     * @param recursive if true, break the segment up into it's sub-segments.  If false, just this segment
     */
    fun points(recursive: Boolean): List<Point> {
        return when {
            recursive -> generateSubSegments().flatMap { listOf(startPoint, endPoint) }
            else      -> listOf(startPoint, endPoint)
        }
    }

}

