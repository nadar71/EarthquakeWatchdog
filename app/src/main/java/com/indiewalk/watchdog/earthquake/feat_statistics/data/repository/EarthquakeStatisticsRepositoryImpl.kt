package com.indiewalk.watchdog.earthquake.feat_statistics.data.repository

import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.domain.repository.AppPreferencesRepository
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeatureDTO
import com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsCacheDao
import com.indiewalk.watchdog.earthquake.feat_statistics.data.local.toCacheEntity
import com.indiewalk.watchdog.earthquake.feat_statistics.data.local.toSnapshot
import com.indiewalk.watchdog.earthquake.feat_statistics.data.remote.EarthquakeStatisticsRemoteDataSource
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsCounts
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsLoadResult
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsPeriod
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSection
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSnapshot
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindows
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.repository.EarthquakeStatisticsRepository
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.time.StatisticsTimeProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.supervisorScope
import java.time.Duration
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class EarthquakeStatisticsLoadException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class EarthquakeStatisticsRepositoryImpl(
    private val remote: EarthquakeStatisticsRemoteDataSource,
    private val cacheDao: StatisticsCacheDao,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val timeProvider: StatisticsTimeProvider
) : EarthquakeStatisticsRepository {

    override fun load(forceRefresh: Boolean): Flow<StatisticsLoadResult> = flow {
        val cached = cacheDao.get()?.toSnapshot()
        val windows = timeProvider.windows()
        val cacheAge = cached?.let { Duration.between(it.retrievedAt, windows.today.end) }
        val isFresh = cacheAge != null && !cacheAge.isNegative && cacheAge < CACHE_TTL

        if (cached != null) {
            emit(
                StatisticsLoadResult(
                    snapshot = cached,
                    isFromCache = true,
                    isStale = !isFresh
                )
            )
        }
        if (!forceRefresh && isFresh) return@flow

        emit(refresh(windows))
    }

    private suspend fun refresh(windows: StatisticsWindows): StatisticsLoadResult =
        supervisorScope {
            val countJobs = windows.all.associate { window ->
                window.period to async { attempt { remote.count(window, THRESHOLD) } }
            }
            val strongestJob = async {
                attempt {
                    remote.events(
                        window = windows.today,
                        threshold = THRESHOLD,
                        orderBy = "magnitude",
                        limit = 1
                    ).features.firstOrNull()?.toStatisticsEvent()
                }
            }

            val countResults = countJobs.mapValues { it.value.await() }
            val strongestResult = strongestJob.await()
            val nearestResult = when (val todayCount = countResults.getValue(StatisticsPeriod.TODAY)) {
                is Attempt.Success -> attempt { findNearest(windows.today, todayCount.value) }
                is Attempt.Failure -> Attempt.Failure(todayCount.error)
            }

            val unavailable = buildSet {
                if (countResults[StatisticsPeriod.TODAY] is Attempt.Failure) add(StatisticsSection.TODAY_COUNT)
                if (countResults[StatisticsPeriod.LAST_7_DAYS] is Attempt.Failure) add(StatisticsSection.WEEK_COUNT)
                if (countResults[StatisticsPeriod.LAST_30_DAYS] is Attempt.Failure) add(StatisticsSection.MONTH_COUNT)
                if (countResults[StatisticsPeriod.YEAR] is Attempt.Failure) add(StatisticsSection.YEAR_COUNT)
                if (strongestResult is Attempt.Failure) add(StatisticsSection.STRONGEST)
                if (nearestResult is Attempt.Failure) add(StatisticsSection.NEAREST)
            }

            val successfulSections = countResults.values.count { it is Attempt.Success } +
                listOf(strongestResult, nearestResult).count { it is Attempt.Success }
            if (successfulSections == 0) {
                val cause = (countResults.values.firstOrNull() as? Attempt.Failure)?.error
                throw EarthquakeStatisticsLoadException("Unable to load earthquake statistics", cause)
            }

            val snapshot = StatisticsSnapshot(
                counts = StatisticsCounts(
                    today = countResults[StatisticsPeriod.TODAY].valueOrNull(),
                    last7Days = countResults[StatisticsPeriod.LAST_7_DAYS].valueOrNull(),
                    last30Days = countResults[StatisticsPeriod.LAST_30_DAYS].valueOrNull(),
                    year = countResults[StatisticsPeriod.YEAR].valueOrNull()
                ),
                strongestToday = strongestResult.valueOrNull(),
                nearestToday = nearestResult.valueOrNull(),
                threshold = THRESHOLD,
                retrievedAt = windows.today.end,
                windows = windows
            )

            if (unavailable.isEmpty() && snapshot.counts.isComplete()) {
                cacheDao.replace(snapshot.toCacheEntity())
            }

            StatisticsLoadResult(
                snapshot = snapshot,
                unavailableSections = unavailable
            )
        }

    private suspend fun findNearest(window: StatisticsWindow, eventCount: Int): StatisticsEvent? {
        if (eventCount == 0) return null
        val settings = appPreferencesRepository.getCurrentSettings()
        val origin = if (settings.manualLocOn) settings.manualPosition else settings.userPosition
        if (!origin.isValid()) throw IllegalStateException("Effective location is unavailable")

        val pageOffsets = (1..eventCount step PAGE_SIZE).toList()
        val features = supervisorScope {
            pageOffsets.map { offset ->
                async {
                    remote.events(
                        window = window,
                        threshold = THRESHOLD,
                        orderBy = "time",
                        limit = minOf(PAGE_SIZE, eventCount - offset + 1),
                        offset = offset
                    ).features
                }
            }.awaitAll().flatten()
        }

        return features.mapNotNull { feature ->
            val latitude = feature.geometry.latitude ?: return@mapNotNull null
            val longitude = feature.geometry.longitude ?: return@mapNotNull null
            feature.toStatisticsEvent(
                distanceKm = haversineDistanceKm(
                    origin.latitude,
                    origin.longitude,
                    latitude,
                    longitude
                )
            )
        }.minByOrNull { requireNotNull(it.distanceKm) }
    }

    private suspend fun <T> attempt(block: suspend () -> T): Attempt<T> = try {
        Attempt.Success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Attempt.Failure(error)
    }

    private fun EQFeatureDTO.toStatisticsEvent(distanceKm: Double? = null): StatisticsEvent? {
        val magnitude = properties.mag ?: return null
        val place = properties.place ?: return null
        val time = properties.time ?: return null
        val latitude = geometry.latitude ?: return null
        val longitude = geometry.longitude ?: return null
        return StatisticsEvent(
            id = id,
            magnitude = magnitude,
            place = place,
            time = time,
            depthKm = geometry.depthKm,
            latitude = latitude,
            longitude = longitude,
            distanceKm = distanceKm
        )
    }

    private fun LatLng.isValid(): Boolean =
        latitude.isFinite() && longitude.isFinite() &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0

    private fun haversineDistanceKm(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double
    ): Double {
        val earthRadiusKm = 6_371.0
        val latitudeDelta = Math.toRadians(toLatitude - fromLatitude)
        val longitudeDelta = Math.toRadians(toLongitude - fromLongitude)
        val a = sin(latitudeDelta / 2) * sin(latitudeDelta / 2) +
            cos(Math.toRadians(fromLatitude)) * cos(Math.toRadians(toLatitude)) *
            sin(longitudeDelta / 2) * sin(longitudeDelta / 2)
        return earthRadiusKm * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun StatisticsCounts.isComplete(): Boolean =
        today != null && last7Days != null && last30Days != null && year != null

    private sealed interface Attempt<out T> {
        data class Success<T>(val value: T) : Attempt<T>
        data class Failure(val error: Exception) : Attempt<Nothing>
    }

    private fun <T> Attempt<T>?.valueOrNull(): T? = (this as? Attempt.Success)?.value

    private companion object {
        const val THRESHOLD = 2.5
        const val PAGE_SIZE = 20_000
        val CACHE_TTL: Duration = Duration.ofMinutes(15)
    }
}
