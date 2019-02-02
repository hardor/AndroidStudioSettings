package ru.profapp.ranobe.helpers

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipHelper {

    const val BUFFER = 2048

    fun zip(_files: Array<String>, zipFolderName: String, zipFileName: String): Boolean {
        try {

            dirChecker(zipFolderName)
            var origin: BufferedInputStream? = null
            val dest = FileOutputStream("$zipFolderName/$zipFileName")
            val out = ZipOutputStream(
                BufferedOutputStream(
                    dest
                )
            )
            val data = ByteArray(BUFFER)

            for (i in _files.indices) {

                val f = File(_files[i]);
                if (f.isFile && f.canRead()) {
                    try {
                        val fi = FileInputStream(_files[i])
                        origin = BufferedInputStream(fi, BUFFER)

                        val groups = _files[i].split("/");
                        val entry =
                            ZipEntry("${groups[groups.size - 2]}/${groups[groups.size - 1]}")

                        out.putNextEntry(entry)
                        var count: Int = origin.read(data, 0, BUFFER)

                        while (count != -1) {
                            out.write(data, 0, count)
                            count = origin.read(data, 0, BUFFER)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        origin?.close()
                    }
                }

            }

            out.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    fun unzip(_zipFile: String, _targetLocation: String): Boolean {

        //create target location folder if not exist
        dirChecker(_targetLocation)

        try {
            val fin = FileInputStream(_zipFile)
            val zin = ZipInputStream(fin)
            var ze: ZipEntry? = zin.nextEntry
            while (ze != null) {

                //create dir if required while unzipping
                if (ze.isDirectory) {
                    dirChecker(ze.name)
                } else {
                    val fout = FileOutputStream(_targetLocation + ze.name)
                    var c = zin.read()
                    while (c != -1) {
                        fout.write(c)
                        c = zin.read()
                    }

                    zin.closeEntry()
                    fout.close()
                }
                ze = zin.nextEntry
            }
            zin.close()
            return true
        } catch (e: Exception) {
            println(e)
            return false
        }

    }

    fun dirChecker(_targetLocation: String) {
        val folder = File(_targetLocation)
        if (!folder.exists())
            folder.mkdirs()

    }
}