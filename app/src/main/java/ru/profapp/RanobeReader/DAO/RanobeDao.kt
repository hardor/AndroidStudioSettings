package ru.profapp.RanobeReader.DAO

import androidx.room.*
import io.reactivex.Flowable
import ru.profapp.RanobeReader.Models.Ranobe

/**
 * Created by Ruslan on 09.02.2018.
 */

@Dao
interface RanobeDao {

    @Query("SELECT * FROM ranobe")
    fun  allRanobe(): Flowable<List<Ranobe>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(ranobe: Ranobe)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg ranobes: Ranobe)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateAll(vararg ranobes: Ranobe)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(ranobe: Ranobe)

    @Query("DELETE FROM ranobe  WHERE url=:UrlToRanobe AND IsFavoriteInWeb != 1")
    fun delete(UrlToRanobe: String)

    @Query("DELETE FROM ranobe  WHERE url=:UrlToRanobe AND IsFavoriteInWeb = 1")
    fun deleteWeb(UrlToRanobe: String)

    @Delete
    fun delete(ranobe: Ranobe)

    @Query("SELECT * FROM ranobe WHERE url=:UrlToRanobe")
    fun getRanobeByUrl(UrlToRanobe: String): Ranobe
    @Query("SELECT * FROM ranobe WHERE IsFavorite = 1")
    fun getFavoriteRanobes(): List<Ranobe>

    @Query("SELECT * FROM ranobe WHERE IsFavorite = 1 AND RanobeSite = :ranobeSite")
    fun getFavoriteBySite(ranobeSite: String): List<Ranobe>

    @Query("SELECT * FROM ranobe WHERE IsFavorite = 1 AND url=:UrlToRanobe LIMIT 1")
    fun isRanobeFavorite(UrlToRanobe: String): Ranobe

    @Query("DELETE FROM ranobe")
    fun cleanTable()
}
