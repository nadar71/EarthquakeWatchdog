package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto

import java.time.Instant

data class EarthquakeQueryParams(
    val format: String = "geojson", // "geojson", "csv", "kml", "text", "xml"
    val eventType: String = "earthquake",
    val orderBy: String = "time",      // "time" | "time-asc" | "magnitude" | "magnitude-asc"
    val startTime: Instant? = null,    // ISO8601 accepted by USGS
    val endTime: Instant? = null,
    val minMagnitude: Double? = null,
    val maxMagnitude: Double? = null,
    // Viewport bbox (optional)
    val minLatitude: Double? = null,
    val maxLatitude: Double? = null,
    val minLongitude: Double? = null,
    val maxLongitude: Double? = null,
    // Or circular search (optional)
    val latitude: Double? = null,
    val longitude: Double? = null,
    val maxRadiusKm: Double? = null,
    // Paging
    val limit: Int? = 200,
    val offset: Int? = null
)
