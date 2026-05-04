package com.indiewalk.watchdog.earthquake

import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeatureDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQGeometryDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQMetadataDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQPropertiesDTO
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

fun sampleEqFeed(
    generated: Long = 1234L,
    features: List<EQFeatureDTO> = listOf(sampleEqFeature())
): EQFeaturesCollectionDTO = EQFeaturesCollectionDTO(
    type = "FeatureCollection",
    metadata = EQMetadataDTO(
        generated = generated,
        url = "https://example.com/feed",
        title = "Sample feed",
        api = "1.0.0",
        count = features.size,
        status = 200
    ),
    bbox = listOf(8.0, 44.0, 5.0, 10.0, 46.0, 15.0),
    features = features
)

fun sampleEqFeature(
    id: String = "feature-1",
    mag: Double? = 4.5,
    time: Long? = System.currentTimeMillis(),
    latitude: Double = 45.0,
    longitude: Double = 9.0
): EQFeatureDTO = EQFeatureDTO(
    type = "Feature",
    id = id,
    properties = EQPropertiesDTO(
        mag = mag,
        place = "Sample place",
        time = time,
        updated = time,
        tz = null,
        url = "https://example.com/$id",
        detail = null,
        felt = null,
        cdi = null,
        mmi = null,
        alert = null,
        status = "reviewed",
        tsunami = 0,
        sig = 100,
        net = "us",
        code = id,
        ids = ",$id,",
        sources = "us",
        types = "origin",
        nst = 10,
        dmin = 0.1,
        rms = 0.2,
        gap = 30.0,
        magType = "ml",
        type = "earthquake"
    ),
    geometry = EQGeometryDTO(
        type = "Point",
        coordinates = listOf(longitude, latitude, 10.0)
    )
)
