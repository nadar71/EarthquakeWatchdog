package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db

import androidx.room.Delete
import androidx.room.Query
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.FeedSnapshotEntity

interface FeedSnapshotDao {
    @Delete
    suspend fun deleteSnapshot(feedSnapshotEntity: FeedSnapshotEntity)
    @Query("DELETE FROM feed_snapshot")
    suspend  fun dropSnapshotTable()
}