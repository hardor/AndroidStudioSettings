package ru.profapp.ranobe

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.MobileAds
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import io.fabric.sdk.android.Fabric
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.dagger.ApplicationComponent
import ru.profapp.ranobe.dagger.DaggerApplicationComponent
import ru.profapp.ranobe.dagger.PreferencesModule
import ru.profapp.ranobe.dao.DatabaseDao
import ru.profapp.ranobe.helpers.LogType
import ru.profapp.ranobe.helpers.StethoUtils
import ru.profapp.ranobe.helpers.logError
import ru.profapp.ranobe.helpers.logMessage
import ru.profapp.ranobe.models.Ranobe
import ru.profapp.ranobe.pref.GeneralPreferencesManager

class MyApp : MultiDexApplication() {

    companion object {

        val TAG = "MyAPP"

        val component: ApplicationComponent = DaggerApplicationComponent.create()

        var fragmentType: Constants.FragmentType? = null

        const val DB_NAME = "DatabaseDao.db"
        @VisibleForTesting
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //Change ranobe
                try {
                    database.execSQL("CREATE TABLE chapter_temp ( Url TEXT NOT NULL DEFAULT ''," + " RanobeUrl TEXT NOT NULL DEFAULT ''," + " Title TEXT NOT NULL DEFAULT ''," + " Status TEXT, " + " CanRead INTEGER NOT NULL DEFAULT (1)," + " New INTEGER NOT NULL DEFAULT (0)," + " [Index] INTEGER NOT NULL DEFAULT (0)," + " Time INTEGER," + " RanobeId INTEGER," + " Downloaded INTEGER NOT NULL DEFAULT (0)," + " IsRead INTEGER NOT NULL DEFAULT (0)," + " RanobeName TEXT NOT NULL DEFAULT ''," + " Id INTEGER, " + " PRIMARY KEY(Url) );")

                    database.execSQL("INSERT INTO chapter_temp ( Url, RanobeUrl, Title, Status, CanRead, New, [Index], Time, RanobeId, Downloaded, IsRead, RanobeName, Id) SELECT Url, RanobeUrl, Title, Status, CanRead, New, [Index], Time, RanobeId, Downloaded, Readed, RanobeName,Id FROM chapter;")
                    database.execSQL("CREATE TABLE ranobe2 (Url TEXT NOT NULL, Id INTEGER, EngTitle TEXT, Title TEXT NOT NULL, Image TEXT, ReadyDate INTEGER, Lang TEXT, Description TEXT, AdditionalInfo TEXT, RanobeSite TEXT NOT NULL, ChapterCount INTEGER, LastReadChapter INTEGER,  IsFavorite INTEGER NOT NULL, IsFavoriteInWeb INTEGER NOT NULL, Rating TEXT, Status TEXT, PRIMARY KEY(Url));")
                    database.execSQL("INSERT INTO ranobe2 (Url,Id,EngTitle,Title,Image,ReadyDate,Lang,Description,AdditionalInfo,RanobeSite,ChapterCount,LastReadChapter,IsFavorite,IsFavoriteInWeb,Rating,Status) SELECT Url,Id,EngTitle,IFNULL(Title,''),Image,ReadyDate,Lang,Description,AdditionalInfo,RanobeSite,CharpterCount,LastReadedCharpter,Favorited,FavoritedInWeb,Rating,Status FROM ranobe;")
                    database.execSQL("DROP TABLE ranobe;")
                    database.execSQL("ALTER TABLE ranobe2 RENAME TO ranobe;")
                    database.execSQL("CREATE TABLE chapter2 ( Url TEXT NOT NULL, RanobeUrl TEXT NOT NULL, Title TEXT NOT NULL, Status TEXT,  CanRead INTEGER NOT NULL, New INTEGER NOT NULL, [Index] INTEGER NOT NULL, Time INTEGER, RanobeId INTEGER, Downloaded INTEGER NOT NULL, IsRead INTEGER NOT NULL, RanobeName TEXT NOT NULL, Id INTEGER,  PRIMARY KEY(Url), FOREIGN KEY(RanobeUrl) REFERENCES ranobe(Url) ON UPDATE NO ACTION ON DELETE CASCADE );")
                    database.execSQL("INSERT INTO chapter2 SELECT * FROM chapter_temp; ")
                    database.execSQL("DROP TABLE chapter;")
                    database.execSQL("DROP TABLE chapter_temp;")
                    database.execSQL("ALTER TABLE chapter2 RENAME TO chapter;")
                    database.execSQL("CREATE INDEX index_chapter_RanobeUrl ON chapter (RanobeUrl);")
                    database.execSQL("CREATE TABLE ranobeImage (RanobeUrl TEXT NOT NULL, Image TEXT, PRIMARY KEY(RanobeUrl));")
                    database.execSQL("CREATE INDEX index_ranobeImage_RanobeUrl ON ranobeImage (RanobeUrl);")
                    database.execSQL("CREATE TABLE chapterHistory (ChapterUrl TEXT NOT NULL, ChapterName TEXT NOT NULL, RanobeName TEXT NOT NULL,  RanobeUrl TEXT NOT NULL,[Index] INTEGER NOT NULL, ReadDate INTEGER NOT NULL, Progress REAL NOT NULL, PRIMARY KEY(ChapterUrl));")
                    database.execSQL("CREATE INDEX index_chapterHistory_ChapterUrl ON chapterHistory (ChapterUrl);")
                    database.execSQL("CREATE TABLE ranobeHistory (RanobeUrl TEXT NOT NULL, RanobeName TEXT NOT NULL, Description TEXT, ReadDate INTEGER NOT NULL, PRIMARY KEY(RanobeUrl))")
                    database.execSQL("CREATE INDEX index_ranobeHistory_RanobeUrl ON ranobeHistory (RanobeUrl)")
                    database.execSQL("CREATE TABLE IF NOT EXISTS textChapter2 (ChapterUrl TEXT NOT NULL, ChapterName TEXT NOT NULL, RanobeName TEXT NOT NULL,  RanobeUrl TEXT NOT NULL,Text TEXT NOT NULL,PRIMARY KEY(ChapterUrl));")
                    database.execSQL("DROP TABLE textChapter;")
                    database.execSQL("ALTER TABLE textChapter2 RENAME TO textChapter;")
                    database.execSQL("CREATE INDEX index_textChapter_ChapterUrl ON textChapter (ChapterUrl);")
                } catch (e: Exception) {
                    logError(TAG, "MIGRATION_2_3 failed", e)
                }

            }
        }
        @VisibleForTesting
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {

                logMessage(LogType.ERROR, "MIGRATION", "MIGRATION start")
                //Change ranobe
                try {
                    database.execSQL("CREATE TABLE chapterHistory_temp (ChapterUrl TEXT NOT NULL, ChapterName TEXT NOT NULL, RanobeName TEXT NOT NULL,  RanobeUrl TEXT NOT NULL,[Index] INTEGER NOT NULL, ReadDate INTEGER NOT NULL,  PRIMARY KEY(ChapterUrl));")

                    database.execSQL("CREATE TABLE chapterProgress (ChapterUrl TEXT NOT NULL, RanobeUrl TEXT NOT NULL, ReadDate INTEGER NOT NULL, Progress REAL NOT NULL,  PRIMARY KEY(ChapterUrl));")

                    database.execSQL("INSERT INTO chapterProgress (ChapterUrl, RanobeUrl, ReadDate, Progress) SELECT ChapterUrl, RanobeUrl, ReadDate, Progress FROM chapterHistory;")
                    database.execSQL("INSERT INTO chapterHistory_temp (ChapterUrl,ChapterName, RanobeName,RanobeUrl,[Index], ReadDate) SELECT ChapterUrl,ChapterName, RanobeName,RanobeUrl,[Index], ReadDate FROM chapterHistory;")

                    database.execSQL("DROP TABLE chapterHistory;")
                    database.execSQL("ALTER TABLE chapterHistory_temp RENAME TO chapterHistory;")
                    database.execSQL("CREATE INDEX index_chapterProgress_ChapterUrl ON chapterProgress (ChapterUrl);")
                    database.execSQL("CREATE INDEX index_chapterHistory_ChapterUrl ON chapterHistory (ChapterUrl);")
                } catch (e: Exception) {
                    logError(TAG, "MIGRATION_3_4 failed", e)
                }

            }
        }
        @VisibleForTesting
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {

                logMessage(LogType.ERROR, "MIGRATION", "MIGRATION start")
                //Change ranobe
                try {
                    database.execSQL("CREATE TABLE IF NOT EXISTS textChapter2 (ChapterUrl TEXT NOT NULL, ChapterName TEXT NOT NULL, RanobeName TEXT NOT NULL,  RanobeUrl TEXT NOT NULL,Text TEXT NOT NULL,ChapterIndex INTEGER NOT NULL,PRIMARY KEY(ChapterUrl));")
                    database.execSQL("INSERT INTO textChapter2 (ChapterUrl,ChapterName, RanobeName,RanobeUrl,Text,ChapterIndex) SELECT ChapterUrl,ChapterName, RanobeName,RanobeUrl,Text,0 FROM textChapter;")
                    database.execSQL("DROP TABLE textChapter;")
                    database.execSQL("ALTER TABLE textChapter2 RENAME TO textChapter;")
                    database.execSQL("CREATE INDEX index_textChapter_ChapterUrl ON textChapter (ChapterUrl);")
                } catch (e: Exception) {
                    logError(TAG, "MIGRATION_4_5 failed", e)
                }

            }
        }

        lateinit var database: DatabaseDao
        var ranobe: Ranobe? = null
        var refWatcher: RefWatcher? = null

        var isApplicationInitialized: Boolean = false
        //        var hidePaymentChapter: Boolean= false

        lateinit var preferencesManager: GeneralPreferencesManager

        fun initDatabase(context: Context): DatabaseDao {
            return Room.databaseBuilder(context, DatabaseDao::class.java, DB_NAME)
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration().build()
        }

    }

    override fun onCreate() {
        super.onCreate()

        component.inject(this)
        preferencesManager = PreferencesModule(this).provideGeneralPreferencesManager()
        MobileAds.initialize(applicationContext, getString(R.string.app_admob_id))
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return
            }
            refWatcher = LeakCanary.install(this)
            StethoUtils.install(this)

        }

        Fabric.with(applicationContext, Crashlytics())

        MyApp.database = initDatabase(this)
    }
}


