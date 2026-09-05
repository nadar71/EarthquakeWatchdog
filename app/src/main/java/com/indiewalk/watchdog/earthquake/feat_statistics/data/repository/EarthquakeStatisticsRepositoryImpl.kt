package com.indiewalk.watchdog.earthquake.feat_statistics.data.repository

import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.domain.repository.AppPreferencesRepository
import com.indiewalk.watchdog.earthquake.core.diagnostics.StatisticsLoadFailure
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeatureDTO
import com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsCacheDao
import com.indiewalk.watchdog.earthquake.feat_statistics.data.local.toCacheEntity
import com.indiewalk.watchdog.earthquake.feat_statistics.data.local.toSnapshot
import com.indiewalk.watchdog.earthquake.feat_statistics.data.remote.EarthquakeStatisticsRemoteDataSource
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsCounts
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.analysis.StatisticsInsightsAnalyzer
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsLoadResult
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsLocation
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
) : Exception(message, cause), StatisticsLoadFailure

class EarthquakeStatisticsRepositoryImpl(
    private val remote: EarthquakeStatisticsRemoteDataSource,
    private val cacheDao: StatisticsCacheDao,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val timeProvider: StatisticsTimeProvider,
    private val insightsAnalyzer: StatisticsInsightsAnalyzer
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
            val eventsResult = when (val monthCount = countResults.getValue(StatisticsPeriod.LAST_30_DAYS)) {
                is Attempt.Success -> attempt { loadEvents(windows.last30Days, monthCount.value) }
                is Attempt.Failure -> Attempt.Failure(monthCount.error)
            }
            val originResult = attempt { effectiveLocation() }
            val nearestResult = when {
                eventsResult is Attempt.Failure -> Attempt.Failure(eventsResult.error)
                originResult is Attempt.Failure -> Attempt.Failure(originResult.error)
                originResult.valueOrNull() == null -> Attempt.Failure(
                    IllegalStateException("Effective location is unavailable")
                )
                else -> attempt {
                    findNearest(
                        events = requireNotNull(eventsResult.valueOrNull()).filter {
                            it.time >= windows.today.start.toEpochMilli() &&
                                it.time <= windows.today.end.toEpochMilli()
                        },
                        origin = requireNotNull(originResult.valueOrNull())
                    )
                }
            }
            val insights = eventsResult.valueOrNull()?.let { events ->
                insightsAnalyzer.analyze(events, windows.last30Days, originResult.valueOrNull())
            }

            val unavailable = buildSet {
                if (countResults[StatisticsPeriod.TODAY] is Attempt.Failure) add(StatisticsSection.TODAY_COUNT)
                if (countResults[StatisticsPeriod.LAST_7_DAYS] is Attempt.Failure) add(StatisticsSection.WEEK_COUNT)
                if (countResults[StatisticsPeriod.LAST_30_DAYS] is Attempt.Failure) add(StatisticsSection.MONTH_COUNT)
                if (countResults[StatisticsPeriod.YEAR] is Attempt.Failure) add(StatisticsSection.YEAR_COUNT)
                if (strongestResult is Attempt.Failure) add(StatisticsSection.STRONGEST)
                if (nearestResult is Attempt.Failure) add(StatisticsSection.NEAREST)
                if (eventsResult is Attempt.Failure) add(StatisticsSection.INSIGHTS)
                if (eventsResult is Attempt.Failure || originResult.valueOrNull() == null) {
                    add(StatisticsSection.NEARBY_TREND)
                }
            }

            val successfulSections = countResults.values.count { it is Attempt.Success } +
                listOf(strongestResult, nearestResult, eventsResult).count { it is Attempt.Success }
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
                insights = insights,
                threshold = THRESHOLD,
                retrievedAt = windows.today.end,
                windows = windows
            )

            val onlyLocationSectionsUnavailable = unavailable.all { it in LOCATION_SECTIONS }
            if (
                snapshot.counts.isComplete() &&
                strongestResult is Attempt.Success &&
                insights != null &&
                onlyLocationSectionsUnavailable
            ) {
                cacheDao.replace(snapshot.toCacheEntity())
            }

            StatisticsLoadResult(
                snapshot = snapshot,
                unavailableSections = unavailable
            )
        }

    private suspend fun loadEvents(window: StatisticsWindow, eventCount: Int): List<StatisticsEvent> {
        if (eventCount == 0) return emptyList()
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

        return features.mapNotNull { it.toStatisticsEvent() }
    }

    private suspend fun effectiveLocation(): StatisticsLocation? {
        val settings = appPreferencesRepository.getCurrentSettings()
        val position = if (settings.manualLocOn) settings.manualPosition else settings.userPosition
        return position.takeIf { it.isValid() }?.let {
            StatisticsLocation(it.latitude, it.longitude)
        }
    }

    private fun findNearest(
        events: List<StatisticsEvent>,
        origin: StatisticsLocation
    ): StatisticsEvent? = events
        .filter { it.latitude.isFinite() && it.longitude.isFinite() }
        .map { event ->
            event.copy(
                distanceKm = haversineDistanceKm(
                    origin.latitude,
                    origin.longitude,
                    event.latitude,
                    event.longitude
                )
            )
        }
        .minByOrNull { requireNotNull(it.distanceKm) }

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
        val LOCATION_SECTIONS = setOf(StatisticsSection.NEAREST, StatisticsSection.NEARBY_TREND)
    }
}
