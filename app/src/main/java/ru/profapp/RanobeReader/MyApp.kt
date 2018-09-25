package ru.profapp.RanobeReader

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.profapp.RanobeReader.DAO.DatabaseDao
import ru.profapp.RanobeReader.Models.Ranobe


class MyApp : Application() {


    companion object {
        const val DB_NAME = "DatabaseDao.db"
        @VisibleForTesting
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //Change ranobe
                try {
                    database.execSQL("CREATE TABLE chapter_temp ( Url TEXT NOT NULL DEFAULT ''," +
                            " RanobeUrl TEXT NOT NULL DEFAULT ''," +
                            " Title TEXT NOT NULL DEFAULT ''," +
                            " Status TEXT, " +
                            " CanRead INTEGER NOT NULL DEFAULT (1)," +
                            " New INTEGER NOT NULL DEFAULT (0)," +
                            " [Index] INTEGER NOT NULL DEFAULT (0)," +
                            " Time INTEGER," +
                            " RanobeId INTEGER," +
                            " Downloaded INTEGER NOT NULL DEFAULT (0)," +
                            " IsRead INTEGER NOT NULL DEFAULT (0)," +
                            " RanobeName TEXT NOT NULL DEFAULT ''," +
                            " Id INTEGER, " +
                            " PRIMARY KEY(Url) );"
                            )

                    database.execSQL("INSERT INTO chapter_temp ( Url, RanobeUrl, Title, Status, CanRead, New, [Index], Time, RanobeId, Downloaded, IsRead, RanobeName, Id) SELECT Url, RanobeUrl, Title, Status, CanRead, New, [Index], Time, RanobeId, Downloaded, Readed, RanobeName,Id FROM chapter;")
                    database.execSQL("CREATE TABLE ranobe2 (Url TEXT NOT NULL, Id INTEGER, EngTitle TEXT, Title TEXT NOT NULL, Image TEXT, ReadyDate INTEGER, Lang TEXT, Description TEXT, AdditionalInfo TEXT, RanobeSite TEXT NOT NULL, ChapterCount INTEGER, LastReadChapter INTEGER, WasUpdated INTEGER NOT NULL, IsFavorite INTEGER NOT NULL, IsFavoriteInWeb INTEGER NOT NULL, Rating TEXT, Status TEXT, PRIMARY KEY(Url));")
                    database.execSQL("INSERT INTO ranobe2 SELECT * FROM ranobe;")
                    database.execSQL("DROP TABLE ranobe;")
                    database.execSQL("ALTER TABLE ranobe2 RENAME TO ranobe;")
                    database.execSQL("CREATE TABLE chapter2 ( Url TEXT NOT NULL, RanobeUrl TEXT NOT NULL, Title TEXT NOT NULL, Status TEXT,  CanRead INTEGER NOT NULL, New INTEGER NOT NULL, [Index] INTEGER NOT NULL, Time INTEGER, RanobeId INTEGER, Downloaded INTEGER NOT NULL, IsRead INTEGER NOT NULL, RanobeName TEXT NOT NULL, Id INTEGER,  PRIMARY KEY(Url), FOREIGN KEY(RanobeUrl) REFERENCES ranobe(Url) ON UPDATE NO ACTION ON DELETE CASCADE );")
                    database.execSQL("INSERT INTO chapter2 SELECT * FROM chapter_temp; ")
                    database.execSQL("DROP TABLE chapter;")
                    database.execSQL("DROP TABLE chapter_temp;")
                    database.execSQL("ALTER TABLE chapter2 RENAME TO chapter;")
                    database.execSQL("CREATE INDEX index_chapter_RanobeUrl ON chapter (RanobeUrl);")
                    database.execSQL("CREATE TABLE ranobeImage (RanobeUrl TEXT NOT NULL, Image TEXT, PRIMARY KEY(RanobeUrl));")
                    database.execSQL( "CREATE INDEX index_ranobeImage_RanobeUrl ON ranobeImage (RanobeUrl);")
                    database.execSQL("CREATE TABLE chapterHistory (ChapterUrl TEXT NOT NULL, ChapterName TEXT NOT NULL, RanobeName TEXT NOT NULL, [Index] INTEGER NOT NULL, ReadDate INTEGER NOT NULL, Progress REAL NOT NULL, PRIMARY KEY(ChapterUrl));")
                    database.execSQL("CREATE INDEX index_chapterHistory_ChapterUrl ON chapterHistory (ChapterUrl);")
                    database.execSQL("CREATE TABLE IF NOT EXISTS `textChapter2` (`ChapterUrl` TEXT NOT NULL, `ChapterName` TEXT NOT NULL, `RanobeName` TEXT NOT NULL, `Text` TEXT NOT NULL, `Index` INTEGER NOT NULL, PRIMARY KEY(`ChapterUrl`));")
                    database.execSQL("INSERT INTO textChapter2 SELECT * FROM textChapter;")
                    database.execSQL("DROP TABLE textChapter;")
                    database.execSQL("ALTER TABLE textChapter2 RENAME TO textChapter;")
                    database.execSQL("CREATE INDEX index_textChapter_ChapterUrl ON textChapter (ChapterUrl);")
                }catch (e:Exception ){
                    e.printStackTrace()
                }


//                database.execSQL("-- Таблица: chapter" +
//                        "--DROP TABLE IF EXISTS chapter;" +
//                        "--CREATE TABLE [chapter] ([Url] TEXT NOT NULL, [RanobeUrl] TEXT, [Id] INTEGER NOT NULL, [Title] TEXT, [Status] TEXT, [CanRead] INTEGER, [New] INTEGER, [Index] INTEGER NOT NULL, [Time] INTEGER, [RanobeId] INTEGER NOT NULL, [Downloaded] INTEGER, [Readed] INTEGER, [Text] TEXT, [RanobeName] TEXT, PRIMARY KEY([Url]), FOREIGN KEY([RanobeUrl]) REFERENCES [ranobe`([Url]) ON UPDATE NO ACTION ON DELETE CASCADE );" +
//                        "" +
//                        "-- Таблица: ranobe" +
//                        "--DROP TABLE IF EXISTS ranobe;" +
//                        "--CREATE TABLE [ranobe] ([Url] TEXT NOT NULL, [Id] INTEGER NOT NULL, [EngTitle] TEXT, [Title] TEXT, [Image] TEXT, [ReadyDate] INTEGER, [Lang] TEXT, [Description] TEXT, [AdditionalInfo] TEXT, [RanobeSite] TEXT, [CharpterCount] INTEGER NOT NULL, [LastReadedCharpter] INTEGER NOT NULL, [WasUpdated] INTEGER, [Favorited] INTEGER, [FavoritedInWeb] INTEGER, [Rating] TEXT, [Status] TEXT, PRIMARY KEY([Url]));" +
//                        "" +
//                        "-- Таблица: textChapter" +
//                        "--DROP TABLE IF EXISTS textChapter;" +
//                        "--CREATE TABLE [textChapter] ([ChapterUrl] TEXT NOT NULL, [ChapterName] TEXT, [RanobeName] TEXT, [Text] TEXT, [Index] INTEGER NOT NULL, PRIMARY KEY([ChapterUrl]));" +
//                        "" +
//                        "-- Индекс: index_chapter_RanobeUrl" +
//                        "--DROP INDEX IF EXISTS index_chapter_RanobeUrl;" +
//                        "--CREATE INDEX [index_chapter_RanobeUrl] ON [chapter] ([RanobeUrl]);" +
//                        "" +
//                        "------Migration" +
//                        "------Change chapter" +
//                        "CREATE TABLE  [chapter_temp] ( Url        TEXT    NOT NULL                       DEFAULT ''," +
//                        "    RanobeUrl  TEXT    NOT NULL                       DEFAULT ''," +
//                        "    Title      TEXT    NOT NULL                       DEFAULT ''," +
//                        "    Status     TEXT,   " +
//                        "    CanRead    INTEGER NOT NULL                       DEFAULT (1)," +
//                        "    [New]      INTEGER NOT NULL                       DEFAULT (0)," +
//                        "    [Index]    INTEGER NOT NULL                       DEFAULT (0)," +
//                        "    [Time]     INTEGER," +
//                        "    RanobeId   INTEGER," +
//                        "    Downloaded INTEGER NOT NULL                       DEFAULT (0)," +
//                        "    IsRead     INTEGER NOT NULL                       DEFAULT (0)," +
//                        "    RanobeName TEXT    NOT NULL                       DEFAULT ''," +
//                        "    Id         INTEGER, " +
//                        "    PRIMARY KEY([Url]) );" +
//                        "INSERT INTO chapter_temp  (" +
//                        "                         Url," +
//                        "                         RanobeUrl," +
//                        "                         Title," +
//                        "                         Status," +
//                        "                         CanRead," +
//                        "                         [New]," +
//                        "                         [Index]," +
//                        "                         [Time]," +
//                        "                         RanobeId," +
//                        "                         Downloaded," +
//                        "                         IsRead," +
//                        "                         RanobeName," +
//                        "                         Id" +
//                        "                     )    SELECT Url, RanobeUrl, Title, Status, CanRead, New, [Index], Time, RanobeId, Downloaded, Readed, RanobeName,Id FROM 'chapter';                   " +
//                        "" +
//                        "------Change ranobe" +
//                        "CREATE TABLE  [ranobe2] ([Url] TEXT NOT NULL, [Id] INTEGER, [EngTitle] TEXT, [Title] TEXT NOT NULL, [Image] TEXT, [ReadyDate] INTEGER, [Lang] TEXT, [Description] TEXT, [AdditionalInfo] TEXT, [RanobeSite] TEXT NOT NULL, [ChapterCount] INTEGER, [LastReadChapter] INTEGER, [WasUpdated] INTEGER NOT NULL, [IsFavorite] INTEGER NOT NULL, [IsFavoriteInWeb] INTEGER NOT NULL, [Rating] TEXT, [Status] TEXT, PRIMARY KEY([Url]));" +
//                        "INSERT INTO ranobe2 SELECT * FROM ranobe;" +
//                        "DROP TABLE ranobe;" +
//                        "ALTER TABLE ranobe2 RENAME TO ranobe;" +
//                        "--------------" +
//                        "" +
//                        "CREATE TABLE  [chapter2] ( Url        TEXT    NOT NULL," +
//                        "    RanobeUrl  TEXT    NOT NULL," +
//                        "    Title      TEXT    NOT NULL," +
//                        "    Status     TEXT,   " +
//                        "    CanRead    INTEGER NOT NULL," +
//                        "    [New]      INTEGER NOT NULL," +
//                        "    [Index]    INTEGER NOT NULL," +
//                        "    [Time]     INTEGER," +
//                        "    RanobeId   INTEGER," +
//                        "    Downloaded INTEGER NOT NULL," +
//                        "    IsRead     INTEGER NOT NULL," +
//                        "    RanobeName TEXT    NOT NULL," +
//                        "    Id         INTEGER, " +
//                        "    PRIMARY KEY([Url])," +
//                        "    FOREIGN KEY([RanobeUrl])    REFERENCES [ranobe]([Url]) ON UPDATE NO ACTION ON DELETE CASCADE );" +
//                        "INSERT INTO chapter2  SELECT * FROM chapter_temp;                   " +
//                        "DROP TABLE chapter;" +
//                        "DROP TABLE chapter_temp;" +
//                        "ALTER TABLE chapter2 RENAME TO chapter;" +
//                        "" +
//                        "" +
//                        "" +
//                        "------Add new" +
//                        "--DROP INDEX IF EXISTS index_textChapter_ChapterUrl;" +
//                        "CREATE  INDEX [index_textChapter_ChapterUrl] ON [textChapter] ([ChapterUrl]);" +
//                        "" +
//                        "-- Таблица: ranobeImage" +
//                        "--DROP TABLE IF EXISTS ranobeImage;" +
//                        "CREATE TABLE  [ranobeImage] ([RanobeUrl] TEXT NOT NULL, [Image] TEXT, PRIMARY KEY([RanobeUrl]));" +
//                        "" +
//                        "--DROP INDEX IF EXISTS index_ranobeImage_RanobeUrl;" +
//                        "CREATE  INDEX [index_ranobeImage_RanobeUrl] ON [ranobeImage] ([RanobeUrl]);" +
//                        "" +
//                        "-- Таблица: chapterHistory" +
//                        "--DROP TABLE IF EXISTS chapterHistory;" +
//                        "CREATE TABLE [chapterHistory] ([ChapterUrl] TEXT NOT NULL, [ChapterName] TEXT NOT NULL, [RanobeName] TEXT NOT NULL, [Index] INTEGER NOT NULL, [ReadDate] INTEGER NOT NULL, [Progress] REAL NOT NULL, PRIMARY KEY([ChapterUrl]));" +
//                        "--DROP INDEX IF EXISTS index_chapterHistory_ChapterUrl;" +
//                        "CREATE  INDEX [index_chapterHistory_ChapterUrl] ON [chapterHistory] ([ChapterUrl]);")

            }
        }
        var database: DatabaseDao? = null
        var ranobe: Ranobe? = null
    }

    override fun onCreate() {
        super.onCreate()
        MyApp.database = Room.databaseBuilder(this, DatabaseDao::class.java, DB_NAME).addMigrations(MIGRATION_2_3).build()
    }
}


