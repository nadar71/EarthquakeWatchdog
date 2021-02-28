package com.indiewalk.watchdog.earthquake.data.local

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import android.content.Context
import android.util.Log
import com.indiewalk.watchdog.earthquake.data.model.Earthquake


@Database(entities = [Earthquake::class], version = 4, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class EarthquakeDatabase : RoomDatabase() {

    abstract fun earthquakeDbDao(): EarthquakeDbDao

    companion object {
        private val TAG = EarthquakeDatabase::class.java.simpleName
        // lock for synchro
        private val LOCK = Any()
        private val DBNAME = "EarthquakeDB"
        private var eqDbInstance: EarthquakeDatabase? = null


        internal val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE EARTHQUAKE_LIST " + "ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE EARTHQUAKE_LIST " + "ADD COLUMN latitude  REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE EARTHQUAKE_LIST " + "ADD COLUMN depth     REAL NOT NULL DEFAULT 0.0")
            }
        }


        internal val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE EARTHQUAKE_LIST " + "ADD COLUMN userDistance INTEGER NOT NULL DEFAULT 0")

            }
        }

        fun getDbInstance(context: Context): EarthquakeDatabase? {
            if (eqDbInstance == null) {
                synchronized(LOCK) {
                    Log.d(TAG, "Creating App db singleton instance...")
                    eqDbInstance = Room.databaseBuilder(context.applicationContext, EarthquakeDatabase::class.java, DBNAME)
                            // .allowMainThreadQueries()
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .build()
                }

            }
            Log.d(TAG, "Db created")
            return eqDbInstance
        }
    }

}