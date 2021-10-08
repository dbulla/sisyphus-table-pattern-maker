package com.nurflugel.sisyphus.sunbursts

import com.nurflugel.sisyphus.domain.Point
import com.nurflugel.sisyphus.domain.Point.Companion.pointFromRad
import com.nurflugel.sisyphus.gui.GuiPreviewer
import com.nurflugel.sisyphus.gui.GuiUtils.Companion.imagesDir
import com.nurflugel.sisyphus.gui.GuiUtils.Companion.tracksDir
import com.nurflugel.sisyphus.gui.ImageWriterController
import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.Math.PI
import java.nio.charset.Charset
import java.time.Duration
import java.time.Instant
import kotlin.math.acos
import kotlin.math.cos

class ClockworkWigglerGenerator {

    companion object {
        const val numberOfTicks = 100000
        const val numberOfTicksPerTurn = 1000
        const val numberOfRhos = 1
        const val r0 = 1.2
        const val r1 = 0.2 //1.0 - r0
        var w0 = 0.0 // second hand speed -  1000 ticks per rev
        var w1 = 0.0;
        const val deltaRhoPerTurn = .01
        const val thetaAdvance = 1.3
        var count = 0;
        private const val multiplier: Int = 320 * 4;
        private const val start: Int = 0;
        private const val end: Int = 501;
        private const val showNames: Boolean = false
        private const val writeTracks: Boolean = false
        private const val showPreview: Boolean = false
        private const val saveImages: Boolean = true

        @JvmStatic
        fun main(args: Array<String>) {

            val generator = ClockworkWigglerGenerator()
            val plotterGui: GuiPreviewer? = if (showPreview) GuiPreviewer() else null
            val imageWriterController: ImageWriterController? = if (saveImages) ImageWriterController() else null
            if (showPreview && plotterGui != null) {
                plotterGui.initialize()
            } else {
                System.setProperty("java.awt.headless", "true")
            }
            if (saveImages && imageWriterController != null) {
                imageWriterController.initialize()
            }

            if (true) { // cheesy way of turning off the iterative generator and work from a given list instead, w/o commenting out the code
                val startTime = Instant.now()
                for (i in (start * multiplier) - 1..(end * multiplier)) {
                    val now = Instant.now()
                    val duration = Duration.between(startTime, now).toMillis()
                    println("count = ${count ++} ")
                    if (count > 0 && duration > 0) {
                        println("average speed = ${count / (duration.toFloat() / 1000)} images/sec")
                    }
                    val waviness: Double = i.toDouble() / multiplier.toDouble()
                    w1 = w0 * waviness // waviness of the tip of the secondhand - 33 revs per rev
                    generator.doIt(waviness, createImageFileBaseName(count), createTrackFileName(waviness), plotterGui, imageWriterController)
                    //  the average speed does not show the speed at the current moment.
                    val end = Instant.now()
                    val duration2 = Duration.between(now, end).toMillis()
                    if (duration2 > 0) {
                        println("current speed = ${1.0 / (duration.toFloat() / 1000)} images/sec")
                    }
                }
            } else { // we want JUST these specific values
                val values = listOf(
                        //                        0.00000001,
                        //                        0.8625,
                        //                        1.2875,
                        //                        1.325,
                        //                        1.725,
                        //                        1.95,
                        //                        2.275,
                        //                        2.4,
                        //                        2.5875,
                        4.1,
                        4.3375,
                        26.85,
                                   )
                for (waviness in values) {
                    w1 = w0 * waviness // waviness of the tip of the secondhand - 33 revs per rev
                    generator.doIt(waviness, createImageFileBaseName(count ++), createTrackFileName(waviness), plotterGui, imageWriterController)
                }
            }
            // uncomment below to auto-shutdown, rather than waiting for the user to press a keystroke
            if (showPreview)
                plotterGui !!.shutDown()
        }

        // image files need to have a numeric sequence so they can be converted to animations
        public fun createImageFileBaseName(count: Int): String {
            val expandedCount = count.toString().padStart(6, '0')
            return "image_$expandedCount.png"
        }

        private fun createTrackFileName(waviness: Double): String {
            return "clockworkSwirl6_${numberOfTicksPerTurn}_$waviness.thr"
        }

    }


