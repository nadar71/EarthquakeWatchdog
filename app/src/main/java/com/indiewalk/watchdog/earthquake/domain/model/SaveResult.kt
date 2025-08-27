package com.indiewalk.watchdog.earthquake.domain.model

data class SaveResult(
    val snapshotGenerated: Long,
    val eventsUpserted: Int
)