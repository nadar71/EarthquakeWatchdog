package com.indiewalk.watchdog.earthquake.feat_eqslist.data.repository

import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.EarthquakeDao
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.FeedSnapshotDao
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.FeedWriterDao
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.remote.EarthquakeApi
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.SaveResult
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EarthquakeQueryParams
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.toEQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.toFeedSnapshot
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.repository.EQRepository
import javax.inject.Inject

class EQRepositoryImpl @Inject constructor(
    private val eqDao: EarthquakeDao,
    private val feedSnapshotDao: FeedSnapshotDao,
    private val feedWriterDao: FeedWriterDao,
    private val earthquakeApi: EarthquakeApi,
): EQRepository {

    //------------------------------------------- API ----------------------------------------------

    // todo: default one

    // fetch eqs and save locally,
    // default request : https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time
    // - min mag 1
    // - order by time desc ( default from remote)
    // - no start/date time: retrieve the last month ( ~170kb average )
    override suspend fun fetchAndSaveDefault(): EQFeaturesCollectionDTO {
        val params = EarthquakeQueryParams(
            format = "geojson",
            eventType = "earthquake",
            orderBy = "time",
            // startTime = Instant.now().minus(1, ChronoUnit.DAYS),
            // endTime = Instant.now(),
            // minMagnitude = 3.5,
            // limit = 200
        )
        val feed = earthquakeApi.fetchFeed(params)
        refreshDB(feed)
        return feed
    }



    override suspend fun getEarthquakesDefault(min_mag: Double, limit: Int): EQFeaturesCollectionDTO {
        val params = EarthquakeQueryParams(
            format = "geojson",
            eventType = "earthquake",
            orderBy = "time",
            // startTime = Instant.now().minus(1, ChronoUnit.DAYS),
            // endTime = Instant.now(),
            // minMagnitude = 3.5,
            // limit = 200
        )
        val feed = earthquakeApi.fetchFeed(params)
        return feed
    }


    override suspend fun getEarthquakesWithParams(params: EarthquakeQueryParams): EQFeaturesCollectionDTO {
        val params = EarthquakeQueryParams(
            format = params.format,
            eventType = params.eventType,
            orderBy = params.orderBy,
            startTime = params.startTime,
            endTime = params.endTime,
            minMagnitude = params.minMagnitude,
            limit = params.limit
        )
        val feed = earthquakeApi.fetchFeed(params)
        return feed
    }

    //------------------------------------------- QUERY --------------------------------------------

    override suspend fun loadAllEQs(): MutableList<EQEntity> {
        return eqDao.loadAllEQs()
    }

    override suspend fun loadAllEQs_orderby_desc_mag(min_mag: Double): MutableList<EQEntity> {
        return eqDao.loadAllEQs_orderby_desc_mag(min_mag)
    }

    override suspend fun loadAllEQs_orderby_asc_mag(min_mag: Double): MutableList<EQEntity> {
        return eqDao.loadAllEQs_orderby_asc_mag(min_mag)
    }

    override suspend fun loadAll_orderby_min_mag(min_mag: Double): MutableList<EQEntity> {
        return eqDao.loadAll_orderby_min_mag(min_mag)
    }

    override suspend fun loadAll_orderby_most_recent(min_mag: Double): MutableList<EQEntity> {
        return eqDao.loadAll_orderby_most_recent(min_mag)
    }

    override suspend fun loadAll_orderby_oldest(min_mag: Double): MutableList<EQEntity> {
        return eqDao.loadAll_orderby_oldest(min_mag)
    }

    override suspend fun loadAll_orderby_nearest(min_mag: Double): MutableList<EQEntity> {
        return eqDao.loadAll_orderby_nearest(min_mag)
    }

    override suspend fun loadAll_orderby_furthest(min_mag: Double): MutableList<EQEntity> {
        return eqDao.loadAll_orderby_furthest(min_mag)
    }

    override suspend fun loadAllInBbox(west: Double, south: Double,
                                       east: Double, north: Double): MutableList<EQEntity> {
        return eqDao.loadAllInBbox(west, south, east, north)
    }


    //----------------------------------- INSERT/UPDATE --------------------------------------------

    override suspend fun upsertEarthquake(eqEntity: EQEntity) {
        eqDao.upsertEarthquake(eqEntity)
    }

    override suspend fun updatedEqDistanceFromUser(new_distance: Int, id: Int) {
        eqDao.updatedEqDistanceFromUser(new_distance, id)
    }

    override suspend fun refreshDB(feed: EQFeaturesCollectionDTO): SaveResult {
        val snapshot = feed.toFeedSnapshot()
        val events = feed.features.map { it.toEQEntity(feedGenerated = snapshot.generated) }
        // clear tables
        feedSnapshotDao.dropSnapshotTable()
        eqDao.dropEarthquakeListTable()
        // write new data
        feedWriterDao.saveSnapshotAndEvents(snapshot, events)
        return SaveResult(snapshotGenerated = snapshot.generated, eventsUpserted = events.size)
    }

    //----------------------------------------- DROP TABLE -----------------------------------------

    override suspend fun dropEarthquakeListTable() {
        eqDao.dropEarthquakeListTable()
    }

    override suspend fun deleteOlderThan(cutoff: Long) {
        eqDao.deleteOlderThan(cutoff)
    }
}