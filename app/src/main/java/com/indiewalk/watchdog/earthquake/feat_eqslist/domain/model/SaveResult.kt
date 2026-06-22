package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model

data class SaveResult(
    val snapshotGenerated: Long,
    val eventsUpserted: Int
)