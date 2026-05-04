package com.indiewalk.watchdog.earthquake.feat_eqslist.data.repository

import com.indiewalk.watchdog.earthquake.FakeAppPreferencesRepository
import com.indiewalk.watchdog.earthquake.FakeFilterPreferencesRepository
import com.indiewalk.watchdog.earthquake.sampleEqEntity
import com.indiewalk.watchdog.earthquake.sampleEqFeed
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.EarthquakeDao
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.FeedSnapshotDao
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.FeedWriterDao
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.remote.EarthquakeApi
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.SaveResult
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQFeedSnapshotEntity
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class EQRepositoryImplTest {

    @Test
    fun `observeInBbox delegates to dao with same bounds`() = runRepositoryTest {
        val dao = RecordingEarthquakeDao()
        val expectedFlow = flowOf(listOf(sampleEqEntity(id = "bbox")))
        dao.observeInBboxFlow = expectedFlow
        val repository = createRepository(eqDao = dao)

        val actual = repository.observeInBbox(1.0, 2.0, 3.0, 4.0)

        assertSame(expectedFlow, actual)
        assertEquals(listOf(1.0, 2.0, 3.0, 4.0), dao.lastObservedBounds)
    }

    @Test
    fun `refreshDB clears previous data and writes snapshot plus events`() = runRepositoryTest {
        val dao = RecordingEarthquakeDao()
        val snapshotDao = RecordingFeedSnapshotDao()
        val writerDao = RecordingFeedWriterDao()
        val repository = createRepository(
            eqDao = dao,
            feedSnapshotDao = snapshotDao,
            feedWriterDao = writerDao
        )
        val feed = sampleEqFeed()

        val result = repository.refreshDB(feed)

        assertEquals(SaveResult(snapshotGenerated = 1234L, eventsUpserted = 1), result)
        assertEquals(1, snapshotDao.dropCalls)
        assertEquals(1, dao.dropCalls)
        assertEquals(1234L, writerDao.savedSnapshot?.generated)
        assertEquals(listOf("feature-1"), writerDao.savedEvents.map { it.id })
    }

    private fun createRepository(
        eqDao: RecordingEarthquakeDao = RecordingEarthquakeDao(),
        feedSnapshotDao: RecordingFeedSnapshotDao = RecordingFeedSnapshotDao(),
        feedWriterDao: RecordingFeedWriterDao = RecordingFeedWriterDao()
    ): EQRepositoryImpl = EQRepositoryImpl(
        eqDao = eqDao,
        feedSnapshotDao = feedSnapshotDao,
        feedWriterDao = feedWriterDao,
        earthquakeApi = EarthquakeApi(HttpClient(OkHttp)),
        appPreferencesRepository = FakeAppPreferencesRepository(),
        filterPreferencesRepository = FakeFilterPreferencesRepository()
    )
}

private fun runRepositoryTest(block: suspend TestScope.() -> Unit) =
    runTest(StandardTestDispatcher(TestCoroutineScheduler())) {
        block()
    }

private class RecordingEarthquakeDao : EarthquakeDao {
    var observeInBboxFlow: Flow<List<EQEntity>> = flowOf(emptyList())
    var lastObservedBounds: List<Double>? = null
    var dropCalls = 0

    override fun observeAll(): Flow<List<EQEntity>> = flowOf(emptyList())

    override fun observeInBbox(
        west: Double,
        south: Double,
        east: Double,
        north: Double
    ): Flow<List<EQEntity>> {
        lastObservedBounds = listOf(west, south, east, north)
        return observeInBboxFlow
    }

    override suspend fun loadAllEQs(): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAllEQs_orderby_desc_mag(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAllEQs_orderby_asc_mag(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAll_orderby_min_mag(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAll_orderby_most_recent(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAll_orderby_oldest(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAll_orderby_nearest(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAll_orderby_furthest(min_mag: Double): MutableList<EQEntity> = mutableListOf()
    override suspend fun loadAllInBbox(
        west: Double,
        south: Double,
        east: Double,
        north: Double,
        limit: Int
    ): MutableList<EQEntity> = mutableListOf()

    override suspend fun upsertEarthquake(eqEntity: EQEntity) = Unit
    override suspend fun updatedEqDistanceFromUser(new_distance: Int, id: Int) = Unit

    override suspend fun dropEarthquakeListTable() {
        dropCalls += 1
    }

    override suspend fun delete(eqEntity: EQEntity) = Unit
    override suspend fun deleteOlderThan(cutoff: Long) = Unit
}

private class RecordingFeedSnapshotDao : FeedSnapshotDao {
    var dropCalls = 0

    override suspend fun deleteSnapshot(EQFeedSnapshotEntity: EQFeedSnapshotEntity) = Unit

    override suspend fun dropSnapshotTable() {
        dropCalls += 1
    }
}

private class RecordingFeedWriterDao : FeedWriterDao {
    var savedSnapshot: EQFeedSnapshotEntity? = null
    var savedEvents: List<EQEntity> = emptyList()

    override suspend fun upsertSnapshot(snapshot: EQFeedSnapshotEntity) {
        savedSnapshot = snapshot
    }

    override suspend fun upsertAllEvents(events: List<EQEntity>) {
        savedEvents = events
    }
}
