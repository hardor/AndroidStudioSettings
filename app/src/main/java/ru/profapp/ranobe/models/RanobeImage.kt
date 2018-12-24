package ru.profapp.ranobe.models

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

}
