package ru.profapp.ranobe.backup

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import ru.profapp.ranobe.R
import ru.profapp.ranobe.common.SingletonHolder
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException


class DriveManager private constructor(context: Context) {

    companion object : SingletonHolder<DriveManager, Context>(::DriveManager);

    private val drive: Drive = Drive.Builder(AndroidHttp.newCompatibleTransport(),
        JacksonFactory.getDefaultInstance(),
        GoogleAccountCredential.usingOAuth2(context,
            listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)).setSelectedAccountName(
            GoogleSignIn.getLastSignedInAccount(context)?.email))
        .setApplicationName(context.getString(R.string.app_name)).build()

    @Throws(IOException::class)
    fun upload(file: java.io.File,
               mimeType: String,
               name: String,
               block: (Drive.Files.Create.() -> Drive.Files.Create)? = null): File {
        val fileMeta = File()
        fileMeta.name = name
        val fileContent = FileContent(mimeType, file)
        var create = drive.Files().create(fileMeta, fileContent)
        create = if (block != null) {
            create.block()
        } else {
            create
        }
        return create.execute()
    }

    @Throws(IOException::class)
    fun download(fileId: String, filePath: String) {

        val restoreFile = java.io.File(filePath)
        ByteArrayOutputStream().use { outputStream ->
            FileOutputStream(restoreFile).use { fos ->
                drive.files().get(fileId).executeMediaAndDownloadTo(outputStream)
                outputStream.writeTo(fos)
            }
        }

    }

    fun isAppFolderExists(): Boolean {
        val appDataFolder = drive.files().list().setSpaces("appDataFolder").execute()
        return appDataFolder != null && appDataFolder.isNotEmpty()
    }

    fun query(name: String): List<File>? {
        val queryString = "name contains '$name' and trashed=false"
        val queryResult = drive.files().list().setQ(queryString).setOrderBy("modifiedTime")
            .execute()
        return queryResult.files
    }

    fun delete(file: File) {
        drive.files().delete(file.id).execute()
    }

    fun queryChildren(folder: File, fileName: String): List<File>? {
        val folderQuery = Query.build {
            filters = mutableListOf<String>().apply {
                add("mimeType = 'application/vnd.google-apps.drive-sdk'")
                add("name = '${folder.name}'")
            }
        }
        val folders = drive.files().list().setQ(folderQuery.toString()).execute()
        val result = mutableListOf<File>()
        folders.files.forEach {
            val fileQuery = Query.build {
                filters = mutableListOf<String>().apply {
                    add("mimeType != 'application/vnd.google-apps.folder'")
                    add("'${it.id}' in parents")
                    add("name = '$fileName'")
                }
            }
            val queryResult = drive.files().list().setQ(fileQuery.toString()).execute()
            result.addAll(queryResult.files)
        }
        return result
    }
}