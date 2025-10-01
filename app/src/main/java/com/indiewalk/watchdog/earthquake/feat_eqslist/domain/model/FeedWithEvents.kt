package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model

import androidx.room.Embedded
import androidx.room.Relation
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQFeedSnapshotEntity

// Projection to read a feed snapshot together with its events.
data class FeedWithEvents(
    @Embedded val feed: EQFeedSnapshotEntity,
    @Relation(
        parentColumn = "generated",
        entityColumn = "feed_generated"
    )
    val events: List<EQEntity>
)
