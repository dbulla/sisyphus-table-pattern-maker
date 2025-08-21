package com.nurflugel.sisyphus.generators.sunbursts

import com.nurflugel.sisyphus.generators.domain.Point
import com.nurflugel.sisyphus.generators.domain.Point.Companion.pointFromRad
import com.nurflugel.sisyphus.output.gui.GuiPreviewer
import com.nurflugel.sisyphus.output.gui.GuiUtils.Companion.imagesDir
import com.nurflugel.sisyphus.output.gui.GuiUtils.Companion.tracksDir
import com.nurflugel.sisyphus.output.gui.ImageWriterController
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset
import java.time.Duration
import java.time.Instant
import kotlin.math.PI
import kotlin.math.cos


class ClockworkWigglerGenerator {

    companion object {
//        private const val START: Int = 0;
//        private const val END: Int = 501;
        private const val START: Int = 0;
        private const val END: Int = 5;
        private const val USE_SELECTED_VALUES: Boolean = false // use a list of values, specified below
        private val values = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19)

        private const val SHOW_NAMES: Boolean = false  // if set to true the value will be rendered in the image, but this slows down rendering a TON
        private const val WRITE_TRACKS: Boolean = true // if true, will save tracks to disk
        private const val SHOW_PREVIEW: Boolean = false // if true, will show images in the UI
        private const val GENERATE_IMAGES: Boolean = true // set to false if you just want a dry run w/o saving images
        private const val OVERWRITE_EXISTING_IMAGES: Boolean = true // set to false if you just want a dry run w/o saving images
        private const val SAVE_IMAGES: Boolean = true

        private const val TWO_PI = 2 * PI
        private const val NUMBER_OF_TICKS = 100000
        private const val NUMBER_OF_TICKS_PER_TURN = 1000
        private const val NUMBER_OF_RHOS = 1
        private const val R0 = 1.2
        private const val R1 = 0.2 //1.0 - r0
        private var W0 = 0.0 // second-hand speed - 1000 ticks per rev
        private var W1 = 0.0;
        private const val DELTA_RHO_PER_TURN = .01
        private const val THETA_ADVANCE = 1.3

        // Setting to something other than 0 will allow the app to "resume" the count if it's been interrupted.
        // Todo - output status in a file so app can automatically restart if interrupted.
        private var count = 0;

        private var initialCount = count;
        private const val MULTIPLIER: Int = 320 * 4; // how "fast" or "slow" the video will run
        // normally we just use the value below, but in this case we want to start at 631830
        //    private const val startIndex = 631830 // last value output when machine is rebooted
        private const val START_INDEX = START * MULTIPLIER - 1
        private const val END_INDEX = END * MULTIPLIER

        private val imageToValueMapFile: File = File("imageToValueMapFile.txt") // map so you can find which image corresponds to which track

        @JvmStatic
        fun main(args: Array<String>) {

            println("count = $count")
            println("start = $START")
            println("end = $END")
            println("startIndex = $START_INDEX")
            println("endIndex = $END_INDEX")
            println("showNames = $SHOW_NAMES")
            println("writeTracks = $WRITE_TRACKS")
            println("showPreview = $SHOW_PREVIEW")
            println("generateImages = $GENERATE_IMAGES")
            println("saveImages = $SAVE_IMAGES")
            println("image to value map = $imageToValueMapFile")

            val generator = ClockworkWigglerGenerator()
            val guiPreviewer: GuiPreviewer? = if (SHOW_PREVIEW) GuiPreviewer() else null
            val imageWriterController: ImageWriterController? = if (SAVE_IMAGES) ImageWriterController() else null
            if (SHOW_PREVIEW && guiPreviewer != null) {
                guiPreviewer.initialize()
            }
            else {
                System.setProperty("java.awt.headless", "true")
            }
            if (SAVE_IMAGES && imageWriterController != null) {
                imageWriterController.initialize()
            }

            if (USE_SELECTED_VALUES) { // we want JUST these specific values
                for (waviness in values) {
//                for (waviness in 1000..3000) {
                    W1 = W0 * waviness // waviness of the tip of the secondhand - 33 revs per rev
                    val imageFileName = createImageFileBaseName(count++)
                    val trackFileName = createTrackFileName(waviness.toDouble())
                    generator.doIt(waviness.toDouble(), imageFileName, trackFileName, guiPreviewer, imageWriterController, GENERATE_IMAGES)
                }
            }
            else { // Use the iterative generator to select values
                val startTime = Instant.now()
                runBlocking {
                    for (i in START_INDEX..END_INDEX) {
                        //                        launch {
                        generateSunburstWithParameters(
                            startTime,
                            i,
                            generator,
                            guiPreviewer,
                            imageWriterController
                        ) //  the average speed does not show the speed at the current moment.
                        //                            val end = Instant.now()
                        //                            val duration2 = Duration.between(now, end).toMillis()
                        //          if (duration2 > 0) {
                        //            println("current speed = ${duration.toFloat() / 1000} images/sec")
                        //          }
                        //                        }
                    }
                }
            }
            // uncomment below to auto-shutdown, rather than waiting for the user to press a keystroke
            if (SHOW_PREVIEW) guiPreviewer!!.shutDown()
        }

