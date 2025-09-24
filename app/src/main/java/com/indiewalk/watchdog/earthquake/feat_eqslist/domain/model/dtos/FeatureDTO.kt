package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos

import com.indiewalk.watchdog.earthquake.core.util.MapsUtils.getEQDistanceFromUser
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.EQEntity

// A single EQ Feature with properties + geometry + id, network/dto.
data class FeatureDTO(
    val type: String,                   // "Feature"
    val properties: PropertiesDTO,
    val geometry: GeometryDTO,
    val id: String
)



fun FeatureDTO.toEntity(feedGenerated: Long?): EQEntity =
    EQEntity(
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
        distanceFromUser = getEQDistanceFromUser(geometry)
    )