package com.indiewalk.watchdog.earthquake

import android.location.Address
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.domain.repository.AppPreferencesRepository
import com.indiewalk.watchdog.earthquake.core.domain.repository.FilterPreferencesRepository
import com.indiewalk.watchdog.earthquake.core.domain.repository.LocationRepository
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.FilterSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.SaveResult
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EarthquakeQueryParams
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.repository.EQRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAppPreferencesRepository(
    initialSettings: AppSettings = AppSettings(),
    initialAskedOnce: Boolean = false
) : AppPreferencesRepository {
    private val settingsState = MutableStateFlow(initialSettings)
    private val askedState = MutableStateFlow(initialAskedOnce)

    override val settingsFlow = settingsState.asStateFlow()
    override val askedLocationOnceFlow = askedState.asStateFlow()

    override suspend fun getCurrentSettings(): AppSettings = settingsState.value
    override suspend fun setThemeMode(themeMode: ThemeMode) {
        settingsState.value = settingsState.value.copy(mode = themeMode)
    }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        settingsState.value = settingsState.value.copy(unitSystem = unitSystem)
    }

    override suspend fun setAskedLocationOnce(value: Boolean) {
        askedState.value = value
    }

    override suspend fun setManualLocationOn(enabled: Boolean) {
        settingsState.value = settingsState.value.copy(manualLocOn = enabled)
    }

    override suspend fun setManualPosition(latLng: LatLng) {
        settingsState.value = settingsState.value.copy(manualPosition = latLng)
    }

    override suspend fun setManualLocationInfo(locationInfo: LocationInfo) {
        settingsState.value = settingsState.value.copy(manualLocationInfo = locationInfo)
    }

    override suspend fun setUserPosition(latLng: LatLng) {
        settingsState.value = settingsState.value.copy(userPosition = latLng)
    }

    override suspend fun setUserLocationInfo(locationInfo: LocationInfo) {
        settingsState.value = settingsState.value.copy(userLocationInfo = locationInfo)
    }

    override suspend fun setLastRefreshTime(lastRefreshTime: String) {
        settingsState.value = settingsState.value.copy(lastRefreshTime = lastRefreshTime)
    }
}

class FakeFilterPreferencesRepository(
    initial: FilterSettings = FilterSettings()
) : FilterPreferencesRepository {
    private val state = MutableStateFlow(initial)

    override val filterSettingsFlow = state.asStateFlow()

    override suspend fun updateFilters(
        sortOption: EqsSortOption,
        minMagnitude: MinMagnitude,
        timeInterval: TimeInterval
    ) {
        state.value = state.value.copy(
            sortOption = sortOption,
            minMag = minMagnitude,
            timeInterval = timeInterval,
            activeCounts = listOf(
                sortOption != EqsSortOption.DATE_DESC,
                minMagnitude != MinMagnitude.MAG_3_0,
                timeInterval != TimeInterval.LAST_30_DAYS
            ).count { it }
        )
    }

    override suspend fun setStartDate(date: String) {
        state.value = state.value.copy(startDate = date)
    }
}

class FakeLocationRepository(
    var lastKnownLatLng: LatLng? = sampleLatLng,
    var locationInfo: LocationInfo = sampleLocationInfo
) : LocationRepository {
    override suspend fun getLastKnownLatLng(): LatLng? = lastKnownLatLng
    override fun getAddress(latLng: LatLng): Address? = null
    override fun getLocationInfo(latLng: LatLng): LocationInfo = locationInfo
    override fun getLocationInfo(address: Address?): LocationInfo = locationInfo
}

class FakeEQRepository(
    initialEarthquakes: List<EQEntity> = emptyList(),
    private val fetchBlock: suspend FakeEQRepository.() -> Unit = {}
) : EQRepository {
    val earthquakes = MutableStateFlow(initialEarthquakes)
    var fetchCount = 0

    override suspend fun fetchAndSaveDefault(): EQFeaturesCollectionDTO {
        fetchCount += 1
        fetchBlock()
        throw NotImplementedError("DTO not needed in tests")
    }

    suspend fun succeedFetch() {
        fetchCount += 1
    }

    override suspend fun getEQsRemoteDefault(min_mag: Double, limit: Int): EQFeaturesCollectionDTO {
        error("unused")
    }

    override suspend fun getEQsRemoteWithParams(params: EarthquakeQueryParams): EQFeaturesCollectionDTO {
        error("unused")
    }

    override suspend fun observeAll(): Flow<List<EQEntity>> = earthquakes.asStateFlow()
    override suspend fun observeInBbox(west: Double, south: Double, east: Double, north: Double): Flow<List<EQEntity>> = earthquakes.asStateFlow()
    override suspend fun loadAllEQs(): MutableList<EQEntity> = earthquakes.value.toMutableList()
    override suspend fun loadAllEQs_orderby_desc_mag(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAllEQs_orderby_asc_mag(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAll_orderby_min_mag(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAll_orderby_most_recent(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAll_orderby_oldest(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAll_orderby_nearest(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAll_orderby_furthest(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAllInBbox(west: Double, south: Double, east: Double, north: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun refreshDB(feed: EQFeaturesCollectionDTO): SaveResult = SaveResult(0, 0)
    override suspend fun upsertEarthquake(eqEntity: EQEntity) = Unit
    override suspend fun updatedEqDistanceFromUser(new_distance: Int, id: Int) = Unit
    override suspend fun dropEarthquakeListTable() = Unit
    override suspend fun deleteOlderThan(cutoff: Long) = Unit
}

