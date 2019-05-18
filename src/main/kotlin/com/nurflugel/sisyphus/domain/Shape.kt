package com.nurflugel.sisyphus.domain

import com.nurflugel.sisyphus.shapes.SharpSawtooth

/**
 * Representation of an abstract shape which will be copied and rotated over and over
 */
abstract class Shape(
    /**  how many copies around the circle? */
    val numberOfCopiesPerRev: Int = 36,
    /** for each run around the circle, how much will rho shrink (0-1)*/
    val rhoRemainingPerRev: Double = 1.0,
    /** for each run around the circle, how many degrees will the next run be offset?*/
    val offsetPerRevInDegrees: Int = 0,
    /**  how many times around the circle? */
    val numberOfRevs: Int = 100,
    /** The 1-to-many segments that make up this shape */
    val segments: MutableList<LinearSegment> = mutableListOf(),
    /** the file name */
    val fileName: String,
    val numberOfSubSegments: Int
                    ) {

    abstract fun defineSegments()


    //todo wtf????
    open fun withOffset(deltaRho: Double, deltaTheta: Double, iteration: Int): Shape {
        return SharpSawtooth(numberOfCopiesPerRev,
                             rhoRemainingPerRev,
                             numberOfRevs,
                             offsetPerRevInDegrees,
                             fileName,
                             segments.map { s -> s.withOffset(deltaRho, deltaTheta, iteration) }
                                 .toMutableList())
    }

    override fun toString(): String {
        return "Shape(segments=$segments)"
    }

    /** Add a description of the settings used to generate this file */
    fun addDescriptionLines(lines: MutableList<String>) {
        val className = this.javaClass.name
        val parameters: List<Pair<String, String>> = getParameterDescriptions()
        parameters.reversed().forEach { lines.add(0, "// ${it.first} = ${it.second}") }
        lines.add(0, "// Template class: $className")
    }

    private fun getParameterDescriptions(): List<Pair<String, String>> {
        return listOf(
            Pair("numberOfCopiesPerRev", numberOfCopiesPerRev.toString()),
            Pair("rhoRemainingPerRev", rhoRemainingPerRev.toString()),
            Pair("numberOfRevs", numberOfRevs.toString()),
            Pair("numberOfSubSegments", numberOfSubSegments.toString()),
            Pair("offsetPerRevInDegrees", offsetPerRevInDegrees.toString()),
            Pair("fileName", fileName)
                     )
    }
}

