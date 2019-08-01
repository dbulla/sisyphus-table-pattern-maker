package com.nurflugel.sisyphus.gui

import org.apache.commons.io.FileUtils
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.geom.Line2D
import java.io.File
import javax.swing.JFrame
import javax.swing.JFrame.EXIT_ON_CLOSE
import javax.swing.JPanel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class GuiController(private val lines: MutableList<String>, fileName: String) {

    private var guiPanel = JPanel()

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val filePath = if (args.isNotEmpty() && args.first().startsWith("thrFile=")) args.last().substringAfter("thrFile=")
            //            else "/Users/douglas_bullard//Documents/JavaStuff/github/douglasBullard/sisyphus-table-pattern-maker/deltaDemo.thr"
            //            else "/Users/douglas_bullard/Downloads/Sisyphus Tracks/Schmigneous/focus.thr"
            else "/Users/douglas_bullard/Downloads/Sisyphus Tracks/crsolomon/1551055361-sun-moon.thr"
            println("filePath = $filePath")
            val lines = FileUtils.readLines(File(filePath))
            val plotterGui = GuiController(lines, filePath)
            plotterGui.showGui()
        }

        const val maxDeltaTheta = 1.0 / 180.0 * PI // one degree max theta
    }

    init {
        val frame = JFrame("$fileName                    Click any key to close")
        frame.contentPane = guiPanel
        frame.defaultCloseOperation = EXIT_ON_CLOSE
        frame.preferredSize = Dimension(1200, 1200)
        frame.pack()
        frame.isVisible = true
        frame.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                System.exit(0)
            }
        })
    }

    private fun getGraphicsContext() = guiPanel

    fun showGui() {
        // initial point of null
        // go through lines, read new current point  if not a comment/empty
        // draw line from previous point to this point
        // current point becomes previous point

        val scaleFactor = getGraphicsContext().size.height / 2 // 2 * rho=1 gives two 
        val offset = scaleFactor

        var previousPoint: Pair<Double, Double>? = null

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
            .map { convertToScreenCoords(it, scaleFactor, offset) }
            .toList()
        for (currentPoint in pairs) {
            if (previousPoint != null) {
                plot(previousPoint, currentPoint)
            }
            previousPoint = currentPoint
        }
    }

    /**
     * for every pair and the next pair, see if the delta theta is large enough (> 1 degree or so)
     * to need to subdivide that pair of pairs into a list of pairs
     */
    private fun handleDeltaTheta(polarPairs: List<Pair<Double, Double>>): List<Pair<Double, Double>> {
        val expandedPairs: MutableList<Pair<Double, Double>> = mutableListOf()

        (0 until polarPairs.size - 1).forEach {
            val here = polarPairs[it]
            val there = polarPairs[it + 1]
            val deltaTheta = there.first - here.first
            val absoluteDeltaTheta = Math.abs(deltaTheta)
            if (absoluteDeltaTheta > Companion.maxDeltaTheta) { // we need to transition theta and rho evenly between here and there
                val expandedList: MutableList<Pair<Double, Double>> = mutableListOf()
                // find the closest number of iterations so each delta theta approximates maxDeltaTheta
                val sss = absoluteDeltaTheta / Companion.maxDeltaTheta
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
    private fun convertToScreenCoords(xy: Pair<Double, Double>, scaleFactor: Int, offset: Int): Pair<Double, Double> {
        return Pair(xy.first * scaleFactor + offset, xy.second * scaleFactor + offset)

    }

    private fun convertLineToPair(line: String): Pair<Double, Double>? {
        val tokens = line.split(" ", "\t")
        val theta = tokens[0].toDoubleOrNull()
        val rho = tokens.last().toDoubleOrNull()
        if (rho != null && theta != null) return Pair(theta, rho)
        return null
    }

    /** Draw a line in the context between the two points */
    private fun plot(previousPoint: Pair<Double, Double>, currentPoint: Pair<Double, Double>) {
        val g = getGraphicsContext().graphics
        val g2 = g as Graphics2D
        val line = Line2D.Double(previousPoint.first, previousPoint.second, currentPoint.first, currentPoint.second)
        g2.draw(line)
    }
}