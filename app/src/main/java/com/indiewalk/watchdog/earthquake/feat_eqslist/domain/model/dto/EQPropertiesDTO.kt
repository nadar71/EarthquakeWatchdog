package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto

// Earthquake properties. Many can be null depending on the event, network/dto.
data class EQPropertiesDTO(
    val mag: Double?,                // magnitude
    val place: String?,
    val time: Long?,                 // epoch millis
    val updated: Long?,              // epoch millis
    val tz: Int?,                    // timezone offset minutes (legacy; often null)
    val url: String?,
    val detail: String?,
    val felt: Int?,                  // number of reports
    val cdi: Double?,                // community intensity
    val mmi: Double?,                // modified mercalli intensity
    val alert: String?,              // "green"/"yellow"/"orange"/"red"
    val status: String?,             // "automatic"/"reviewed"/...
    val tsunami: Int?,               // 0/1 flag
    val sig: Int?,                   // significance
    val net: String?,                // network
    val code: String?,
    val ids: String?,
    val sources: String?,
    val types: String?,
    val nst: Int?,                   // number of stations
    val dmin: Double?,               // horizontal distance to nearest station
    val rms: Double?,                // root mean square
    val gap: Double?,                // azimuthal gap
    val magType: String?,            // e.g., "mb", "ml", "mwr"
    val type: String?                // event type, e.g., "earthquake"
)
