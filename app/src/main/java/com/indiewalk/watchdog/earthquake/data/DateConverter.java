package com.indiewalk.watchdog.earthquake.data;

import android.arch.persistence.room.TypeConverter;
import android.util.Log;

import java.util.Date;

public class DateConverter {
    private final static String TAG = DateConverter.class.getSimpleName();

    @TypeConverter
    public static Date toDate(Long timestamp){
        Log.d(TAG, "to Date from milllisec : " + timestamp + " is " + new Date(timestamp));
        return timestamp == null ? null : new Date(timestamp);

    }
    @TypeConverter
    public static Long fromDate(Date date){
        Log.d(TAG, "to millisecfromepoch from date: " + date + " is " + date.getTime());
        return date == null ? null : date.getTime();

    }
}