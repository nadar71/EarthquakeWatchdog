package com.indiewalk.watchdog.earthquake.feat_statistics.data.remote

import com.google.gson.GsonBuilder
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsPeriod
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class EarthquakeStatisticsApiTest {

    private val window = StatisticsWindow(
        period = StatisticsPeriod.TODAY,
        start = Instant.parse("2026-08-13T22:00:00Z"),
        end = Instant.parse("2026-08-14T10:30:00Z")
    )

    @Test
    fun countSendsGlobalMagnitudeAndTimeParameters() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/fdsnws/event/1/count", request.url.encodedPath)
            assertEquals("earthquake", request.url.parameters["eventtype"])
            assertEquals("2.5", request.url.parameters["minmagnitude"])
            assertEquals("2026-08-13T22:00:00Z", request.url.parameters["starttime"])
            assertEquals("2026-08-14T10:30:00Z", request.url.parameters["endtime"])
            respond("146", HttpStatusCode.OK)
        }
        val api = EarthquakeStatisticsApi(testClient(engine))

        assertEquals(146, api.count(window, threshold = 2.5))
    }

    @Test
    fun eventQueryPreservesOrderLimitAndOffset() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/fdsnws/event/1/query", request.url.encodedPath)
            assertEquals("geojson", request.url.parameters["format"])
            assertEquals("magnitude", request.url.parameters["orderby"])
            assertEquals("1", request.url.parameters["limit"])
            assertEquals("20001", request.url.parameters["offset"])
            respond(
                content = EMPTY_COLLECTION,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val api = EarthquakeStatisticsApi(testClient(engine))

        val result = api.events(
            window = window,
            threshold = 2.5,
            orderBy = "magnitude",
            limit = 1,
            offset = 20_001
        )

        assertEquals(0, result.metadata.count)
        assertEquals(emptyList<Any>(), result.features)
    }

    private fun testClient(engine: MockEngine) = HttpClient(engine) {
        install(ContentNegotiation) {
            gson {
                serializeNulls()
                setLenient()
            }
        }
    }

    private companion object {
        val EMPTY_COLLECTION: String = GsonBuilder().create().toJson(
            mapOf(
                "type" to "FeatureCollection",
                "metadata" to mapOf(
                    "generated" to 1L,
                    "url" to "https://earthquake.usgs.gov",
                    "title" to "USGS earthquakes",
                    "api" to "1.0.0",
                    "count" to 0,
                    "status" to 200
                ),
                "bbox" to null,
                "features" to emptyList<Any>()
            )
        )
    }
}
