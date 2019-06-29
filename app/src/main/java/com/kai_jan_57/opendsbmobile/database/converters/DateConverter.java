package com.kai_jan_57.opendsbmobile.database.converters;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {

    @TypeConverter
    public static Long toTimestamp(Date date) {
        if (date == null) {
            return null;
        }
        return date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long date) {
        if (date == null) {
            return null;
        }
        return new Date(date);
    }

}
