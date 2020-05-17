package com.indiewalk.watchdog.earthquake.data

import android.arch.persistence.room.TypeConverter
import android.util.Log

import java.util.Date

object DateConverter {
    private val TAG = DateConverter::class.java.simpleName

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        Log.d(TAG, "to Date from milllisec : " + timestamp + " is " + Date(timestamp!!))
        return if (timestamp == null) null else Date(timestamp)

    }

    @TypeConverter
    fun fromDate(date: Date): Long? {
        Log.d(TAG, "to millisecfromepoch from date: " + date + " is " + date.time)
        return date?.time

    }
}