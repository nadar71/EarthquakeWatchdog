package com.indiewalk.watchdog.earthquake.domain.model.geojson

// Geometry describing the location. Coordinates are [lon, lat, depth(km)].
data class Geometry(
    val type: String,                // "Point" fro earthquake
    val coordinates: List<Double>    // [longitude, latitude, depthKm]
) {
    val longitude: Double? get() = coordinates.getOrNull(0)
    val latitude: Double?  get() = coordinates.getOrNull(1)
    val depthKm: Double?   get() = coordinates.getOrNull(2)
}
