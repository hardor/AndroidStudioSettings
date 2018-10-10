package ru.profapp.RanobeReader.Models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

/**
 * Created by Ruslan on 21.02.2018.
 */
@Entity(tableName = "ranobeHistory", indices = [Index(value = ["RanobeUrl"])])
class RanobeHistory {

    @PrimaryKey
    @ColumnInfo(name = "RanobeUrl")
    var ranobeUrl: String
    @ColumnInfo(name = "RanobeName")
    var ranobeName: String
    @ColumnInfo(name = "Description")
    var description: String? = null
    @ColumnInfo(name = "ReadDate")
    var readDate: Date = Date()

    constructor(ranobeUrl: String, ranobeName: String, description: String?) {
        this.ranobeUrl = ranobeUrl
        this.ranobeName = ranobeName
        this.description = description
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RanobeHistory

        if (ranobeUrl != other.ranobeUrl) return false
        if (ranobeName != other.ranobeName) return false
        if (description != other.description) return false
        if (readDate != other.readDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ranobeUrl.hashCode()
        result = 31 * result + ranobeName.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + readDate.hashCode()
        return result
    }

}