    /**
     * Think of a secondhand on a clock as the end traces out a circle as it turns.
     *
     * Now, add another, smaller clock at the end of the second hand - as it's second hand turns at a different rate from the main.
     *
     * What sort of pattern does it trace?  What if that one also had an even smaller second hand at it's end?
     *
     * Requirements - all r0 through rn MUST equal 1 for the table to start nicely.  Else, add an extra start point at rho=0 or 1 to appease the table gods.
     *
     */
    fun doIt(waviness: Double, imageFileName: String, trackFileName: String, plotterGui: GuiPreviewer?, imageWriterController: ImageWriterController?) {
        w0 = 2 * PI / numberOfTicksPerTurn
        w1 = w0 * waviness // waviness of the tip of the secondhand - 33 revs per rev
        println(
                """
|   numberOfCounts = $numberOfTicks
|   numberOfRhos =   $numberOfRhos
|   waviness =       $waviness
|   r0 =             $r0
|   r1 =             $r1
|   w0 =             $w0
|   w1 =             $w1""".trimMargin()
               )

        val points = mutableListOf<Point>()

        val deltaRhoPerCount = deltaRhoPerTurn / numberOfTicksPerTurn
        var rho = r0

        println("   rho = $rho")
        for (t in 0..numberOfTicks) {
            val thetaInRads = w0 * t * thetaAdvance  // the second hand angle
            val thetaOneInRads = w1 * t              // controls the height of the wiggle
            val deltaRho = r1 * cos(thetaOneInRads)

            if (isValid(thetaOneInRads))
                points.add(pointFromRad(rho = rho + deltaRho, thetaInRads = thetaInRads))

            rho -= deltaRhoPerCount
            if (rho < 0.0) break
        }

        val output = mutableListOf<String>()
        output.add("816.6588952562589 0.0")

        points
            .reversed()
            .filter { ! it.thetaInRads().isNaN() }
            .forEach { output.add("${it.thetaInRads()}  ${it.rho.formatNicely(4)}") }

        FileUtils.forceMkdir(File(imagesDir))

        if (writeTracks) { // only write the tracks if desired - for large animations, it takes too much file space
            // add the contents of this file, so we can remember how to get it back!
            val programLines = FileUtils.readLines(File("src/main/kotlin/com/nurflugel/sisyphus/sunbursts/ClockworkWigglerGenerator.kt"), Charset.defaultCharset())
            output.add("//")
            output.add("// add this file so we can remember how to get it back!")
            output.add("//")
            programLines.forEach { output.add("// $it") }
            FileUtils.forceMkdir(File(tracksDir))
            FileUtils.writeLines(File("$tracksDir/$trackFileName"), output)
        }
        if (showPreview && plotterGui != null) {
            plotterGui.showPreview(trackFileName, output, showNames) // show the preview && write the images to disk
        }
        if (saveImages && imageWriterController != null) {
            imageWriterController.writeImage(imageFileName, output, showNames) //write the images to disk
        }
        println("Done!")
    }

    /** Determine how to clip the delta Rho */
    private fun isValid(thetaOneInRads: Double): Boolean {
        val cosTheta = cos(thetaOneInRads)
        val trimmedTheta = acos(cosTheta)
        // what the above does is strip away any theta greater than 2 PI.

        val thetaInDegrees = trimmedTheta.radsToDegrees()
        val angleSpread = 90
        val startAngle = (180 - angleSpread / 2.0)
        return thetaInDegrees > startAngle && thetaInDegrees <= 180
    }


    /**
     * Take the number and, if it's > 1, replace it with 1, if
     * it's < 0, replace it with 0.  Else, trim to the number of digits
     * @param digits Number of digits after the decimal
     */
    private fun Double.formatNicely(digits: Int): String {
        val number = when {
            this > 1.0 -> 1.0
            this < 0.0 -> 0.0
            else       -> this
        }
        return "%.${digits}f".format(number)
    }

    private fun Double.radsToDegrees(): Double = this * 180.0 / PI
    private fun Double.degreesToRads(): Double = this / 180.0 * PI
}

