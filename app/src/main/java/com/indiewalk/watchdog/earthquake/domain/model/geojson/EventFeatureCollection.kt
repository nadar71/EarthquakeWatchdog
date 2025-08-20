package com.indiewalk.watchdog.earthquake.domain.model.geojson

// Top-level GeoJSON container for earthquake feeds.
data class EventFeatureCollection(
    val type: String,                // "FeatureCollection"
    val metadata: Metadata,
    val bbox: List<Double>?,         // [minLon, minLat, minDepth, maxLon, maxLat, maxDepth]
    val features: List<Feature>
)
