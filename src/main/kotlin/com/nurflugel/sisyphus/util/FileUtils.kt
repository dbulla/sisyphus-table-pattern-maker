package com.nurflugel.sisyphus.util

import com.nurflugel.sisyphus.sunbursts.ClockworkWigglerGenerator.Companion.createImageFileBaseName
import java.io.File
import java.time.Duration
import java.time.Instant

class FileUtils {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
            val oldDir = "./images4"
            val newDir = "./imagesForVideoProcessing"
            val startNum = 1 // start at 1, not 0
            val endNum = 999999
            val skipNum = 1
            val shouldCleanExistingFiles = true
            copyFilesToNewDir(oldDir, newDir, startNum, endNum, skipNum, shouldCleanExistingFiles)
    }

    /**
     * Copies files from the old dir to the new dir.  Options are:
     * @param oldDir Source dir for images
     * @param newDir target dir for images
     * @param startNum starting index to copy, will be left-padded with 0's - 5 would start at image_000005.png
     * @param endNum Final index to copy.
     * @param skipNum Increment to copy - will result in 1/skipNum files being copied.  NOTE: this will also result in the files being renamed sequentially so the MPEG creator can properly process them
     * @param shouldCleanExistingFiles If true, will delete any files in the target dir first.
     */
    @Suppress("SameParameterValue")
    private fun copyFilesToNewDir(
        oldDir: String,
        newDir: String,
        startNum: Int,
        endNum: Int,
        skipNum: Int = 1,
        shouldCleanExistingFiles: Boolean = true,
                                 ) {

      val targetDir = File(newDir)
      val alreadyExists = targetDir.exists()
      if (shouldCleanExistingFiles && alreadyExists) {
        targetDir.deleteRecursively()
      }
      var count = 0;
      val start = Instant.now()
      val totalImagesToProcess = (endNum - startNum) / skipNum
      targetDir.mkdirs()
      for (index in startNum..endNum step skipNum) {
        val sourceFileName = createImageFileBaseName(index)
        val targetFileName = when (skipNum) {
          1   -> sourceFileName
          else -> createImageFileBaseName(count)
        }
        if (count > 0 && count % 10 == 0) {
          val now = Instant.now()
          val timeSoFar: Long = Duration.between(start, now).toMillis() / 1000
          val rate = count.toFloat() / timeSoFar
          val timeRemaining: Long = totalImagesToProcess * timeSoFar / count // in seconds
          val eta = now.plusSeconds(timeRemaining)
          println("sourceFileName = $sourceFileName, targetFileName = $targetFileName, count = $count, rate = $rate images copied/sec, time remaining: ${timeRemaining / 60.0} minutes, ETA: $eta")
        }
                println("targetFileName = ${targetFileName}")
        File(oldDir, sourceFileName).copyTo(File(targetDir, targetFileName))
        count ++
      }
    }
  }
}