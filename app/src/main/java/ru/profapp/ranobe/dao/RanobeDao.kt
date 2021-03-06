package ru.profapp.ranobe.dao

import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Single
import ru.profapp.ranobe.models.Ranobe
import ru.profapp.ranobe.models.RanobeWithChapters

/**
 * Created by Ruslan on 09.02.2018.
 */

@Dao
interface RanobeDao {

    @Query("SELECT * FROM ranobe")
    fun allRanobe(): Single<List<Ranobe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ranobe: Ranobe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg ranobes: Ranobe)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAll(vararg ranobes: Ranobe)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(ranobe: Ranobe)

    @Query("DELETE FROM ranobe  WHERE url=:UrlToRanobe AND IsFavoriteInWeb != 1")
    fun delete(UrlToRanobe: String)

    @Query("DELETE FROM ranobe  WHERE url=:UrlToRanobe AND IsFavoriteInWeb = 1")
    fun deleteWeb(UrlToRanobe: String)

    @Query("DELETE FROM ranobe  WHERE url=:UrlToRanobe ")
    fun deleteByUrl(UrlToRanobe: String)

    @Delete
    fun delete(ranobe: Ranobe)

    @Query("SELECT * FROM ranobe WHERE url=:UrlToRanobe")
    fun getRanobeByUrl(UrlToRanobe: String): Ranobe

    @Transaction
    @Query("SELECT * FROM ranobe WHERE url=:UrlToRanobe")
    fun getRanobeWithChaptersByUrl(UrlToRanobe: String): Maybe<RanobeWithChapters>

    @Transaction
    @Query("SELECT * FROM ranobe WHERE IsFavorite = 1")
    fun getFavoriteRanobes(): Single<List<RanobeWithChapters>>

    @Query("SELECT * FROM ranobe WHERE IsFavorite = 1")
    fun getLocalFavoriteRanobes(): Single<List<Ranobe>>

    @Query("SELECT * FROM ranobe ")
    fun getAll(): List<Ranobe>

    @Query("SELECT * FROM ranobe WHERE IsFavorite = 1 AND url=:UrlToRanobe LIMIT 1")
    fun isRanobeFavorite(UrlToRanobe: String): Maybe<Ranobe>

    @Query("DELETE FROM ranobe")
    fun cleanTable()
}
