package com.nurflugel.sisyphus.gui

import com.nurflugel.sisyphus.gui.GuiUtils.Companion.aliasedRenderingHints
import com.nurflugel.sisyphus.gui.GuiUtils.Companion.isUsable
import com.nurflugel.sisyphus.gui.GuiUtils.Companion.maxDeltaTheta
import org.apache.commons.io.FileUtils
import java.awt.*
import java.awt.Color.BLACK
import java.awt.Color.GRAY
import java.awt.Font.PLAIN
import java.awt.RenderingHints.*
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import java.io.File
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
import java.awt.GraphicsEnvironment
import javax.swing.BorderFactory


//private const val WIDTH = 1920
//private const val HEIGHT = 1080 
private const val WIDTH = 2560
private const val HEIGHT = 1440

private const val SHUT_DOWN_WITH_KEY_PRESS = false

class GuiPreviewer {
    private val frame = JFrame()
    private var guiPanel = JPanel()

    companion object {
        // run this to display an existing .thr track
        @JvmStatic
        fun main(args: Array<String>) {
            val filePath = if (args.isNotEmpty() && args.first().startsWith("thrFile=")) args.last().substringAfter("thrFile=")
            //            else "/Users/douglas_bullard//Documents/JavaStuff/github/douglasBullard/sisyphus-table-pattern-maker/deltaDemo.thr"
            //            else "/Users/douglas_bullard/Downloads/Sisyphus Tracks/Schmigneous/focus.thr"
            else "/Users/douglas_bullard/Downloads/Sisyphus Tracks/crsolomon/1551055361-sun-moon.thr"
            println("filePath = $filePath")
            val lines = FileUtils.readLines(File(filePath), "UTF-8")
            val plotterGui = GuiPreviewer()
            plotterGui.showPreview(filePath, lines, false)
        }
    }

    internal fun initialize() {
        val gd = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        val width = gd.displayMode.width
        val height = gd.displayMode.height
        println("       width from device = $width")
        println("       height from device = $height")
        frame.title = "Click any key to close"
        frame.isUndecorated = true
        frame.rootPane.border = BorderFactory.createEmptyBorder();

        frame.contentPane = guiPanel
        frame.defaultCloseOperation = EXIT_ON_CLOSE
        frame.pack()
        frame.preferredSize = Dimension(WIDTH, HEIGHT) // todo need to add 45
        frame.minimumSize = Dimension(WIDTH, HEIGHT) // todo need to add 45
        frame.extendedState = JFrame.MAXIMIZED_BOTH;

        println("       width from frame = ${frame.size.width}")
        println("       height from frame = ${frame.size.height}")

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

    // todo split this up into showPreview and writeImage - clearer, easier to turn on/off what's desired
    fun showPreview(trackFileName: String, lines: MutableList<String>, showNames: Boolean) {
        // initial point of null
        // go through lines, read new current point  if not a comment/empty
        // draw line from previous point to this point
        // current point becomes previous point
        frame.isVisible = true
        frame.title = "$trackFileName     Click any key to close"
        // todo reset the values for scales based on the present GUI size
        val graphicsContext = getGraphicsContext()
        val graphics2D = graphicsContext.graphics as Graphics2D
        graphics2D.clearRect(0, 0, graphicsContext.width, graphicsContext.height)
        val scaleFactorY = graphicsContext.size.height / 2 // 2 * rho=1 gives two 
        val scaleFactorX = graphicsContext.size.width / 2 // 2 * rho=1 gives two 
        val offsetX = scaleFactorX * 1 // todo x and y
        val offsetY = scaleFactorY * 1 // todo x and y

        graphics2D.setRenderingHints(aliasedRenderingHints)
        graphics2D.font = Font("Helvetica", PLAIN, 13)

        var previousPoint: Pair<Double, Double>? = null

        val graphicsPairs = GuiUtils.createGraphicsCoordinates(lines, scaleFactorY, offsetX, offsetY)

        val bImg = BufferedImage(guiPanel.width, guiPanel.height, TYPE_INT_RGB)
        val cg = bImg.createGraphics()
        guiPanel.paintAll(cg)

        for (currentPoint in graphicsPairs) {
            drawUiPreview(previousPoint, currentPoint, graphics2D, trackFileName, showNames)
            previousPoint = currentPoint
        }
        guiPanel.parent.isVisible = false
    }

    // todo split this up into showPreview and writeImage - clearer, easier to turn on/off what's desired
    fun writeImage(imageFileName: String, lines: MutableList<String>, showNames: Boolean) {
        // initial point of null
        // go through lines, read new current point  if not a comment/empty
        // draw line from previous point to this point
        // current point becomes previous point
        val graphicsContext = getGraphicsContext()
        val graphics2D = graphicsContext.graphics as Graphics2D
        graphics2D.clearRect(0, 0, graphicsContext.width, graphicsContext.height)
        val scaleFactorY = graphicsContext.size.height / 2 // 2 * rho=1 gives two 
        val scaleFactorX = graphicsContext.size.width / 2 // 2 * rho=1 gives two 
        val offsetX = scaleFactorX * 1 // todo x and y
        val offsetY = scaleFactorY * 1 // todo x and y

        graphics2D.setRenderingHints(aliasedRenderingHints)
        graphics2D.font = Font("Helvetica", PLAIN, 13)

        var previousPoint: Pair<Double, Double>? = null

        val graphicsPairs = GuiUtils.createGraphicsCoordinates(lines, scaleFactorY, offsetX, offsetY)

        val bImg = BufferedImage(guiPanel.width, guiPanel.height, TYPE_INT_RGB)
        val cg = bImg.createGraphics()
        guiPanel.paintAll(cg)

        for (currentPoint in graphicsPairs) {
            //            drawImageToSave(previousPoint, currentPoint, cg, imageFileName, showNames)
            previousPoint = currentPoint
        }
    }


    /** Draw a line in the context between the two points */
    private fun drawUiPreview(
            possiblePreviousPoint: Pair<Double, Double>?,
            currentPoint: Pair<Double, Double>,
            graphics: Graphics2D,
            fileName: String,
            showName: Boolean,
                             ) {
        val previousPoint = when {
            isUsable(possiblePreviousPoint) -> possiblePreviousPoint !!
            else                            -> currentPoint
        }
        val line = Line2D.Double(previousPoint.first, previousPoint.second, currentPoint.first, currentPoint.second)
        graphics.color = BLACK

        graphics.draw(line)
        if (showName) {
            //            graphics.setRenderingHints(nonAliasedRenderingHints)
            graphics.drawString(fileName, 20, 30);
            //            graphics.setRenderingHints(aliasedRenderingHints)
        }
    }
}


//private fun drawImageToSave(
//        possiblePreviousPoint: Pair<Double, Double>?,
//        currentPoint: Pair<Double, Double>,
//        graphics: Graphics2D,
//        fileName: String,
//        showName: Boolean,
//                           ) {
//    val previousPoint = when {
//        isUsable(possiblePreviousPoint) -> possiblePreviousPoint !!
//        else                                     -> currentPoint
//    }
//    val line = Line2D.Double(previousPoint.first, previousPoint.second, currentPoint.first, currentPoint.second)
//    graphics.color = GRAY
//    graphics.draw(line)
//    if (showName) {
//        //        graphics.setRenderingHints(nonAliasedRenderingHints)
//        graphics.drawString(fileName, 20, 30);
//        //        graphics.setRenderingHints(aliasedRenderingHints)
//    }
//}

