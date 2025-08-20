package com.indiewalk.watchdog.earthquake.domain.model.geojson

// Metadata about the feed generation and counts.
data class Metadata(
    val generated: Long,
    val url: String,
    val title: String,
    val api: String,
    val count: Int,
    val status: Int
)
