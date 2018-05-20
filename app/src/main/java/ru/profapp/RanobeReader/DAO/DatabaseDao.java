package ru.profapp.RanobeReader.DAO;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;
import ru.profapp.RanobeReader.Models.TextChapter;

@android.arch.persistence.room.Database(entities = {Ranobe.class,
        Chapter.class, TextChapter.class}, version = 2, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class DatabaseDao extends RoomDatabase {

    private static final String DB_NAME = "DatabaseDao.db";
    private static final Migration FROM_2_TO_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull final SupportSQLiteDatabase database) {
            // database.execSQL("DROP TABLE IF EXISTS ranobe");
             // database.execSQL("CREATE TABLE notify");
        }

    };
    private static volatile DatabaseDao instance;

    public static synchronized DatabaseDao getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static DatabaseDao create(final Context context) {
        return Room.databaseBuilder(
                context,
                DatabaseDao.class,
                DB_NAME)
                //.addMigrations(FROM_2_TO_3)
                .build();
    }

    public abstract RanobeDao getRanobeDao();

    public abstract ChapterDao getChapterDao();

    public abstract TextDao getTextDao();

}

