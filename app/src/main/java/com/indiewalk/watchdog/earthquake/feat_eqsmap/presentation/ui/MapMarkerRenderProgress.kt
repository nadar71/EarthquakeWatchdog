package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

internal class MapMarkerRenderProgress(
    private val totalMarkerCount: Int,
    private val batchSize: Int = DEFAULT_BATCH_SIZE,
) {
    init {
        require(totalMarkerCount >= 0)
        require(batchSize > 0)
    }

    fun nextCount(currentCount: Int): Int =
        (currentCount + batchSize).coerceAtMost(totalMarkerCount)

    fun isComplete(mapLoaded: Boolean, renderedMarkerCount: Int): Boolean =
        mapLoaded && renderedMarkerCount >= totalMarkerCount

    private companion object {
        const val DEFAULT_BATCH_SIZE = 25
    }
}
