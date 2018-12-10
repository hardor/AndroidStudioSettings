package ru.profapp.ranobe.utils

import ru.profapp.ranobe.helpers.LogType
import ru.profapp.ranobe.helpers.logError
import java.io.*

object FileUtils {

    fun copyFile(toCopy: File, destFile: File): Boolean {
        try {
            return FileUtils.copyStream(FileInputStream(toCopy),
                    FileOutputStream(destFile))
        } catch (e: FileNotFoundException) {
            logError(LogType.ERROR, "CopyFile", "", e, false)
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
            logError(LogType.ERROR, "copyStream", "", e, false)
        }
        return false
    }

}