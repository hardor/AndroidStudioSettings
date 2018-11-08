package ru.profapp.RanobeReader.Utils

import ru.profapp.RanobeReader.Helpers.LogHelper
import java.io.*
import java.net.JarURLConnection
import java.net.URL

object FileUtils {

    fun copyFile(toCopy: File, destFile: File): Boolean {
        try {
            return FileUtils.copyStream(FileInputStream(toCopy),
                    FileOutputStream(destFile))
        } catch (e: FileNotFoundException) {
            LogHelper.logError(LogHelper.LogType.ERROR, "CopyFile", "", e, false)
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
            LogHelper.logError(LogHelper.LogType.ERROR, "copyStream", "", e, false)
        }
        return false
    }

}