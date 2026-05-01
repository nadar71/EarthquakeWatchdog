package com.indiewalk.watchdog.earthquake

import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity

fun sampleEqEntity(
    id: String = "eq-1",
    mag: Double? = 4.5,
    time: Long? = System.currentTimeMillis(),
    distanceFromUser: Int? = 10,
    latitude: Double? = 45.0,
    longitude: Double? = 9.0
): EQEntity = EQEntity(
    id = id,
    feedGenerated = 1L,
    mag = mag,
    place = "Sample place",
    time = time,
    updated = time,
    tz = null,
    url = null,
    detail = null,
    felt = null,
    cdi = null,
    mmi = null,
    alert = null,
    status = null,
    tsunami = null,
    sig = null,
    net = null,
    code = null,
    ids = null,
    sources = null,
    types = null,
    nst = null,
    dmin = null,
    rms = null,
    gap = null,
    magType = null,
    eventType = "earthquake",
    geometryType = "Point",
    longitude = longitude,
    latitude = latitude,
    depthKm = 10.0,
    distanceFromUser = distanceFromUser
)

val sampleLatLng = LatLng(45.0, 9.0)
val sampleLocationInfo = LocationInfo("Milan", "IT", "IT, Milan, Sample Street")