        private fun generateSunburstWithParameters(
            startTime: Instant?,
            i: Int,
            generator: ClockworkWigglerGenerator,
            guiPreviewer: GuiPreviewer?,
            imageWriterController: ImageWriterController?,
        ) {
            val loopStartTime = Instant.now()
            val waviness: Double = i.toDouble() / MULTIPLIER.toDouble()
            W1 = W0 * waviness // waviness of the tip of the secondhand - 33 revs per rev
            val imageFileName = createImageFileBaseName(count)
            val trackFileName = createTrackFileName(waviness)
            val fileAlreadyExists = File(imageFileName).exists()
            // only generate the image if it doesn't already exist.
            if (!fileAlreadyExists || OVERWRITE_EXISTING_IMAGES) {
                generator.doIt(
                    waviness,
                    imageFileName,
                    trackFileName,
                    guiPreviewer,
                    imageWriterController,
                    GENERATE_IMAGES
                )
            }
            val now = Instant.now()
            val duration = Duration.between(loopStartTime, now).toMillis()

            val extraBit = if (duration > 0) "speed = ${1.0 / (duration.toFloat() / 1000)} images/sec" else ""
            println("count = ${count++}, delta count = ${count - initialCount} $extraBit")
        }

        // image files need to have a numeric sequence so they can be converted to animations
        fun createImageFileBaseName(count: Int): String {
            val expandedCount = count.toString().padStart(6, '0')
            return "./$imagesDir/image_$expandedCount.png"
        }

        private fun createTrackFileName(waviness: Double): String {
            return "clockworkSwirl6_${NUMBER_OF_TICKS_PER_TURN}_$waviness.thr"
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
    fun doIt(
        waviness: Double,
        imageFileName: String,
        trackFileName: String,
        plotterGui: GuiPreviewer?,
        imageWriterController: ImageWriterController?,
        generateImages: Boolean, // set to false if you just want to do a dry-run
    ) {
        if (WRITE_TRACKS) {
            imageToValueMapFile.appendText("$imageFileName\t$trackFileName\n")
        }
        W0 = TWO_PI / NUMBER_OF_TICKS_PER_TURN
        W1 = W0 * waviness // waviness of the tip of the secondhand - 33 revs per rev
        //    println(
        //        """
        //|   numberOfCounts = $numberOfTicks
        //|   numberOfRhos =   $numberOfRhos
        //|   waviness =       $waviness
        //|   r0 =             $r0
        //|   r1 =             $r1
        //|   w0 =             $w0
        //|   w1 =             $w1""".trimMargin()
        //           )
        if (generateImages) {
            generateImageOld(trackFileName, plotterGui, imageWriterController, imageFileName)
        }
        //        println("Done!")
    }

    private fun generateImageOld(
        trackFileName: String,
        plotterGui: GuiPreviewer?,
        imageWriterController: ImageWriterController?,
        imageFileName: String,
    ) {
        val points = mutableListOf<Point>()

        val deltaRhoPerCount = DELTA_RHO_PER_TURN / NUMBER_OF_TICKS_PER_TURN
        var rho = R0

        //      println("   rho = $rho")
        for (t in 0..NUMBER_OF_TICKS) {
            val thetaInRads = W0 * t * THETA_ADVANCE  // the second hand angle
            val thetaOneInRads = W1 * t              // controls the height of the wiggle
            val deltaRho = R1 * cos(thetaOneInRads)

            if (isValid(thetaOneInRads)) {
                points.add(pointFromRad(rho = rho + deltaRho, thetaInRads = thetaInRads))
            }
            rho -= deltaRhoPerCount
            if (rho < 0.0) break
        }

        val output = mutableListOf<String>()
        output.add("816.6588952562589 0.0")
        points.reversed().filter { !it.thetaInRads().isNaN() }.forEach { output.add("${it.thetaInRads()}  ${it.rho.formatNicely(4)}") }
        if (SAVE_IMAGES) {
            FileUtils.forceMkdir(File(imagesDir))
        }

        if (WRITE_TRACKS) { // only write the tracks if desired - for large animations, it takes too much file space
            // add the contents of this file, so we can remember how to get it back!
//            val programLines = FileUtils.readLines(File("src/main/kotlin/com/nurflugel/sisyphus/generators/sunbursts/ClockworkWigglerGenerator.kt"), Charset.defaultCharset())
//            output.add("//")
//            output.add("// add this file so we can remember how to get it back!")
//            output.add("//")
//            programLines.forEach { output.add("// $it") }
            FileUtils.forceMkdir(File(tracksDir))
            FileUtils.writeLines(File("$tracksDir/$trackFileName"), output)
        }
//        if (SHOW_PREVIEW && plotterGui != null) {
//            plotterGui.showPreview(trackFileName, output, SHOW_NAMES) // show the preview && write the images to disk
//        }
//        if (SAVE_IMAGES && imageWriterController != null) {
//            imageWriterController.writeImage(imageFileName, output, SHOW_NAMES) //write the images to disk
//        }
    }

    /** Determine how to clip the delta Rho */
    private fun isValid(thetaOneInRads: Double): Boolean {
        val trimmedTheta = ((thetaOneInRads % TWO_PI) + TWO_PI) % TWO_PI
        // what the above does is strip away any theta greater than 2 PI.

        val thetaInDegrees = Math.toDegrees(trimmedTheta)
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
}

