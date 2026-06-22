package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto

// Metadata about the feed generation and counts, network/dto.
data class EQMetadataDTO(
    val generated: Long,
    val url: String,
    val title: String,
    val api: String,
    val count: Int,
    val status: Int
)
