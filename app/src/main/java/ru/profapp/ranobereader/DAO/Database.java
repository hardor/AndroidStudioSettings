package ru.profapp.ranobereader.DAO;

/**
 * Created by Ruslan on 09.02.2018.
 */

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import ru.profapp.ranobereader.Models.Chapter;
import ru.profapp.ranobereader.Models.Ranobe;

@android.arch.persistence.room.Database(entities = {Ranobe.class,
        Chapter.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class Database extends RoomDatabase {

    private static final String DB_NAME = "Database.db";
    private static final Migration FROM_1_TO_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull final SupportSQLiteDatabase database) {
           // database.execSQL("DROP TABLE IF EXISTS ranobe");
          //  database.execSQL("CREATE TABLE IF NOT EXISTS");
        }


    };
    private static volatile Database instance;

    public static synchronized Database getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static Database create(final Context context) {
        return Room.databaseBuilder(
                context,
                Database.class,
                DB_NAME)
                .addMigrations(FROM_1_TO_2)
                .build();
    }

    public abstract RanobeDao getRanobeDao();

    public abstract ChapterDao getChapterDao();

    @Override
    public void close() {
        super.close();
    }
}

//    To add things to the database we need to invoke:
//

//        Getting things is also pretty simple:
//
//        List<Repo> allRepos = RepoDatabase
//        .getInstance(MainActivity.this)
//        .getRepoDao()
//        .getAllRepos();