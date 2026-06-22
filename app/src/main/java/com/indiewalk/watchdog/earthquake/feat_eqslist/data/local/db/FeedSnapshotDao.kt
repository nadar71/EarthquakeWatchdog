package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQFeedSnapshotEntity

@Dao
interface FeedSnapshotDao {
    @Delete
    suspend fun deleteSnapshot(EQFeedSnapshotEntity: EQFeedSnapshotEntity)
    @Query("DELETE FROM feed_snapshot")
    suspend  fun dropSnapshotTable()
}