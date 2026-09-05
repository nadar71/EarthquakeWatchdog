package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GsonSerializationContractTest {

    @Test
    fun usgsWireFieldNamesRoundTripThroughGson() {
        val json = """
            {
              "type": "FeatureCollection",
              "metadata": {
                "generated": 1720000000000,
                "url": "https://earthquake.usgs.gov/feed",
                "title": "USGS feed",
                "api": "1.14.1",
                "count": 1,
                "status": 200
              },
              "bbox": [-180.0, -90.0, -2.0, 180.0, 90.0, 700.0],
              "features": [{
                "type": "Feature",
                "properties": {
                  "mag": 5.4,
                  "place": "Test location",
                  "time": 1720000000000,
                  "updated": 1720000001000,
                  "tz": 0,
                  "url": "https://earthquake.usgs.gov/event",
                  "detail": "https://earthquake.usgs.gov/detail",
                  "felt": 12,
                  "cdi": 4.2,
                  "mmi": 5.1,
                  "alert": "green",
                  "status": "reviewed",
                  "tsunami": 0,
                  "sig": 450,
                  "net": "us",
                  "code": "test",
                  "ids": ",ustest,",
                  "sources": ",us,",
                  "types": ",origin,",
                  "nst": 25,
                  "dmin": 0.12,
                  "rms": 0.8,
                  "gap": 42.0,
                  "magType": "mb",
                  "type": "earthquake"
                },
                "geometry": {
                  "type": "Point",
                  "coordinates": [12.5, 41.9, 10.0]
                },
                "id": "us-test"
              }]
            }
        """.trimIndent()

        val gson = Gson()
        val feed = gson.fromJson(json, EQFeaturesCollectionDTO::class.java)
        val feature = feed.features.single()
        val roundTripped = gson.toJson(feed)

        assertEquals("mb", feature.properties.magType)
        assertEquals(12.5, feature.geometry.longitude)
        assertEquals(41.9, feature.geometry.latitude)
        assertEquals(10.0, feature.geometry.depthKm)
        assertTrue(roundTripped.contains("\"magType\":\"mb\""))
        assertTrue(roundTripped.contains("\"generated\":1720000000000"))
        assertTrue(roundTripped.contains("\"coordinates\":[12.5,41.9,10.0]"))
    }
}
