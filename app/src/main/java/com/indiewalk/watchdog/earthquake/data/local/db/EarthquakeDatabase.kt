package com.indiewalk.watchdog.earthquake.data.local.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import android.content.Context
import android.util.Log
import androidx.work.impl.WorkDatabaseMigrations.MIGRATION_1_2
import androidx.work.impl.WorkDatabaseMigrations.MIGRATION_4_5
import com.indiewalk.watchdog.earthquake.domain.model.EarthquakeUI
import kotlin.jvm.java


@Database(
    entities = [EarthquakeUI::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class EarthquakeDatabase : RoomDatabase() {
    abstract fun earthquakeDbDao(): EarthquakeDbDao

    companion object {
        private val DBNAME = "EarthquakeDB"


        /*internal val MIGRATION_2_3: Migration = object : Migration(2, 3) {
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
        }*/

        fun getDbInstance(context: Context): EarthquakeDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                EarthquakeDatabase::class.java,
                DBNAME
            )
                .build()
        }
    }

}