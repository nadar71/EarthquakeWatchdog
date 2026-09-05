package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQFeedSnapshotEntity
import com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsCacheDao
import com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsCacheEntity
import kotlin.jvm.java

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS statistics_cache (
                cacheId INTEGER NOT NULL PRIMARY KEY,
                todayCount INTEGER NOT NULL,
                weekCount INTEGER NOT NULL,
                monthCount INTEGER NOT NULL,
                yearCount INTEGER NOT NULL,
                threshold REAL NOT NULL,
                retrievedAtEpochMillis INTEGER NOT NULL,
                windowsJson TEXT NOT NULL,
                strongestEventJson TEXT,
                nearestEventJson TEXT
            )""".trimIndent()
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE statistics_cache ADD COLUMN insightsJson TEXT")
    }
}

@Database(
    entities = [
        EQEntity::class,
        EQFeedSnapshotEntity::class,
        StatisticsCacheEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class EarthquakeDatabase : RoomDatabase() {
    abstract fun feedSnapshotDao(): FeedSnapshotDao
    abstract fun earthquakeDao(): EarthquakeDao
    abstract fun feedWriterDao(): FeedWriterDao
    abstract fun statisticsCacheDao(): StatisticsCacheDao

    companion object {
        private val DBNAME = "EarthquakesDB"

        fun getDbInstance(context: Context): EarthquakeDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                EarthquakeDatabase::class.java,
                DBNAME
            )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                .build()
        }
    }

}
