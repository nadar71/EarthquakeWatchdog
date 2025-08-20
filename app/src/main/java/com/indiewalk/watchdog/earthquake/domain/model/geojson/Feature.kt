package com.indiewalk.watchdog.earthquake.domain.model.geojson

// A single Feature with properties + geometry + id.
data class Feature(
    val type: String,                // "Feature"
    val properties: Properties,
    val geometry: Geometry?,         // may be null in some feeds
    val id: String
)
