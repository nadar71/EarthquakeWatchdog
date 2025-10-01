package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// DB entity with feed metadata and bbox, Derived from EQFeaturesCollectionDTO.toFeedSnapshot()
// from object Geojson https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php
// One row per each Geojson feed fetch
// (which has 1 or more EarthquakeEntity derived from EQFeaturesCollectionDTO.features).
// BBox flattened for easy querying.
@Entity(tableName = "feed_snapshot")
data class EQFeedSnapshotEntity(
    @PrimaryKey val generated: Long, // epoch ms (acts as snapshot id)
    val url: String,
    val title: String,
    val api: String,
    val count: Int,
    val status: Int,
    // bbox: [minLon, minLat, minDepth, maxLon, maxLat, maxDepth]
    val minLon: Double?,
    val minLat: Double?,
    val minDepth: Double?,
    val maxLon: Double?,
    val maxLat: Double?,
    val maxDepth: Double?
)