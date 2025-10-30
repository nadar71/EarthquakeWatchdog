package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto

import com.indiewalk.watchdog.earthquake.core.model.AppSettings
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