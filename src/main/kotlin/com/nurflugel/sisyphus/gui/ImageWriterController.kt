package com.nurflugel.sisyphus.gui

import com.nurflugel.sisyphus.gui.GuiUtils.Companion.aliasedRenderingHints
import com.nurflugel.sisyphus.gui.GuiUtils.Companion.createGraphicsCoordinates
import com.nurflugel.sisyphus.gui.GuiUtils.Companion.imagesDir
import com.nurflugel.sisyphus.gui.GuiUtils.Companion.isUsable
import org.apache.commons.io.FileUtils
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants.EXIT_ON_CLOSE
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import java.awt.GraphicsEnvironment


class ImageWriterController {

    companion object {
        private const val WIDTH = 2560
        private const val HEIGHT = 1440
    }

    private lateinit var graphics2D: Graphics2D

    internal fun initialize() {
        val localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val ge = localGraphicsEnvironment
        val fonts = ge.availableFontFamilyNames
        val bufferedImage = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)
        graphics2D = localGraphicsEnvironment.createGraphics(bufferedImage)
    }

    //    // todo split this up into showPreview and writeImage - clearer, easier to turn on/off what's desired
    fun writeImage(imageFileName: String, lines: MutableList<String>, showNames: Boolean) {
        val localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val ge = localGraphicsEnvironment
        val fonts = ge.availableFontFamilyNames
        val bufferedImage = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)
        graphics2D = localGraphicsEnvironment.createGraphics(bufferedImage)
        // initial point of null
        // go through lines, read new current point  if not a comment/empty
        // draw line from previous point to this point
        // current point becomes previous point
        //                val graphicsContext = getGraphicsContext()
        //                val graphics2D = graphicsContext.graphics as Graphics2D
        graphics2D.clearRect(0, 0, WIDTH, HEIGHT)
        val scaleFactorY = HEIGHT / 2 // 2 * rho=1 gives two 
        val scaleFactorX = WIDTH / 2 // 2 * rho=1 gives two 
        val offsetX = scaleFactorX * 1 // todo x and y
        val offsetY = scaleFactorY * 1 // todo x and y

        graphics2D.setRenderingHints(aliasedRenderingHints)
        graphics2D.font = Font("Helvetica", Font.PLAIN, 13)

        var previousPoint: Pair<Double, Double>? = null
        val graphicsPairs = createGraphicsCoordinates(lines, scaleFactorY, offsetX, offsetY)
        val cg = bufferedImage.createGraphics()

        for (currentPoint in graphicsPairs) {
            drawImageToSave(previousPoint, currentPoint, cg, imageFileName, showNames)
            previousPoint = currentPoint
        }
        val imageFile = File("./$imagesDir/${imageFileName}")
        if (ImageIO.write(bufferedImage, "png", imageFile)) {
            println("$imageFile -- saved")
        }
    }

    private fun drawImageToSave(
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
        graphics.color = Color.GRAY
        graphics.draw(line)
        if (showName) {
            //        graphics.setRenderingHints(nonAliasedRenderingHints)
            graphics.drawString(fileName, 20, 30);
            //        graphics.setRenderingHints(aliasedRenderingHints)
        }
    }

}