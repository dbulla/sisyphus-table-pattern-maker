package com.nurflugel.sisyphus.gui

import org.apache.commons.io.FileUtils
import java.awt.*
import java.awt.Color.BLACK
import java.awt.Color.GRAY
import java.awt.Font.PLAIN
import java.awt.RenderingHints.*
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JFrame.EXIT_ON_CLOSE
import javax.swing.JPanel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.nio.charset.Charset

private const val WIDTH = 1000
private const val SHUT_DOWN_WITH_KEY_PRESS = true
private const val SLOW_GUI_DRAW = false

class GuiController {

    private val frame = JFrame()
    private var guiPanel = JPanel()

    companion object {
        const val imagesDir = "images3"
        const val tracksDir = "tracks3"
        const val maxDeltaTheta = 1.0 / 180.0 * PI // one degree max theta

        // run this to display an existing .thr track
        @JvmStatic
        fun main(args: Array<String>) {
            val filePath = if (args.isNotEmpty() && args.first().startsWith("thrFile=")) args.last().substringAfter("thrFile=")
            //            else "/Users/douglas_bullard//Documents/JavaStuff/github/douglasBullard/sisyphus-table-pattern-maker/deltaDemo.thr"
            //            else "/Users/douglas_bullard/Downloads/Sisyphus Tracks/Schmigneous/focus.thr"
            else "/Users/douglas_bullard/Downloads/Sisyphus Tracks/crsolomon/1551055361-sun-moon.thr"
            println("filePath = $filePath")
            val lines = FileUtils.readLines(File(filePath), "UTF-8")
            val plotterGui = GuiController()
            plotterGui.showPreview(filePath, lines, false)
        }
    }

    internal fun initialize() {
        frame.title = "Click any key to close"
        frame.contentPane = guiPanel
        frame.defaultCloseOperation = EXIT_ON_CLOSE
        frame.preferredSize = Dimension(WIDTH, WIDTH)
        frame.pack()
        frame.isVisible = true

        if (SHUT_DOWN_WITH_KEY_PRESS) {
            frame.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent?) {
                    shutDown()
                }
            })
        }
    }

    internal fun shutDown() {
        guiPanel.parent.isVisible = false
        frame.isVisible = false
        frame.dispose()
        //        System.exit(0) // this will kill the app completely
    }

    private fun getGraphicsContext() = guiPanel

    fun showPreview(fileName: String, lines: MutableList<String>, saveImages: Boolean) {
        // initial point of null
        // go through lines, read new current point  if not a comment/empty
        // draw line from previous point to this point
        // current point becomes previous point
        frame.title = "$fileName     Click any key to close"

        val graphicsContext = getGraphicsContext()
        val graphics2D = graphicsContext.graphics as Graphics2D
        graphics2D.clearRect(0, 0, graphicsContext.width, graphicsContext.height)
        val scaleFactor = graphicsContext.size.height / 2 // 2 * rho=1 gives two 
        val offset = scaleFactor

        val renderingHints = RenderingHints(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
        renderingHints[KEY_RENDERING] = VALUE_RENDER_QUALITY;

        graphics2D.setRenderingHints(renderingHints)
        graphics2D.font = Font("Helvetica", PLAIN, 13)

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


        val bImg = BufferedImage(guiPanel.width, guiPanel.height, TYPE_INT_RGB)
        val cg = bImg.createGraphics()
        guiPanel.paintAll(cg)

        for (currentPoint in pairs) {
            plot(previousPoint, currentPoint, graphics2D, fileName)
            if (saveImages) {
                printPlot(previousPoint, currentPoint, cg, fileName)
            }
            previousPoint = currentPoint
        }
        try {
            if (saveImages) {
                val imageFileName = File("./$imagesDir/${fileName.replace(".thr", ".png")}")
                if (ImageIO.write(bImg, "png", imageFileName)) {
                    println("$imageFileName -- saved")
                }
            }
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        guiPanel.parent.isVisible = false
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
        graphics: Graphics2D,
        fileName: String
                    ) {
        val previousPoint = when {
            isUsable(possiblePreviousPoint) -> possiblePreviousPoint !!
            else                            -> currentPoint
        }
        val line = Line2D.Double(previousPoint.first, previousPoint.second, currentPoint.first, currentPoint.second)
        graphics.color = BLACK

        graphics.draw(line)
        if (SLOW_GUI_DRAW) {
            //        graphics.drawString(fileName, 20, 30);
            graphics.drawString("-", 20, 30);
        }
    }
}

private fun printPlot(
    possiblePreviousPoint: Pair<Double, Double>?,
    currentPoint: Pair<Double, Double>,
    graphics: Graphics2D,
    fileName: String
                     ) {
    val previousPoint = when {
        isUsable(possiblePreviousPoint) -> possiblePreviousPoint !!
        else                            -> currentPoint
    }
    val line = Line2D.Double(previousPoint.first, previousPoint.second, currentPoint.first, currentPoint.second)
    graphics.color = GRAY
    graphics.draw(line)
    if (SLOW_GUI_DRAW) {
        //        graphics.drawString(fileName, 20, 30);
        graphics.drawString("-", 20, 30);
    }
}

/** Is this a usable point?  */
private fun isUsable(pair: Pair<Double, Double>?): Boolean {

    return when {
        pair == null                              -> false
        pair.first.isNaN() || pair.second.isNaN() -> false
        else                                      -> true
    }
}
