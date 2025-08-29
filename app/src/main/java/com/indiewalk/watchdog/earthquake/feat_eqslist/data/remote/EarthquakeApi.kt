package com.indiewalk.watchdog.earthquake.feat_eqslist.data.remote

import com.indiewalk.watchdog.earthquake.core.di.UsgsClient
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.EarthquakeQueryParams
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLProtocol
import io.ktor.http.path
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.EQFeaturesCollectionDTO
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class EarthquakeApi @Inject constructor(
    @UsgsClient private val client: HttpClient
) {

    // Fetches a GeoJSON feed from USGS and deserializes into EQFeaturesCollectionDTO.
    // API docs: https://earthquake.usgs.gov/fdsnws/event/1/
    suspend fun fetchFeed(
        params: EarthquakeQueryParams = EarthquakeQueryParams()
    ): EQFeaturesCollectionDTO {
        return client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "earthquake.usgs.gov"
                path("fdsnws", "event", "1", "query")
                // Required
                parameter("format", params.format)
                parameter("eventtype", params.eventType)
                parameter("orderby", params.orderBy)
                // Time filters (ISO-8601 strings)
                params.startTime?.let { parameter("starttime", it.iso()) }
                params.endTime?.let { parameter("endtime", it.iso()) }
                // Magnitude filters
                params.minMagnitude?.let { parameter("minmagnitude", it) }
                params.maxMagnitude?.let { parameter("maxmagnitude", it) }
                // Bounding box
                params.minLatitude?.let { parameter("minlatitude", it) }
                params.maxLatitude?.let { parameter("maxlatitude", it) }
                params.minLongitude?.let { parameter("minlongitude", it) }
                params.maxLongitude?.let { parameter("maxlongitude", it) }
                // Circular search
                params.latitude?.let { parameter("latitude", it) }
                params.longitude?.let { parameter("longitude", it) }
                params.maxRadiusKm?.let { parameter("maxradiuskm", it) }
                // Paging
                params.limit?.let { parameter("limit", it) }
                params.offset?.let { parameter("offset", it) }
            }
        }.body()
    }

    private fun Instant.iso(): String = DateTimeFormatter.ISO_INSTANT.format(this)
}
