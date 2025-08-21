package com.indiewalk.watchdog.earthquake.domain.model.remote

// top-level GeoJSON container for earthquake feeds as received from the USGS network/dto.
// DTO from object Geojson https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php
data class EQFeaturesCollectionDTO(
    val type: String,                // "FeatureCollection"
    val metadataDTO: MetadataDTO,
    val bbox: List<Double>?,         // [minLon, minLat, minDepth, maxLon, maxLat, maxDepth]
    val features: List<FeaturesDTO>
)