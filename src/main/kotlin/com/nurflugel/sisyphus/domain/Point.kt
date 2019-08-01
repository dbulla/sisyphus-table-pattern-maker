package com.nurflugel.sisyphus.domain

import java.lang.Math.PI

/**
 * Constructor for points in a Cartesian OR Polar coordinate system.  Named parameters
 * make this easier than you'd think.
 */
class Point
private constructor(var x: Double, var y: Double, var rho: Double, private var theta: Double, var numberOfTurns: Int = 0) {

    /** for all intents and purposes, rho is zero */
    fun isRhoPracticallyZero() = rho < RHO_PRACTICALLY_ZERO


    /** for all intents and purposes, rho is zero */
    fun isRhoPracticallyOne(): Boolean {
        return rho > PRACTICALLY_ONE
    }


    fun isEqual(previousPoint: Point?): Boolean {
        //    println("is ${this.rho}:${this.theta} = ${previousPoint?.rho}:${previousPoint?.theta})?  $result")
        return when {
            previousPoint == null                                                        -> false
            Math.abs(previousPoint.rho - rho) > RHO_PRACTICALLY_ZERO                     -> false
            Math.abs(previousPoint.theta - theta) > THETA_PRACTICALLY_ZERO               -> false
            // if both rhos are zero, they're equal, regardless of theta 
            previousPoint.isRhoPracticallyZero() && previousPoint.isRhoPracticallyZero() -> true
            else                                                                         -> true
        }
    }

    override fun toString(): String {
        return "Point(theta=${thetaInDegrees()}), rho=$rho, x=$x, y=$y, numTurns=$numberOfTurns"
    }

    fun thetaInDegrees(): Double {
        return (theta * 180 / PI) + numberOfTurns * 360
    }

    fun thetaInRads(): Double {
        return theta + 2 * PI * numberOfTurns
    }

    fun thetaNoTurns(): Double {
        return theta
    }

    companion object {
        fun pointFromRad(rho: Double, thetaInRads: Double, numberOfTurns: Int = 0): Point {
            return Point(x = rho * Math.cos(thetaInRads),
                         y = rho * Math.sin(thetaInRads),
                         rho = rho,
                         theta = thetaInRads,
                         numberOfTurns = numberOfTurns
                        )
        }

        fun pointFromDeg(rho: Double, thetaInDegrees: Double, numberOfTurns: Int = 0): Point {
            return pointFromRad(rho = rho,
                                thetaInRads = thetaInDegrees / 180 * PI + numberOfTurns * 2 * PI,
                                numberOfTurns = numberOfTurns
                               )
        }

        fun pointFromXY(x: Double, y: Double, numberOfTurns: Int = 0): Point {
            val theta = Math.atan2(y, x)
            val realTheta = when {
                // if theta is negative, and x and y are also negative, then add 2 pi to get us positive again. IRRITATING!
                //            x < 0 && y<0 && theta<0 -> theta + 2 * PI
                y < 0 && theta < 0 -> theta + 2 * PI
                else               -> theta
            }

            return Point(x = x,
                         y = y,
                         rho = Math.sqrt(x * x + y * y),
                         theta = realTheta,
                         numberOfTurns = numberOfTurns
                        )
        }

        /** for all intents and purposes, rho is zero */
        fun isRhoPracticallyZero(rho: Double): Boolean {
            return rho < RHO_PRACTICALLY_ZERO
        }

        /** for all intents and purposes, rho is zero */
        fun isRhoPracticallyOne(rho: Double): Boolean {
            return rho > PRACTICALLY_ONE
        }

        /** The minimum threshold to be considered zero. */
        const val THETA_PRACTICALLY_ZERO = .0001
        const val RHO_PRACTICALLY_ZERO = .001
        const val PRACTICALLY_ONE = 1.0 - RHO_PRACTICALLY_ZERO
    }
}
