package com.indiewalk.watchdog.earthquake.domain.model.dtos

// Metadata about the feed generation and counts, network/dto.
data class MetadataDTO(
    val generated: Long,
    val url: String,
    val title: String,
    val api: String,
    val count: Int,
    val status: Int
)
