package ru.profapp.RanobeReaderTest.DAO

import androidx.room.TypeConverter
import java.util.*

/**
 * Created by Ruslan on 09.02.2018.
 */

class DateConverter {

    @TypeConverter
    fun toDate(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
}
