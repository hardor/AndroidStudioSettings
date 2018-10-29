package ru.profapp.RanobeReader.Activities

import android.Manifest
import android.Manifest.permission
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import ru.profapp.RanobeReader.Common.Constants.Ranoberf_Login_Pref
import ru.profapp.RanobeReader.Common.Constants.Rulate_Login_Pref
import ru.profapp.RanobeReader.Common.Constants.last_chapter_id_Pref
import ru.profapp.RanobeReader.Common.MyExceptionHandler
import ru.profapp.RanobeReader.DAO.DatabaseDao
import ru.profapp.RanobeReader.Helpers.LogHelper
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.MyApp.Companion.DB_NAME
import ru.profapp.RanobeReader.R
import ru.profapp.RanobeReader.Utils.FileUtils
import java.io.File


class BackupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        setContentView(R.layout.activity_backup)
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))
        val backupButton = findViewById<Button>(R.id.backup_button_backup)
        backupButton.setOnClickListener {
            backupRestore(true)
        }

        val restoreButton = findViewById<Button>(R.id.backup_button_restore)
        restoreButton.setOnClickListener {
            backupRestore(false)
        }
    }

    private fun backupRestore(isBuckup: Boolean) {
        val permission = ActivityCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE)
        if (permission == PackageManager.PERMISSION_GRANTED) {

            MyApp.database.close()

            val db = getDatabasePath(DB_NAME)
            //    val dbShm = File(db.parent, "$DB_NAME-shm")
            //   val dbWal = File(db.parent, "$DB_NAME-wal")

            // val prefLastChapterId = File("/data/data/" + packageName + "/shared_prefs/$last_chapter_id_Pref.xml")
            val prefLastChapterId = File("${filesDir.path}/../shared_prefs/$last_chapter_id_Pref.xml")
            val prefRulateLogin = File("${filesDir.path}/../shared_prefs/$Rulate_Login_Pref.xml")
            val prefRanobeRfLogin = File("${filesDir.path}/../shared_prefs/$Ranoberf_Login_Pref.xml")

            val folder = File("${Environment.getExternalStorageDirectory().path}/RanobeReaderBackup/")
            if (!folder.exists())
                folder.mkdirs()

            val db2 = File(folder, DB_NAME)
            //   val dbShm2 = File(folder, "$DB_NAME-shm")
            //    val dbWal2 = File(folder, "$DB_NAME-wal")

            val prefLastChapterId2 = File(folder, "$last_chapter_id_Pref.xml")
            val prefRulateLogin2 = File(folder, "$Rulate_Login_Pref.xml")
            val prefRanobeRfLogin2 = File(folder, "$Ranoberf_Login_Pref.xml")

            try {
                if (isBuckup) {
                    val builder = AlertDialog.Builder(this@BackupActivity)
                    builder.setTitle(getString(R.string.backup_restore))
                            .setMessage(getString(R.string.readyToBackup))
                            .setIcon(R.drawable.ic_info_black_24dp)
                            .setCancelable(true)
                            .setNegativeButton("Cancel") { dialog, id1 -> dialog.cancel() }
                            .setPositiveButton("OK") { dialog, id1 ->
                                FileUtils.copyFile(db, db2)
                                //    FileUtils.copyFile(dbShm, dbShm2)
                                //      FileUtils.copyFile(dbWal, dbWal2)
                                FileUtils.copyFile(prefLastChapterId, prefLastChapterId2)
                                FileUtils.copyFile(prefRulateLogin, prefRulateLogin2)
                                FileUtils.copyFile(prefRanobeRfLogin, prefRanobeRfLogin2)
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.activity_backup_success), Snackbar.LENGTH_LONG).show()
                            }

                    val alert = builder.create()
                    alert.show()

                } else {
                    val builder = AlertDialog.Builder(this@BackupActivity)
                    builder.setTitle(getString(R.string.backup_restore))
                            .setMessage(getString(R.string.readyToRestore))
                            .setIcon(R.drawable.ic_info_black_24dp)
                            .setCancelable(true)
                            .setNegativeButton("Cancel") { dialog, id1 -> dialog.cancel() }
                            .setPositiveButton("OK") { dialog, id1 ->
                                val res1 = FileUtils.copyFile(db2, db)
                                val res2 = FileUtils.copyFile(prefLastChapterId2, prefLastChapterId)
                                val res3 = FileUtils.copyFile(prefRulateLogin2, prefRulateLogin)
                                val res4 = FileUtils.copyFile(prefRanobeRfLogin2, prefRanobeRfLogin)
                                if (res1 || res2 || res3 || res4) {
                                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.backup_restore_success), Snackbar.LENGTH_LONG).show()


                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            or Intent.FLAG_ACTIVITY_NEW_TASK)

                                    val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

                                    val mgr = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)

                                    this.finish()
                                    System.exit(2)

                                } else {
                                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.backup_restore_nothing), Snackbar.LENGTH_LONG).show()
                                }
                            }

                    val alert = builder.create()
                    alert.show()

                }
            } catch (e: Exception) {
                Snackbar.make(findViewById(android.R.id.content), "Error", Snackbar.LENGTH_LONG).show()
                LogHelper.logError(LogHelper.LogType.ERROR, "SAVEDB", "Error", e, false)
            } finally {
                MyApp.database = Room.databaseBuilder(this, DatabaseDao::class.java, DB_NAME).addMigrations(MyApp.MIGRATION_2_3).build()
            }

        } else {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.allow_external_storage), Snackbar.LENGTH_LONG).setAction(getString(R.string.allow)) { view ->
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
            }.show()
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
}
