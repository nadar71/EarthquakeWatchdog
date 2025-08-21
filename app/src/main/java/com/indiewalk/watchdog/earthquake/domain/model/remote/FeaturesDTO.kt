package com.indiewalk.watchdog.earthquake.domain.model.remote

import androidx.room.Embedded

// A single EQ Feature with properties + geometry + id, network/dto.
data class FeaturesDTO(
    val type: String,                       // "Feature"
    @Embedded val propertiesDTO: PropertiesDTO,
    @Embedded val geometryDTO: GeometryDTO?,      // may be null
    val id: String
)