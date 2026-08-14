package com.indiewalk.watchdog.earthquake.feat_statistics.data.remote

import com.indiewalk.watchdog.earthquake.core.di.UsgsClient
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.path
import java.time.format.DateTimeFormatter
import javax.inject.Inject

interface EarthquakeStatisticsRemoteDataSource {
    suspend fun count(window: StatisticsWindow, threshold: Double): Int

    suspend fun events(
        window: StatisticsWindow,
        threshold: Double,
        orderBy: String,
        limit: Int,
        offset: Int = 1
    ): EQFeaturesCollectionDTO
}

class EarthquakeStatisticsApi @Inject constructor(
    @UsgsClient private val client: HttpClient
) : EarthquakeStatisticsRemoteDataSource {
    override suspend fun count(window: StatisticsWindow, threshold: Double): Int =
        client.get {
            usgsPath("count")
            commonParameters(window, threshold)
        }.bodyAsText().trim().toInt()

    override suspend fun events(
        window: StatisticsWindow,
        threshold: Double,
        orderBy: String,
        limit: Int,
        offset: Int
    ): EQFeaturesCollectionDTO = client.get {
        usgsPath("query")
        parameter("format", "geojson")
        commonParameters(window, threshold)
        parameter("orderby", orderBy)
        parameter("limit", limit)
        parameter("offset", offset)
    }.body()

    private fun HttpRequestBuilder.usgsPath(method: String) {
        url {
            protocol = URLProtocol.HTTPS
            host = "earthquake.usgs.gov"
            path("fdsnws", "event", "1", method)
        }
    }

    private fun HttpRequestBuilder.commonParameters(
        window: StatisticsWindow,
        threshold: Double
    ) {
        parameter("eventtype", "earthquake")
        parameter("minmagnitude", threshold)
        parameter("starttime", DateTimeFormatter.ISO_INSTANT.format(window.start))
        parameter("endtime", DateTimeFormatter.ISO_INSTANT.format(window.end))
    }
}
