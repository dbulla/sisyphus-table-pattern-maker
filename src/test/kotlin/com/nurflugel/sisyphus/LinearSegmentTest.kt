package com.nurflugel.sisyphus

import com.nurflugel.sisyphus.domain.LinearSegment
import com.nurflugel.sisyphus.domain.Point
import com.nurflugel.sisyphus.domain.Point.Companion.RHO_PRACTICALLY_ZERO
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe

const val expected1 = 0.8660254037844387

class LinearSegmentTest : StringSpec(
    {
        "spitting into sub segments should work - horizontal line" {
            val segment = LinearSegment(Point.pointFromRad(0.0, 0.0),
                                        Point.pointFromRad(1.0, 0.0), 2
                                       )
            val subSegments: List<LinearSegment> = segment.generateSubSegments()
            subSegments[0].startPoint.rho shouldBe Double.NaN
            subSegments[0].endPoint.rho shouldBe Double.NaN
            subSegments[1].startPoint.rho shouldBe Double.NaN
            subSegments[1].endPoint.rho shouldBe 1.0
        }


        "spitting into sub segments should work - offset horizontal line" {
            val segment = LinearSegment(Point.pointFromXY(0.0, 0.25),
                                        Point.pointFromXY(0.5, 0.25), 2
                                       )
            val subSegments: List<LinearSegment> = segment.generateSubSegments()
            isCloseEnough(subSegments[0].startPoint.x, 0.0)
            isCloseEnough(subSegments[0].endPoint.x, 0.25)
            isCloseEnough(subSegments[1].startPoint.x, 0.25)
            isCloseEnough(subSegments[1].endPoint.x, 0.55)
            isCloseEnough(subSegments[0].startPoint.y, 0.25)
            isCloseEnough(subSegments[0].endPoint.y, 0.25)
            isCloseEnough(subSegments[1].startPoint.y, 0.25)
            isCloseEnough(subSegments[1].endPoint.y, 0.25)
        }

        "spitting into sub segments should work - SLOPED line" {
            val segment = LinearSegment(Point.pointFromDeg(1.0, 90.0),
                                        Point.pointFromRad(1.0, 0.0), 2
                                       )
            val subSegments: List<LinearSegment> = segment.generateSubSegments()
            subSegments[0].startPoint.x shouldBeLessThan RHO_PRACTICALLY_ZERO
            subSegments[0].startPoint.y shouldBe 1.0

            subSegments[0].endPoint.x shouldBe 0.5
            subSegments[0].endPoint.y - 0.5 shouldBeLessThan .0001

            subSegments[1].startPoint.x - 0.5 shouldBeLessThan .0001
            subSegments[1].startPoint.y - 0.5 shouldBeLessThan .0001

            subSegments[1].endPoint.x - 1.0 shouldBeLessThan .0001
            subSegments[1].endPoint.y - 1.0 shouldBeLessThan .0001
        }

        "spitting into sub segments should work - Vertical line w/2 subseg" {
            val segment = LinearSegment(Point.pointFromDeg(1.0, 120.0),
                                        Point.pointFromDeg(1.0, 240.0), 2
                                       )
            val subSegments: List<LinearSegment> = segment.generateSubSegments()

            subSegments.size shouldBe 2

            isCloseEnough(subSegments[0].startPoint.x, - 0.5)
            isCloseEnough(subSegments[0].startPoint.y, expected1)
            isCloseEnough(subSegments[0].endPoint.x, - 0.5)
            isCloseEnough(subSegments[0].endPoint.y, 0.0)

            isCloseEnough(subSegments[1].startPoint.x, - 0.5)
            isCloseEnough(subSegments[1].startPoint.y, 0.0)
            isCloseEnough(subSegments[1].endPoint.x, - 0.5)
            isCloseEnough(subSegments[1].endPoint.y, - expected1)

        }

        "spitting into sub segments should work - Vertical line w/3 subseg" {
            val segment = LinearSegment(Point.pointFromDeg(1.0, 120.0),
                                        Point.pointFromDeg(1.0, 240.0), 3
                                       )
            val subSegments: List<LinearSegment> = segment.generateSubSegments()

            subSegments.size shouldBe 3

            isCloseEnough(subSegments[0].startPoint.x, - 0.5)
            isCloseEnough(subSegments[0].startPoint.y, expected1)
            isCloseEnough(subSegments[0].endPoint.x, - 0.5)
            isCloseEnough(subSegments[0].endPoint.y, 0.288675134594813)

            isCloseEnough(subSegments[1].startPoint.x, - 0.5)
            isCloseEnough(subSegments[1].startPoint.y, 0.288675134594813)
            isCloseEnough(subSegments[1].endPoint.x, - 0.5)
            isCloseEnough(subSegments[1].endPoint.y, - 0.288675134594813)

            isCloseEnough(subSegments[2].startPoint.x, - 0.5)
            isCloseEnough(subSegments[2].startPoint.y, - 0.288675134594813)
            isCloseEnough(subSegments[2].endPoint.x, - 0.5)
            isCloseEnough(subSegments[2].endPoint.y, - expected1)

        }
    }
                                    )

private fun isCloseEnough(actual: Double, expected: Double) {
    println("actual = $actual, expected = $expected")
    Math.abs(actual - expected) shouldBeLessThan RHO_PRACTICALLY_ZERO
}