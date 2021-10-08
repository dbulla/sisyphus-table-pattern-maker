package com.nurflugel.sisyphus.util

import com.nurflugel.sisyphus.sunbursts.ClockworkWigglerGenerator.Companion.createImageFileBaseName
import java.io.File

class FileUtils {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val oldDir = "./images4"
            val newDir = "./imagesForVideoProcessing"
            val startNum = 0
            val endNum = 999999
            val skipNum = 1
            val shouldCleanExistingFiles = true
            copyFilesToNewDir(oldDir, newDir, startNum, endNum, skipNum, shouldCleanExistingFiles)
        }

        /**
         * Copies files from the old dir to the new dir.  Options are:
         * @param oldDir Source dir fior images
         * @param newDir target dir fior images
         * @param filePrefix How the files begins, minus the numbers, like "image_"
         * @param startNum starting index to copy, will be left-padded with 0's - 5 would start at image_000005.png
         * @param endNum Final index to copy.
         * @param skipNum Increment to copy - will result in 1/skipNum files being copied.  NOTE: this will also result in the files being renamed sequentially so the MPEG creator can properly process them
         * @param shouldCleanExistingFiles If true, will delete any files in the target dir first.
         */
        @Suppress("SameParameterValue")
        private fun copyFilesToNewDir(oldDir: String, newDir: String, startNum: Int, endNum: Int, skipNum: Int = 1, shouldCleanExistingFiles: Boolean = true) {
            val targetDir = File(newDir)
            if (shouldCleanExistingFiles && targetDir.exists()) {
                targetDir.delete()
            }
            var count = 0;
            targetDir.mkdirs()
            for (index in startNum..endNum step skipNum) {
                val sourceFileName = createImageFileBaseName(index)
                val targetFileName = when {
                    skipNum != 1 -> sourceFileName
                    else         -> createImageFileBaseName(count ++)
                }
                File(oldDir, sourceFileName).copyTo(File(targetDir, targetFileName))
            }
            //                    intStream.between(startNum, endNum).forEach{
            //                        val sourceFileName= filePrefix+ leftPad(it)+".png"
            //                        val number = 
            //                        val targetFileName= filePrefix + leftPad()
            //                    }
            //        
        }
    }
}