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


@Database(entities = {Earthquake.class}, version = 4, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class EarthquakeDatabase extends RoomDatabase {
    private static final String TAG = EarthquakeDatabase.class.getSimpleName();
    // lock for synchro
    private static final Object LOCK   = new Object();
    private static final String DBNAME = "EarthquakeDB";
    private static EarthquakeDatabase eqDbInstance ;


    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE EARTHQUAKE_LIST "+"ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE EARTHQUAKE_LIST "+"ADD COLUMN latitude  REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE EARTHQUAKE_LIST "+"ADD COLUMN depth     REAL NOT NULL DEFAULT 0.0");
        }
    };


    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE EARTHQUAKE_LIST "+"ADD COLUMN userDistance INTEGER NOT NULL DEFAULT 0");

        }
    };

    public static EarthquakeDatabase getDbInstance(Context context){
        if(eqDbInstance == null){
            synchronized (LOCK){
                Log.d(TAG, "Creating App db singleton instance...");
                eqDbInstance = Room.databaseBuilder(context.getApplicationContext(), EarthquakeDatabase.class,EarthquakeDatabase.DBNAME)
                        //.allowMainThreadQueries() // TODO : temporary for debugging, delete this
                        .addMigrations(MIGRATION_2_3)
                        .addMigrations(MIGRATION_3_4)
                        .build();
            }

        }
        Log.d(TAG, "Db created");
        return eqDbInstance;
    }

    public abstract EarthquakeDbDao earthquakeDbDao();

}