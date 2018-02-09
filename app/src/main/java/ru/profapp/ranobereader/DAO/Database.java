package ru.profapp.ranobereader.DAO;

/**
 * Created by Ruslan on 09.02.2018.
 */

import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import ru.profapp.ranobereader.Models.Chapter;
import ru.profapp.ranobereader.Models.Ranobe;

@android.arch.persistence.room.Database(entities = {Ranobe.class, Chapter.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class Database extends RoomDatabase {

    private static final String DB_NAME = "Database.db";
    private static volatile Database instance;

    static synchronized Database getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static Database create(final Context context) {
        return Room.databaseBuilder(
                context,
                Database.class,
                DB_NAME).build();
    }

    public abstract RanobeDao getRanobeDao();

    public abstract ChapterDao getChapterDao();
}

//    To add things to the database we need to invoke:
//
//        RepoDatabase
//        .getInstance(context)
//        .getRepoDao()
//        .insert(new Repo(1, "Cool Repo Name", "url"));
//
//        Getting things is also pretty simple:
//
//        List<Repo> allRepos = RepoDatabase
//        .getInstance(MainActivity.this)
//        .getRepoDao()
//        .getAllRepos();