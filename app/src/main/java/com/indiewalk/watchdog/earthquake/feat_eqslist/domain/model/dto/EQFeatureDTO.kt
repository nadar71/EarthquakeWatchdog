package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto

import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.getEQDistanceFromUser
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity

// Single EQ Feature network/dto with properties + geometry + id.
data class EQFeatureDTO(
    val type: String,                   // "Feature"
    val properties: EQPropertiesDTO,
    val geometry: EQGeometryDTO,
    val id: String
)



fun EQFeatureDTO.toEQEntity(
    feedGenerated: Long?,
    settings: AppSettings
): EQEntity {
    val distKm = getEQDistanceFromUser(geometry, settings)?.toInt() // truncate to int
    return baseEntity(feedGenerated, distKm)
}


private fun EQFeatureDTO.baseEntity(
    feedGenerated: Long?,
    distanceFromUserKm: Int?
) = EQEntity(
    id = id,
    feedGenerated = feedGenerated,
    mag = properties.mag,
    place = properties.place,
    time = properties.time,
    updated = properties.updated,
    tz = properties.tz,
    url = properties.url,
    detail = properties.detail,
    felt = properties.felt,
    cdi = properties.cdi,
    mmi = properties.mmi,
    alert = properties.alert,
    status = properties.status,
    tsunami = properties.tsunami,
    sig = properties.sig,
    net = properties.net,
    code = properties.code,
    ids = properties.ids,
    sources = properties.sources,
    types = properties.types,
    nst = properties.nst,
    dmin = properties.dmin,
    rms = properties.rms,
    gap = properties.gap,
    magType = properties.magType,
    eventType = properties.type,
    geometryType = geometry.type,
    longitude = geometry.longitude,
    latitude = geometry.latitude,
    depthKm = geometry.depthKm,
    distanceFromUser = distanceFromUserKm // stored as KM (canonical)
)

// -- DATA SAMPLE --
/*private fun sampleFeature1() = EQFeatureDTO(
    type = "Feature",
    id = "sample-ci-001",
    properties = EQPropertiesDTO(
        mag = 5.0,
        place = "76 km West of Macquarie Island",
        time = 1583196360000,  // Mar 03, 2020 2:06 am (example)
        updated = 1583199960000,
        tz = null,
        url = "https://earthquake.usgs.gov/earthquakes/eventpage/sample-ci-001",
        detail = null,
        felt = null,
        cdi = null,
        mmi = null,
        alert = null,
        status = "reviewed",
        tsunami = 0,
        sig = 385,
        net = "ci",
        code = "001",
        ids = ",sample-ci-001,",
        sources = "ci",
        types = "origin,phase-data",
        nst = 25,
        dmin = 0.123,
        rms = 0.76,
        gap = 45.0,
        magType = "mb",
        type = "earthquake"
    ),
    geometry = EQGeometryDTO(
        type = "Point",
        coordinates = listOf(158.95, -54.5, 10.0) // lon, lat, depth(km)
    )
).toEQEntity(124124124).toEarthquakeUI(distanceFromUserCustom = 123456789)
*/
