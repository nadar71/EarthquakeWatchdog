package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.repository

import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.SaveResult
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.EarthquakeQueryParams

interface EQRepository {
    //------------------------------------------- API ----------------------------------------------
    // get earthquakes from api and save to db
    suspend fun fetchAndSaveDefault(): EQFeaturesCollectionDTO

    suspend fun getEarthquakesDefault(
        min_mag: Double,
        limit: Int = 200
    ): EQFeaturesCollectionDTO
    suspend fun getEarthquakesWithParams(params: EarthquakeQueryParams): EQFeaturesCollectionDTO

    //------------------------------------------- QUERY --------------------------------------------
    suspend  fun loadAllEQs(): MutableList<EQEntity>
    suspend  fun loadAllEQs_orderby_desc_mag(min_mag: Double): MutableList<EQEntity>
    suspend  fun loadAllEQs_orderby_asc_mag(min_mag: Double): MutableList<EQEntity>
    suspend  fun loadAll_orderby_min_mag(min_mag: Double): MutableList<EQEntity>
    suspend  fun loadAll_orderby_most_recent(min_mag: Double): MutableList<EQEntity>
    suspend  fun loadAll_orderby_oldest(min_mag: Double): MutableList<EQEntity>
    suspend  fun loadAll_orderby_nearest(min_mag: Double): MutableList<EQEntity>
    suspend  fun loadAll_orderby_furthest(min_mag: Double): MutableList<EQEntity>
    suspend  fun loadAllInBbox(
        west: Double, south: Double, east: Double, north: Double //, limit: Int = 200
    ): MutableList<EQEntity>
    //----------------------------------- INSERT/UPDATE --------------------------------------------
    suspend fun refreshDB(feed: EQFeaturesCollectionDTO): SaveResult
    suspend  fun upsertEarthquake(eqEntity: EQEntity)
    suspend  fun updatedEqDistanceFromUser(new_distance: Int, id: Int)
    //----------------------------------------- DROP TABLE -----------------------------------------
    suspend  fun dropEarthquakeListTable()
    suspend fun deleteOlderThan(cutoff: Long)
}