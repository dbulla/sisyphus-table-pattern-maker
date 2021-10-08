package com.nurflugel.sisyphus.gui

import java.awt.RenderingHints
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class GuiUtils {
    companion object {
        val aliasedRenderingHints = run {
            val hints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            hints[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY;
            hints[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_OFF;
            hints;
        }
        val nonAliasedRenderingHints = run {
            val hints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
            hints[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY
            hints
        }
        const val imagesDir = "images4"
        const val tracksDir = "tracks4"
        const val maxDeltaTheta = 1.0 / 180.0 * PI // one degree max theta

        /** This function takes the raw rho/theta pairs, cleans them up, removes
         * any comments, then "expands" to handle converting lines between two into a series of rho/theta arcs which approximates that line.
         *
         * Finally, converts them into graphics coordinates suitable for plotting/printing.
         */
        public fun createGraphicsCoordinates(
                lines: MutableList<String>,
                scaleFactorY: Int, // how much to scale by.  Assumption is that scaleFactorY==scaleFactorX (i.e., equal scaling)
                offsetX: Int,
                offsetY: Int,
                                            ): List<Pair<Double, Double>> {
            val polarPairs: List<Pair<Double, Double>> = lines
                .asSequence()
                .filter { it.isNotBlank() }
                .map { it.trim() }
                .filter { ! it.startsWith("//") }
                .filter { ! it.startsWith("#") }
                .map { convertLineToPair(it) }
                .filter { it != null }
                .map { it !! }
                .toList()

            val expandedPolarPairs = handleDeltaTheta(polarPairs)

            val pairs = expandedPolarPairs
                // here is where we need to take the pairs of pairs, and deal with delta thetas
                .map { convertToXy(it) }
                .map { convertToScreenCoords(it, scaleFactorY, offsetX, offsetY) }
                .filter { isUsable(it) }
                .toList()
            return pairs
        }

        /**
         * for every pair and the next pair, see if the delta theta is large enough (> 1 degree or so)
         * to need to subdivide that pair of pairs into a list of pairs.
         *
         * This "expands" the points, converting lines between two points into a series of rho/theta arcs which approximates that line.
         */
        private fun handleDeltaTheta(polarPairs: List<Pair<Double, Double>>): List<Pair<Double, Double>> {
            val expandedPairs: MutableList<Pair<Double, Double>> = mutableListOf()

            (0 until polarPairs.size - 1).forEach {
                val here = polarPairs[it]
                val there = polarPairs[it + 1]
                val deltaTheta = there.first - here.first
                val absoluteDeltaTheta = Math.abs(deltaTheta)
                if (absoluteDeltaTheta > maxDeltaTheta) { // we need to transition theta and rho evenly between here and there
                    val expandedList: MutableList<Pair<Double, Double>> = mutableListOf()
                    // find the closest number of iterations so each delta theta approximates maxDeltaTheta
                    val sss = absoluteDeltaTheta / maxDeltaTheta
                    val toInt = sss.toInt()
                    val numberOfSplits: Int = toInt + 1
                    val newDeltaTheta = deltaTheta / numberOfSplits
                    val newDeltaRho = (there.second - here.second) / numberOfSplits
                    (0 until numberOfSplits).forEach { index ->
                        val subTheta = here.first + (index * newDeltaTheta)
                        val subRho = here.second + (index * newDeltaRho)
                        expandedList.add(Pair(subTheta, subRho))
                    }
                    expandedPairs.addAll(expandedList)
                } else { // no need to expand
                    expandedPairs.add(here)
                }
            }
            expandedPairs.add(polarPairs.last())
            return expandedPairs
        }

        /** Convert the theta/rho coordinates to XY coordinates */
        private fun convertToXy(line: Pair<Double, Double>): Pair<Double, Double> {
            val theta = line.first
            val rho = line.second
            val x = rho * cos(theta)
            val y = rho * sin(theta)
            return Pair(x, y)
        }

        /** Convert the XY coordinates to screen coordinates - deal with scaling and offsets*/
        private fun convertToScreenCoords(xy: Pair<Double, Double>, scaleFactor: Int, offsetX: Int, offsetY: Int): Pair<Double, Double> {
            return Pair(xy.first * scaleFactor + offsetX, xy.second * scaleFactor + offsetY)
        }

        private fun convertLineToPair(line: String): Pair<Double, Double>? {
            val tokens = line.split(" ", "\t")
            //            val theta = tokens[0].toDoubleOrNull()
            //            val rho = tokens.last().toDoubleOrNull()
            val theta = tokens[0].toDouble()
            val rho = tokens.last().toDouble()
            //            if (rho != null && theta != null) return Pair(theta, rho)
            return Pair(theta, rho)
            //            return null
        }

        /** Is this a usable point?  */
        internal fun isUsable(pair: Pair<Double, Double>?): Boolean {
            return when {
                pair == null                              -> false
                pair.first.isNaN() || pair.second.isNaN() -> false
                else                                      -> true
            }
        }
    }
}