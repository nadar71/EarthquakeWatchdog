package com.indiewalk.watchdog.earthquake.data.repository

import com.indiewalk.watchdog.earthquake.data.local.db.EarthquakeDao
import com.indiewalk.watchdog.earthquake.data.local.db.FeedWriterDao
import com.indiewalk.watchdog.earthquake.domain.model.local.SaveResult
import com.indiewalk.watchdog.earthquake.domain.model.remote.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.domain.model.remote.toEntity
import com.indiewalk.watchdog.earthquake.domain.model.remote.toFeedSnapshot
import javax.inject.Inject

class EQRepositoryImpl @Inject constructor(
    private val eqDao: EarthquakeDao,
    private val feedSnapshotDao: EarthquakeDao,
    private val feedWriterDao: FeedWriterDao
) {

    suspend fun saveFromFeed(feed: EQFeaturesCollectionDTO): SaveResult {
        val snapshot = feed.toFeedSnapshot()
        val events = feed.features.map { it.toEntity(feedGenerated = snapshot.generated) }
        feedWriterDao.saveSnapshotAndEvents(snapshot, events)
        return SaveResult(snapshotGenerated = snapshot.generated, eventsUpserted = events.size)
    }
}