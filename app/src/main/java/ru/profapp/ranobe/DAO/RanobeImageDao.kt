package ru.profapp.ranobe.DAO

import androidx.room.*
import io.reactivex.Maybe
import ru.profapp.ranobe.models.RanobeImage

/**
 * Created by Ruslan on 09.02.2018.
 */

@Dao
interface RanobeImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(image: RanobeImage)

    @Query("DELETE FROM ranobeImage  WHERE RanobeUrl=:UrlToRanobe")
    fun delete(UrlToRanobe: String)

    @Delete
    fun delete(image: RanobeImage)

    @Query("SELECT * FROM ranobeImage WHERE RanobeUrl=:UrlToRanobe")
    fun getImageByUrl(UrlToRanobe: String): Maybe<RanobeImage>

    @Query("DELETE FROM ranobeImage")
    fun cleanTable()
}
