package ru.profapp.RanobeReader


import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.DAO.DatabaseDao
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp.Companion.MIGRATION_2_3
import java.io.IOException
import java.util.*


@RunWith(AndroidJUnit4::class)
class MigrationTest {


    // Helper for creating Room databases and migrations
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            Objects.requireNonNull(DatabaseDao::class.java.canonicalName),
            FrameworkSQLiteOpenHelperFactory())

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 2)

        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        //db.execSQL(...);

        db.close()

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3);


    }

    companion object {
        private val TEST_DB = "test-db"
        private val RANOBE = Ranobe(Constants.RanobeSite.RanobeRf)
    }

}
