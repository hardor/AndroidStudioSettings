package ru.profapp.ranobe.backup

import com.google.android.material.snackbar.Snackbar
import pub.devrel.easypermissions.EasyPermissions
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.activities.BackupActivity
import ru.profapp.ranobe.helpers.ZipHelper
import java.io.File


class LocalBackup(private val activity: BackupActivity) {

    //ask to the user a name for the backup and perform it. The backup will be saved to a custom folder.
    fun performBackup(appFiles: MutableList<String>, destPath:String) {
        val perms = Permissions.PERMISSIONS_STORAGE

        val permission = EasyPermissions.hasPermissions(activity, *perms)

        if (!permission) {
            EasyPermissions.requestPermissions(activity,
                activity.resources.getString(R.string.write_and_read_rationale),
                Permissions.RC_STORAGE,
                *perms)
        } else {

            ZipHelper.zip(appFiles.toTypedArray(),
                destPath,
                "RanobeReaderBackup.zip")
            Snackbar.make(activity.findViewById(android.R.id.content),
                activity.resources.getString(R.string.activity_backup_success),
                Snackbar.LENGTH_LONG).show()

        }

    }

    //ask to the user what backup to restore
    fun performRestore(destPath: String): Boolean {

        val perms = Permissions.PERMISSIONS_STORAGE

        val permission = EasyPermissions.hasPermissions(activity, *perms)

        if (!permission) {
            EasyPermissions.requestPermissions(activity,
                activity.resources.getString(R.string.write_and_read_rationale),
                Permissions.RC_STORAGE,
                *perms)
        } else {


            val zipFile = File(destPath)

            if (zipFile.isFile && zipFile.canRead()) {
                MyApp.database.close()
                ZipHelper.unzip(zipFile.absolutePath, "${activity.filesDir.path}/../")


                File(activity.getDatabasePath(MyApp.DB_NAME).parent,
                    "${MyApp.DB_NAME}-shm").delete()

                File(activity.getDatabasePath(MyApp.DB_NAME).parent,
                    "${MyApp.DB_NAME}-wal").delete()


                return true
            }
        }
        return false

    }

    companion object {
        private const val TAG = "Local Backup"
    }
}
