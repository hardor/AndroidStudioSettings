package ru.profapp.RanobeReader.Models

import com.google.android.gms.drive.DriveId

import java.util.Date

class BackupItem(var driveId: DriveId?, var modifiedDate: Date?, var backupSize: Long)