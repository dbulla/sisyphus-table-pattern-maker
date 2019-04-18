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
import kotlin.math.cos
import kotlin.math.sin

class GuiController(private val lines: MutableList<String>, fileName: String) {

    private var guiPanel = JPanel()

    init {
        val frame = JFrame(fileName)
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

        val pairs = lines
            .asSequence()
            .filter { it.isNotBlank() }
            .map { it.trim() }
            .filter { ! it.startsWith("//") }
            .filter { ! it.startsWith("#") }
            .map { convertLineToPair(it) }
            .filter { it != null }
            .map { it !! }
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

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val filePath = if (args.isNotEmpty() && args.first().startsWith("thrFile=")) args.last().substringAfter("thrFile=")
            else "/Users/douglas_bullard/Downloads/Sisyphus Tracks/sisyphus-master/thr_paths/dither_hypnogrid.thr"
            println("filePath = $filePath")
            val lines = FileUtils.readLines(File(filePath))
            val plotterGui = GuiController(lines, filePath)
            plotterGui.showGui()
        }

    }
}