package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model

import androidx.room.Embedded
import androidx.room.Relation

// Convenient projection to read a feed snapshot together with its events.
data class FeedWithEvents(
    @Embedded val feed: FeedSnapshotEntity,
    @Relation(
        parentColumn = "generated",
        entityColumn = "feed_generated"
    )
    val events: List<EQEntity>
)
