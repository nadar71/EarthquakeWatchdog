package com.indiewalk.watchdog.earthquake.domain.model.remote

import com.indiewalk.watchdog.earthquake.domain.model.local.FeedSnapshotEntity

// top-level GeoJSON container for earthquake feeds as received from the USGS network/dto.
// DTO from object Geojson https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php
data class EQFeaturesCollectionDTO(
    val type: String,                // "FeatureCollection"
    val metadata: MetadataDTO,
    val bbox: List<Double>?,         // [minLon, minLat, minDepth, maxLon, maxLat, maxDepth]
    val features: List<FeaturesDTO>
)


fun EQFeaturesCollectionDTO.toFeedSnapshot(): FeedSnapshotEntity =
    FeedSnapshotEntity(
        generated = metadata.generated,
        url = metadata.url,
        title = metadata.title,
        api = metadata.api,
        count = metadata.count,
        status = metadata.status,
        minLon = bbox?.getOrNull(0),
        minLat = bbox?.getOrNull(1),
        minDepth = bbox?.getOrNull(2),
        maxLon = bbox?.getOrNull(3),
        maxLat = bbox?.getOrNull(4),
        maxDepth = bbox?.getOrNull(5)
    )


