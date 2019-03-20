package ru.profapp.ranobe.activities

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.snackbar.Snackbar
import com.google.api.services.drive.DriveScopes
import droidninja.filepicker.FilePickerConst
import kotlinx.android.synthetic.main.activity_backup.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.EasyPermissions
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.MyApp.Companion.DB_NAME
import ru.profapp.ranobe.R
import ru.profapp.ranobe.backup.DriveManager
import ru.profapp.ranobe.backup.LocalBackup
import ru.profapp.ranobe.backup.Permissions
import ru.profapp.ranobe.common.Constants.Ranoberf_Login_Pref
import ru.profapp.ranobe.common.Constants.Rulate_Login_Pref
import ru.profapp.ranobe.common.Constants.last_chapter_id_Pref
import ru.profapp.ranobe.common.MyExceptionHandler
import ru.profapp.ranobe.helpers.*
import java.io.File
import java.io.IOException
import java.util.*


class BackupActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {


    private var driveManager: DriveManager? = null
    private lateinit var progressBar: ProgressBar


    companion object {
        val appFiles = mutableListOf<String>()

        private const val TAG = "Backup Activity"

        const val BACKUP_FILE_NAME = "RanobeReaderBackup.zip"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!MyApp.isApplicationInitialized) {


            launchActivity<MainActivity> {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            // We are done, so finish this activity and get out now
            finish()
            return
        }
        MyApp.component.inject(this)
        setupActionBar()


        setContentView(R.layout.activity_backup)
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))

        progressBar = findViewById(R.id.progressBar_backup)

        appFiles.add(this@BackupActivity.getDatabasePath(DB_NAME).absolutePath)
        appFiles.add("${filesDir.path}/../shared_prefs/$last_chapter_id_Pref.xml")
        appFiles.add("${filesDir.path}/../shared_prefs/$Rulate_Login_Pref.xml")
        appFiles.add("${filesDir.path}/../shared_prefs/$Ranoberf_Login_Pref.xml")
        appFiles.add("${filesDir.path}/../shared_prefs/${applicationContext.packageName}_preferences.xml")


        val builder = AlertDialog.Builder(this@BackupActivity)
            .setTitle(getString(R.string.backup_restore)).setIcon(R.drawable.ic_info_black_24dp)
            .setCancelable(true).setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }


        backup_button_backup.setOnClickListener {

            builder.setMessage(getString(R.string.readyToBackup)).setPositiveButton("OK") { _, _ ->
                progressBar.visibility = View.VISIBLE
                try {
                    LocalBackup(this).performBackup(appFiles)
                } catch (e: Exception) {
                    Snackbar.make(findViewById(android.R.id.content), "Error", Snackbar.LENGTH_LONG)
                        .show()
                    logError(TAG, "Error", e)
                } finally {
                    progressBar.visibility = View.GONE
                }

            }.create().show()
        }


        backup_button_restore.setOnClickListener {
            builder.setMessage(getString(R.string.readyToRestore)).setPositiveButton("OK") { _, _ ->
                progressBar.visibility = View.VISIBLE
                try {

                    val result: Boolean = LocalBackup(this).performRestore()

                    if (result) {

                        rebootApp()

                    } else {
                        Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.backup_restore_nothing),
                            Snackbar.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Snackbar.make(findViewById(android.R.id.content), "Error", Snackbar.LENGTH_LONG)
                        .show()
                    logError(TAG, "Error", e)
                } finally {
                    MyApp.database = MyApp.initDatabase(this)
                    progressBar.visibility = View.GONE
                }

            }.create().show()

        }



        backup_button_backup_gdrive.setOnClickListener {


            progressBar.visibility = View.VISIBLE

            upload()


        }


        backup_button_restore_gdrive.setOnClickListener {
            builder.setMessage(getString(R.string.readyToRestoreGdrive))
                .setPositiveButton("OK") { _, _ ->
                    progressBar.visibility = View.VISIBLE

                    restore()


                }.create().show()

        }

        signIn()

    }

    private fun rebootApp() {
        Snackbar.make(findViewById(android.R.id.content),
            getString(R.string.backup_restore_success),
            Snackbar.LENGTH_LONG).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val mgr = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)

        this.finish()
        System.exit(2)
    }


    private fun signIn() {

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            driveManager = DriveManager.getInstance(this)
        } else {
            val googleSignInClient = buildGoogleSignInClient()
            startActivityForResult(googleSignInClient.signInIntent,
                Permissions.REQUEST_CODE_SIGN_IN)
        }
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))
            .requestEmail().build()
        return GoogleSignIn.getClient(this, signInOptions)
    }

    private fun upload() {
        GlobalScope.launch(Dispatchers.IO) {

            var resultString = ""
            if (driveManager != null) {
                val zipFile = ZipHelper.zip(appFiles.toTypedArray(),
                    this@BackupActivity.filesDir.path,
                    BACKUP_FILE_NAME)

                if (zipFile) {
                    try {
                        driveManager!!.upload(File(filesDir, BACKUP_FILE_NAME),
                            "application/zip",
                            BACKUP_FILE_NAME + "-${Date().formatToServerDateDefaults()}")
                        resultString = resources.getString(R.string.activity_backup_gdrive_success)
                    } catch (e: IOException) {
                        resultString = resources.getString(R.string.activity_backup_gdrive_fail_upload)
                    }

                    FileUtils.deleteFile(this@BackupActivity.filesDir.path, BACKUP_FILE_NAME)


                } else {
                    resultString = resources.getString(R.string.activity_backup_gdrive_fail_create_zip)

                }
            } else {
                resultString = resources.getString(R.string.activity_backup_auth_failed)
            }

            withContext(Dispatchers.Main) {
                Snackbar.make(findViewById(android.R.id.content),
                    resultString,
                    Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun restore() {
        GlobalScope.launch(Dispatchers.IO) {

            var resultString = ""

            if (driveManager != null) {
                try {
                    val allFiles = driveManager?.query(BACKUP_FILE_NAME)

                    if (allFiles != null && allFiles.any()) {

                        val file = allFiles.last()


                        driveManager?.download(file.id,
                            this@BackupActivity.filesDir.path + "/RanobeReader-temp.zip")

                        val downloadFile = File(this@BackupActivity.filesDir.path,
                            "RanobeReader-temp.zip")

                        if (downloadFile.exists() && downloadFile.isFile) {
                            MyApp.database.close()
                            ZipHelper.unzip(this@BackupActivity.filesDir.path + "/RanobeReader-temp.zip",
                                "${filesDir.path}/../")


                            File(getDatabasePath(DB_NAME).parent, "$DB_NAME-shm").delete()

                            File(getDatabasePath(DB_NAME).parent, "$DB_NAME-wal").delete()


                            // Close the streams
                            downloadFile.delete()

                            withContext(Dispatchers.Main) {
                                rebootApp()
                            }
                        } else {
                            resultString = resources.getString(R.string.activity_backup_gdrive_fail_download)
                        }

                    } else {
                        resultString = resources.getString(R.string.activity_backup_gdrive_fail_not_found)
                    }
                } catch (e: IOException) {

                    resultString = resources.getString(R.string.activity_backup_gdrive_fail_download)

                }

            } else {
                resultString = resources.getString(R.string.activity_backup_auth_failed)
            }

            withContext(Dispatchers.Main) {
                Snackbar.make(findViewById(android.R.id.content),
                    resultString,
                    Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }

        }
    }

    private fun find() {
        GlobalScope.launch(Dispatchers.IO) {
            val files = driveManager?.query(BACKUP_FILE_NAME)
            files?.forEach {}
        }
    }

    private fun delete() {
        GlobalScope.launch(Dispatchers.IO) {
            val files = driveManager?.query(BACKUP_FILE_NAME)
            files?.forEach { file ->
                driveManager?.delete(file)
            }
        }
    }


    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onDestroy() {

        Thread.setDefaultUncaughtExceptionHandler(null)
        super.onDestroy()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            return
        }

        when (requestCode) {
            FilePickerConst.REQUEST_CODE_DOC -> if (resultCode == Activity.RESULT_OK) {
                // docPaths = ArrayList()
                // docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS))
            }

            Permissions.REQUEST_CODE_SIGN_IN -> {
                if (resultCode == Activity.RESULT_OK) {
                    driveManager = DriveManager.getInstance(this)
                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                        resources.getString(R.string.activity_backup_auth_failed),
                        Snackbar.LENGTH_LONG).show()
                }
            }

        }


    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {

            AlertDialog.Builder(this@BackupActivity)
                .setTitle(getString(R.string.alert_perms_denied))
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setMessage(getString(R.string.alert_perms_denied_message)).setCancelable(true)
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                .setPositiveButton("OK") { _, _ ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }.create().show()


        }
    }


}