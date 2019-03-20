package ru.profapp.ranobe.helpers

import ru.profapp.ranobe.helpers.FileUtils.dirChecker
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


object ZipHelper {

    private const val BUFFER = 2048

    fun zip(_files: Array<String>, zipFolderName: String, zipFileName: String): Boolean {
        try {

            dirChecker(zipFolderName)

            val dest = FileOutputStream("$zipFolderName/$zipFileName")

            val byteArrayOutputStream = zipToMemory(_files) ?: return false

            byteArrayOutputStream.writeTo(dest)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    fun getFilesInFolder(path: String): List<String> {

        val f = File(path)
        if (f.exists() && f.isDirectory) {

        }
        return listOf<String>()
    }

    private fun zipToMemory(_files: Array<String>): ByteArrayOutputStream? {

        val baos: ByteArrayOutputStream = ByteArrayOutputStream()
        val zos: ZipOutputStream = ZipOutputStream(baos)
        var origin: BufferedInputStream? = null
        try {
            val data = ByteArray(BUFFER)

            for (i in _files.indices) {

                val f = File(_files[i])

                if (f.isDirectory) {
                    zipSubFolder(zos, f, f.parent.length)
                } else if (f.isFile) {
                    try {
                        val fi = FileInputStream(_files[i])
                        origin = BufferedInputStream(fi, BUFFER)

                        val groups = _files[i].split("/")
                        val entry = ZipEntry("${groups[groups.size - 2]}/${groups[groups.size - 1]}")

                        zos.putNextEntry(entry)
                        var count: Int = origin.read(data, 0, BUFFER)

                        while (count != -1) {
                            zos.write(data, 0, count)
                            count = origin.read(data, 0, BUFFER)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        origin?.close()
                    }
                }

            }

            return baos

        } catch (ioe: IOException) {
            ioe.printStackTrace()
            return null

        } finally {
            zos.closeEntry()
            zos.close()
        }

    }

    private fun zipSubFolder(out: ZipOutputStream, folder: File, basePathLength: Int) {

        val BUFFER = 2048

        val fileList = folder.listFiles()
        var origin: BufferedInputStream? = null
        for (file in fileList) {
            if (file.isDirectory) {
                zipSubFolder(out, file, basePathLength)
            } else {
                val data = ByteArray(BUFFER)
                val unmodifiedFilePath = file.path
                val relativePath = unmodifiedFilePath.substring(basePathLength)
                val fi = FileInputStream(unmodifiedFilePath)
                origin = BufferedInputStream(fi, BUFFER)
                val entry = ZipEntry(relativePath)
                entry.time = file.lastModified() // to keep modification time after unzipping
                out.putNextEntry(entry)
                var size: Int
                do {
                    size = origin.read(data, 0, BUFFER)
                    if (size < 0) break
                    out.write(data, 0, size)
                } while (true)


                origin.close()
            }
        }
    }

    fun unzip(_zipFile: String, _targetLocation: String): Boolean {

        return unzip(FileInputStream(_zipFile), _targetLocation)

    }

    private fun unzip(_fileInputStream: FileInputStream, _targetLocation: String): Boolean {

        //create target location folder if not exist

        val buffer = ByteArray(1024)
        try {

            dirChecker(_targetLocation)

            val zin = ZipInputStream(BufferedInputStream(_fileInputStream))
            var ze: ZipEntry? = zin.nextEntry
            while (ze != null) {

                val fileName = ze.name
                val unzippedFile = File(_targetLocation + File.separator + fileName)

                if (ze.isDirectory) {
                    unzippedFile.mkdir()
                } else {
                    File(unzippedFile.parent).mkdirs()
                    val fout = FileOutputStream(_targetLocation + ze.name)
                    var size: Int
                    do {
                        size = zin.read(buffer, 0, buffer.size)
                        if (size < 0) break
                        fout.write(buffer, 0, size)
                    } while (true)

                    fout.close()
                }
                ze = zin.nextEntry
            }
            zin.closeEntry()
            zin.close()
            return true
        } catch (e: Exception) {
            println(e)
            return false
        }

    }


}