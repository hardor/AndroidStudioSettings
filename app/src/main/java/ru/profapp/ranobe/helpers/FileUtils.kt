package ru.profapp.ranobe.helpers

import java.io.*


object FileUtils {

    fun deleteFile(destPath: String, fileName: String) {

        val myFile = File(destPath, fileName)
        if (myFile.exists()) myFile.delete()
    }


    fun copyFile(toCopyPath: String, destPath: String): Boolean {

        val toCopyFile = File(toCopyPath)
        val destFile = File(destPath)

        return copyFile(toCopyFile, destFile)
    }

    private fun copyFile(toCopy: File, destFile: File): Boolean {
        try {
            return copyStream(FileInputStream(toCopy), FileOutputStream(destFile))
        } catch (e: FileNotFoundException) {
            logError("CopyFile", "", e, false)
        }

        return false
    }

    private fun copyStream(inputStream: InputStream, os: OutputStream): Boolean {
        try {
            val buf = ByteArray(1024)
            var len = inputStream.read(buf)
            while (len > 0) {
                os.write(buf, 0, len)
                len = inputStream.read(buf)
            }
            inputStream.close()
            os.close()
            return true
        } catch (e: IOException) {
            logError("copyStream", "", e, false)
        }
        return false
    }

    fun dirChecker(_targetLocation: String) {
        val folder = File(_targetLocation)

        if (!folder.exists()) {
            if (folder.isDirectory) folder.mkdirs()
            else folder.parentFile.mkdirs()
        }


    }

}