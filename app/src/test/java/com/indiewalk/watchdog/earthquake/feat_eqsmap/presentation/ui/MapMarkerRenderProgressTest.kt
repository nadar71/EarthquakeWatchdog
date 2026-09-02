package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapMarkerRenderProgressTest {

    @Test
    fun `markers are rendered in bounded batches`() {
        val progress = MapMarkerRenderProgress(totalMarkerCount = 120, batchSize = 25)

        assertEquals(25, progress.nextCount(0))
        assertEquals(50, progress.nextCount(25))
        assertEquals(120, progress.nextCount(100))
    }

    @Test
    fun `last batch never exceeds available markers`() {
        val progress = MapMarkerRenderProgress(totalMarkerCount = 12, batchSize = 25)

        assertEquals(12, progress.nextCount(0))
    }

    @Test
    fun `loading completes only after map and every marker are ready`() {
        val progress = MapMarkerRenderProgress(totalMarkerCount = 50, batchSize = 25)

        assertFalse(progress.isComplete(mapLoaded = false, renderedMarkerCount = 50))
        assertFalse(progress.isComplete(mapLoaded = true, renderedMarkerCount = 25))
        assertTrue(progress.isComplete(mapLoaded = true, renderedMarkerCount = 50))
    }

    @Test
    fun `empty data completes when map is loaded`() {
        val progress = MapMarkerRenderProgress(totalMarkerCount = 0, batchSize = 25)

        assertTrue(progress.isComplete(mapLoaded = true, renderedMarkerCount = 0))
    }
}
