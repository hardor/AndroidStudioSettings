package ru.profapp.RanobeReader.DAO;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by Ruslan on 09.02.2018.
 */

public class DateConverter {

    @TypeConverter
    public static Date toDate(long dateLong) {
        if (dateLong == 0) {
            return null;
        }
        return new Date(dateLong);
    }

    @TypeConverter
    public static long fromDate(Date date) {
        if (date == null) {
            return 0;
        }
        return date.getTime();
    }
}
