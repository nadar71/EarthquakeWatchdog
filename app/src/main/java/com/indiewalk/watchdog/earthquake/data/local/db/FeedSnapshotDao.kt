package com.indiewalk.watchdog.earthquake.data.local.db

import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.indiewalk.watchdog.earthquake.domain.model.local.FeedSnapshotEntity

interface FeedSnapshotDao {

    /*@Upsert
    suspend fun upsert(feedSnapshotEntity: FeedSnapshotEntity)*/

    @Delete
    suspend fun delete(feedSnapshotEntity: FeedSnapshotEntity)

    @Query("DELETE FROM earthquakes")
    suspend  fun dropEarthquakeListTable()

}