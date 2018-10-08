package ru.profapp.RanobeReader.Models

import androidx.annotation.NonNull
import androidx.room.*

/**
 * Created by Ruslan on 09.02.2018.
 */
@Entity(tableName = "ranobeImage", indices = [Index(value = ["RanobeUrl"])])
class RanobeImage() {
    @Ignore
    constructor(ranobeUrl: String, image: String) : this() {
        this.ranobeUrl = ranobeUrl

        this.image = image
    }

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "RanobeUrl")
    var ranobeUrl: String = ""
    @ColumnInfo(name = "Image")
    var image: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RanobeImage

        if (ranobeUrl != other.ranobeUrl) return false

        return true
    }

    override fun hashCode(): Int {
        return ranobeUrl.hashCode()
    }

}
