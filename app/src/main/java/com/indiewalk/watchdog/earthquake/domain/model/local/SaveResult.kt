package com.indiewalk.watchdog.earthquake.domain.model.local

data class SaveResult(
    val snapshotGenerated: Long,
    val eventsUpserted: Int
)