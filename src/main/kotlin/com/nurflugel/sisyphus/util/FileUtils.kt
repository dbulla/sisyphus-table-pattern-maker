package com.nurflugel.sisyphus.util

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
         * @param oldDir Source dir fior images
         */
        private fun copyFilesToNewDir(oldDir: String, newDir: String, startNum: Int, endNum: Int, skipNum: Int, shouldCleanExistingFiles: Boolean) {


        }
    }
}