package com.indiewalk.watchdog.earthquake.data.local.db

import com.indiewalk.watchdog.earthquake.domain.model.local.EQEntity
import com.indiewalk.watchdog.earthquake.domain.model.local.FeedSnapshotEntity
import androidx.room.Dao
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface FeedWriterDao {
    @Upsert
    suspend fun upsertSnapshot(snapshot: FeedSnapshotEntity)

    @Upsert
    suspend fun upsertAllEvents(events: List<EQEntity>)

    // Atomic write of snapshot + events.
    @Transaction
    suspend fun saveSnapshotAndEvents(
        snapshot: FeedSnapshotEntity,
        events: List<EQEntity>
    ) {
        upsertSnapshot(snapshot)
        upsertAllEvents(events)
    }
}
