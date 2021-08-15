package com.nurflugel.sisyphus.gui

import com.nurflugel.sisyphus.sunbursts.ClockworkWigglerGenerator
import com.nurflugel.sisyphus.sunbursts.ClockworkWigglerGenerator.Companion.fileName
import org.apache.commons.io.FileUtils
import java.awt.Color
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
import java.io.IOException

import javax.imageio.ImageIO
import java.awt.image.BufferedImage


class GuiController(private val lines: MutableList<String>, fileName: String) {

    private val frame = JFrame()
    private var guiPanel = JPanel()

    companion object {
        const val imagesDir = "images2"
        const val tracksDir = "tracks2"

        @JvmStatic
        fun main(args: Array<String>) {
            val filePath = if (args.isNotEmpty() && args.first().startsWith("thrFile=")) args.last().substringAfter("thrFile=")
            //            else "/Users/douglas_bullard//Documents/JavaStuff/github/douglasBullard/sisyphus-table-pattern-maker/deltaDemo.thr"
            //            else "/Users/douglas_bullard/Downloads/Sisyphus Tracks/Schmigneous/focus.thr"
            else "/Users/douglas_bullard/Downloads/Sisyphus Tracks/crsolomon/1551055361-sun-moon.thr"
            println("filePath = $filePath")
            val lines = FileUtils.readLines(File(filePath))
            val plotterGui = GuiController(lines, filePath)
            plotterGui.showPreview(ClockworkWigglerGenerator.fileName)
        }

        const val maxDeltaTheta = 1.0 / 180.0 * PI // one degree max theta
    }

    private fun initialize(filename: String) {
        frame.title = "$fileName                    Click any key to close"
        frame.contentPane = guiPanel
        frame.defaultCloseOperation = EXIT_ON_CLOSE
        frame.preferredSize = Dimension(1200, 1200)
        frame.pack()
        frame.isVisible = true
        frame.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                //                System.exit(0)
                frame.isVisible = false
            }
        })
        //        frame.de
    }

    //    public fun close(){
    //        frame.
    //    }

    private fun getGraphicsContext() = guiPanel

    fun showPreview(fileName: String) {
        initialize(fileName)
        // initial point of null
        // go through lines, read new current point  if not a comment/empty
        // draw line from previous point to this point
        // current point becomes previous point

        val graphicsContext = getGraphicsContext()
        val graphics2D = graphicsContext.graphics as Graphics2D
        val scaleFactor = graphicsContext.size.height / 2 // 2 * rho=1 gives two 
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
            .filter { isUsable(it) }
            .toList()


        val bImg = BufferedImage(guiPanel.width, guiPanel.height, BufferedImage.TYPE_INT_RGB)
        val cg = bImg.createGraphics()
        guiPanel.paintAll(cg)

        for (currentPoint in pairs) {
            plot(previousPoint, currentPoint, graphics2D)
            printPlot(previousPoint, currentPoint, cg)
            previousPoint = currentPoint
        }
        try {
            val imageFileName = File("./$imagesDir/${fileName.replace(".thr", ".png")}")
            println("imageFileName = $imageFileName")
            if (ImageIO.write(bImg, "png", imageFileName)) {
                println("-- saved")
            }
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        //        guiPanel.isVisible=false
        guiPanel.parent.isVisible = false
        frame.isVisible = false
        frame.dispose()
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
    private fun plot(
        possiblePreviousPoint: Pair<Double, Double>?,
        currentPoint: Pair<Double, Double>,
        graphics: Graphics2D
                    ) {
        val previousPoint = when {
            isUsable(possiblePreviousPoint) -> possiblePreviousPoint !!
            else                            -> currentPoint
        }
        val line = Line2D.Double(previousPoint.first, previousPoint.second, currentPoint.first, currentPoint.second)
        graphics.color = Color.BLACK
        graphics.draw(line)
    }

    private fun printPlot(
        possiblePreviousPoint: Pair<Double, Double>?,
        currentPoint: Pair<Double, Double>,
        graphics: Graphics2D
                         ) {
        val previousPoint = when {
            isUsable(possiblePreviousPoint) -> possiblePreviousPoint !!
            else                            -> currentPoint
        }
        val line = Line2D.Double(previousPoint.first, previousPoint.second, currentPoint.first, currentPoint.second)
        graphics.color = Color.BLACK
        graphics.draw(line)
    }

    /** Is this a usable point?  */
    private fun isUsable(pair: Pair<Double, Double>?): Boolean {

        return when {
            pair == null                              -> false
            pair.first.isNaN() || pair.second.isNaN() -> false
            else                                      -> true
        }
    }
}