package ru.profapp.ranobe.backup

import android.Manifest

object Permissions {

    const val RC_STORAGE = 101
    const val REQUEST_CODE_SIGN_IN = 102

    // Storage Permissions variables
    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)


}
