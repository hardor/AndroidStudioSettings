package ru.profapp.ranobe.Models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

/**
 * Created by Ruslan on 21.02.2018.
 */
@Entity(tableName = "ranobeHistory", indices = [Index(value = ["RanobeUrl"])])
class RanobeHistory(@PrimaryKey
                    @ColumnInfo(name = "RanobeUrl") var ranobeUrl: String,
                    @ColumnInfo(name = "RanobeName") var ranobeName: String,
                    @ColumnInfo(name = "Description") var description: String?) {

    @ColumnInfo(name = "ReadDate")
    var readDate: Date = Date()

}

