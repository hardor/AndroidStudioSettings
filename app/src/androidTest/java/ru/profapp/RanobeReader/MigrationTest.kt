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
    fun migrate2To3() {
        var db = helper.createDatabase(TEST_DB, 2)

        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.

        try {
            db.execSQL("INSERT INTO chapter (Url, RanobeUrl, Id, Title, Status, CanRead, New, \"Index\", Time, RanobeId, Downloaded, Readed, Text, RanobeName) VALUES ('tl.rulate.ru/book/3693/248127', 'tl.rulate.ru/book/3693', 248127, 'Том 5. Глава 1 - Победа над мелочью (Часть 2) Версия №2', '', 0, 1, 0, 0, 3693, 0, 0, NULL, 'Искатель подземелья');");
            db.execSQL("INSERT INTO chapter (Url, RanobeUrl, Id, Title, Status, CanRead, New, \"Index\", Time, RanobeId, Downloaded, Readed, Text, RanobeName) VALUES ('tl.rulate.ru/book/3693/248126', 'tl.rulate.ru/book/3693', 248126, 'Том 5. Глава 1 - Победа над мелочью (Часть 2) Версия №1', '', 0, 1, 1, 0, 3693, 0, 0, NULL, 'Искатель подземелья');")
            db.execSQL("INSERT INTO ranobe (Url, Id, EngTitle, Title, Image, ReadyDate, Lang, Description, AdditionalInfo, RanobeSite, CharpterCount, LastReadedCharpter, WasUpdated, Favorited, FavoritedInWeb, Rating, Status) VALUES ('tl.rulate.ru/book/4004', 4004, 'The New Gate', 'Новые Врата', 'http://tl.rulate.ru/i/book/17/3/23386.jpg', 1536089588000, 'с английского на русский', 'Статус: В работе Перевод: с английского на русский Количество глав: 41', NULL, 'tl.rulate.ru', 41, 0, 1, 0, 1, '4.7 (на основе 179 голосов)', 'В работе');")
            db.execSQL("INSERT INTO ranobe (Url, Id, EngTitle, Title, Image, ReadyDate, Lang, Description, AdditionalInfo, RanobeSite, CharpterCount, LastReadedCharpter, WasUpdated, Favorited, FavoritedInWeb, Rating, Status) VALUES ('https://xn--80ac9aeh6f.xn--p1ai/reincarnator/', 5, 'Reincarnator / Hwan Saeng Jwa / ???', 'Реинкарнатор', 'https://xn--80ac9aeh6f.xn--p1ai/statics/images/book/5/reinkarnator-1509702112.png', 1536426600000, NULL, 'Создав новый мир \"Бездна\", Бог заселил его новыми созданиями, а затем переместил в него всё человечество. И вот, спустя 50 лет, в живых осталось всего четыре человека, которые и выбрали одного, отправившегося в прошлое, чтобы спасти человечество. Таким образом, Кан Хансу вернулся на 25 лет назад, когда ему было двадцать и пришел его черед отправляться в Бездну. У него осталось всего пять лет до того момента, когда все человечество будет перенесено в этот дикий мир, в котором уже обитают далеко недружественные расы и существа.', 'Статус произведения: Завершено Статус перевода: Активен Количество глав: 489', 'https://xn--80ac9aeh6f.xn--p1ai', 0, 0, 1, 0, 1, 'Likes: 295, Dislikes: 43', NULL);")
            db.execSQL("INSERT INTO textChapter (ChapterUrl, ChapterName, RanobeName, Text, \"Index\") VALUES ('https://xn--80ac9aeh6f.xn--p1ai/glavniy-geroy-skryvaet-svoyu-silu/glava-103-posledovateli-bedstviya-chast-3/', 'Глава 103. Последователи Бедствия (часть 3)', 'Главный герой скрывает свою силу', '<p>Плавучий Дворец. Это было жилище Императора и сердце самой могущественной нации на континенте, человеческой Империи. Особый гость был приглашен в Плавучий Дворец лично Императором, и сейчас предстал перед всеми. <br></p><p>- Говори, Регрессор. </p><p>Император, сидевший на золотом троне, заговорил тихим голосом, молодая женщина, павшая перед ним ниц, наконец, подняла голову. Этой женщиной была Ли Сочин. Она старалась говорить аккуратно с Первым Чемпионом Континента и самым достойным звания \"самый могущественный человек на всем континенте\" человеком. </p>', 11);")
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
