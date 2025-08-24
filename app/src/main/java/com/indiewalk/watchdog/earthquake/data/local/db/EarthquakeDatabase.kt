package com.indiewalk.watchdog.earthquake.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.indiewalk.watchdog.earthquake.data.local.db.EarthquakeDao
import com.indiewalk.watchdog.earthquake.domain.model.local.EQEntity
import com.indiewalk.watchdog.earthquake.domain.model.local.FeedSnapshotEntity
import kotlin.jvm.java


@Database(
    entities = [
        EQEntity::class,
        FeedSnapshotEntity::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class EarthquakeDatabase : RoomDatabase() {
    abstract fun earthquakeDbDao(): EarthquakeDao

    companion object {
        private val DBNAME = "EarthquakesDB"

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