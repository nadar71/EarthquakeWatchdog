package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db

import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQFeedSnapshotEntity
import androidx.room.Dao
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface FeedWriterDao {
    @Upsert
    suspend fun upsertSnapshot(snapshot: EQFeedSnapshotEntity)
    @Upsert
    suspend fun upsertAllEvents(events: List<EQEntity>)
    // Atomic write of snapshot + events.
    @Transaction
    suspend fun saveSnapshotAndEvents(
        snapshot: EQFeedSnapshotEntity,
        events: List<EQEntity>
    ) {
        upsertSnapshot(snapshot)
        upsertAllEvents(events)
    }
}
