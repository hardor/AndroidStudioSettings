package ru.profapp.RanobeReader

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.profapp.RanobeReader.DAO.DatabaseDao
import ru.profapp.RanobeReader.Models.Ranobe



class MyApp: Application() {


    companion object {
        const val DB_NAME = "DatabaseDao.db"
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER, " + "`name` TEXT, PRIMARY KEY(`id`))")
            }
        }
        var database: DatabaseDao? = null
        var ranobe: Ranobe? = null
    }

    override fun onCreate() {
        super.onCreate()
        MyApp.database =  Room.databaseBuilder(this, DatabaseDao::class.java, DB_NAME).addMigrations(MIGRATION_2_3).build()
    }
}
