package com.nurflugel.sisyphus.sunbursts

import com.nurflugel.sisyphus.gui.GuiController
import com.nurflugel.sisyphus.sunbursts.Direction.INWARDS
import com.nurflugel.sisyphus.sunbursts.Direction.OUTWARDS
import org.apache.commons.io.FileUtils
import java.io.File
import kotlin.math.PI

enum class Direction {
    INWARDS, // from the outside in
    OUTWARDS // from the inside out
}

class SunburstGenerator {

    companion object {
        const val numberOfWaves = 7
        const val numberOfBurstsBase = 10
        const val numberOfBurstsMin = 4
        const val numberOfRaysBase = 20
        const val numberOfRaysMin = 4
        const val rhoVariance = .1
        const val thetaVariance = PI / 15


        @JvmStatic
        fun main(args: Array<String>) {
            SunburstGenerator().doIt(INWARDS)
        }
    }


    fun doIt(direction: Direction) {
        // Make three successive waves of bursts, largest to smallest
        // each burst will have n arms, which have each arm the center of randomly generated radial lines
        //
        // determines core rhos
        println("numberOfWaves= $numberOfWaves")
        println("numberOfBurstsBase= $numberOfBurstsBase")
        println("numberOfBurstsMin= $numberOfBurstsMin")
        println("numberOfRaysBase= $numberOfRaysBase")
        println("numberOfRaysMin= $numberOfRaysMin")
        println("rhoVariance= $rhoVariance")
        println("thetaVariance= $thetaVariance")
        val allRays = mutableListOf<Pair<Double, Double>>() // a ray has implied start of 0,0, out to rho, theta, and back.  
        val rand = java.util.Random()

        // start from the outside in
        (0..numberOfWaves).reversed().forEach { wave ->
            val raysForThisWave = mutableListOf<Pair<Double, Double>>()
            // determine core rho for the wave
            val maxRhoForWave = (1.0 / numberOfWaves) * wave
            // for each burst, vary theta and core rho 
            val numberOfBursts = (Math.abs(rand.nextGaussian()
                                          ) * numberOfBurstsBase + numberOfBurstsMin).toInt() // at least 2, as many as 6 - determines core thetas
            println("numberOfBursts = $numberOfBursts")
            // determine core theta
            val deltaTheta = 2 * PI / numberOfBursts
            println("deltaTheta = $deltaTheta")

            // for each burst...
            (0 until numberOfBursts).forEach { burst ->
                val coreTheta = deltaTheta * burst
                println("printing burst $burst of wave $wave at core theta ${coreTheta * 180 / PI} degrees (not rads!)")
                val numberOfRaysPerBurst = (Math.abs(rand.nextGaussian()
                                                    ) * numberOfRaysBase + numberOfRaysMin).toInt() // vary for each burst
                // now with core theta and core rho, draw n rays, varying rho and theta by a varied amount
                repeat((0..numberOfRaysPerBurst).count()) {
                    val rho = maxRhoForWave - Math.abs(rand.nextGaussian()) * rhoVariance
                    val theta = coreTheta + (coreTheta + (Math.abs(rand.nextGaussian()) * thetaVariance * 2 - thetaVariance))

                    println("rho = $rho  theta = $theta")
                    raysForThisWave.add(Pair(theta, rho))
                }
            }

            // make it so the rays for this wave are drawn at random, rather than all the rays for a burst
            // drawn together
            raysForThisWave.shuffle()
            // add the wave's rays to the total
            allRays.addAll(raysForThisWave)
        }

        // now generate .thr file
        val output = mutableListOf<String>()
        output.add("// numberOfWaves= $numberOfWaves")
        output.add("// numberOfBurstsBase= $numberOfBurstsBase")
        output.add("// numberOfBurstsMin= $numberOfBurstsMin")
        output.add("// numberOfRaysBase= $numberOfRaysBase")
        output.add("// numberOfRaysMin= $numberOfRaysMin")
        output.add("// rhoVariance= $rhoVariance")
        output.add("// thetaVariance= $thetaVariance")

        // for each ray, draw from center to end and back
        when (direction) {
            OUTWARDS -> output.add("0.0000    0.000") // start at 0,0
            INWARDS  -> output.add("0.0000    1.000") // start at 0,1
        }
        
        (0 until allRays.size).forEach {
            val rayN = allRays[it]
            when (direction) {
                OUTWARDS -> {
                    output.add("${rayN.first}    0.000") // rotate to new theta
                    output.add("${rayN.first}    ${Math.abs(rayN.second)}") // draw the ray out
                    output.add("${rayN.first}    0.000") // draw the ray back in
                }
                INWARDS  -> {
                    output.add("${rayN.first}    1.000") // rotate to new theta while rho =1
                    output.add("${rayN.first}    ${1.0 - Math.abs(rayN.second * 0.95)}") // draw the ray inwards (but not all the way)
                    output.add("${rayN.first}    1.000") // draw the ray back out to rho =1
                }
            }
        }

        FileUtils.writeLines(File("inwards_sunburst.thr"), output)

        val plotterGui = GuiController(output, "inwards_sunburst.thr")
        plotterGui.showPreview(ClockworkWigglerGenerator.fileName)
        println("Done!")


    }

}
