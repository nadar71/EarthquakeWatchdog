package com.indiewalk.watchdog.earthquake.domain.model.remote

import androidx.compose.material3.ModalBottomSheetDefaults.properties
import androidx.room.Embedded
import com.indiewalk.watchdog.earthquake.core.util.MapsUtils.getEQDistanceFromUser
import com.indiewalk.watchdog.earthquake.domain.model.local.EQEntity

// A single EQ Feature with properties + geometry + id, network/dto.
data class FeaturesDTO(
    val type: String,                       // "Feature"
    @Embedded val properties: PropertiesDTO,
    @Embedded val geometry: GeometryDTO,      // may be null
    val id: String
)



fun FeaturesDTO.toEntity(feedGenerated: Long?): EQEntity =
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