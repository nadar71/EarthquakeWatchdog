package com.indiewalk.watchdog.earthquake.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;


@Database(entities = {Earthquake.class}, version = 2, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class EarthquakeDatabase extends RoomDatabase {
    private static final String TAG = EarthquakeDatabase.class.getSimpleName();
    // lock for synchro
    private static final Object LOCK   = new Object();
    private static final String DBNAME = "EarthquakeDB";
    private static EarthquakeDatabase eqDbInstance ;


    public static EarthquakeDatabase getDbInstance(Context context){
        if(eqDbInstance == null){
            synchronized (LOCK){
                Log.d(TAG, "Creating App db singleton instance...");
                eqDbInstance = Room.databaseBuilder(context.getApplicationContext(), EarthquakeDatabase.class,EarthquakeDatabase.DBNAME)
                        //.allowMainThreadQueries() // TODO : temporary for debugging, delete this
                        .build();
            }

        }
        Log.d(TAG, "Db created");
        return eqDbInstance;
    }

    public abstract EarthquakeDbDao earthquakeDbDao();

}