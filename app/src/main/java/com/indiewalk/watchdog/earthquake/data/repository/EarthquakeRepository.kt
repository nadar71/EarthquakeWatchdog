package com.indiewalk.watchdog.earthquake.data.repository

import androidx.lifecycle.LiveData
import android.content.Context
import android.util.Log

import com.indiewalk.watchdog.earthquake.AppEarthquake
import com.indiewalk.watchdog.earthquake.data.local.db.EarthquakeDatabase
import com.indiewalk.watchdog.earthquake.domain.model.Earthquake
import com.indiewalk.watchdog.earthquake.data.remote.EarthquakeNetworkDataSource
import com.indiewalk.watchdog.earthquake.core.util.AppExecutors
import com.indiewalk.watchdog.earthquake.core.util.GenericUtils

class EarthquakeRepository {
    private lateinit var networkDataSource: EarthquakeNetworkDataSource
    lateinit var executors: AppExecutors
    private var mInitialized = false

    private var eqDb: EarthquakeDatabase


    // Checks if there are eqs data in db, otherwise return true to request remote data
    private val isRequestDataNeeded: Boolean
        get() {
            val eqs = eqDb.earthquakeDbDao().loadAllNoLiveData()
            return if (eqs.size <= 0) true else false
        }


    // Return  the non-empty list of earthquake if any
    val earthquakesList: LiveData<List<Earthquake>>
        get() = loadAll()

    // standard constructor
    private constructor(earthquakeDatabase: EarthquakeDatabase) {
        this.eqDb = earthquakeDatabase
    }

    // constructor with data source
    private constructor(
        earthquakeDatabase: EarthquakeDatabase,
        networkDataSource: EarthquakeNetworkDataSource,
        executors: AppExecutors
    ) {
        this.eqDb = earthquakeDatabase
        this.networkDataSource = networkDataSource
        this.executors = executors

        // get a copy of data from data source, to keep them updated through observer
        val networkData = networkDataSource.earthquakesData

        // observe data from data source; in case of change, update all the db
        networkData.observeForever { newEqFromNetwork ->
            executors.diskIO().execute {
                Log.d(
                    TAG,
                    "EarthquakeRepository observer : " + "New values found, deletes previous and insert new ones. "
                )
                // Delete old and insert new data
                dropEarthquakeListTable()

                // update with distance from user, distance unit each earthquake
                GenericUtils.setEqDistanceFromCurrentCoords(
                    newEqFromNetwork,
                    AppEarthquake.getsContext() as AppEarthquake
                )

                if (newEqFromNetwork != null) {
                    eqDb.earthquakeDbDao().renewDataInsert(*newEqFromNetwork)
                }
                Log.d(TAG, "WeatherAppRepository observer : New values inserted. ")
            }
        }


    }


    // Creates periodic sync tasks and checks to see if an immediate sync is required. If an
    // immediate sync is required, this method will take care of making sure that sync occurs.
    @Synchronized
    fun initializeData() {

        // Db data initialized check : at every app start
        if (mInitialized) {
            Log.d(TAG, "initializeData: data already initialized, return")
            return
        }

        // if not initialized :
        mInitialized = true

        // Set Synchronizing data every SYNC_INTERVAL_HOURS; if already present replace
        networkDataSource.scheduleRecurringFetchEarthquakeSync()


        // try to fetch earthquakes remote data if needed
        executors.diskIO().execute {
            if (GenericUtils.isConnectionOk) {
                if (isRequestDataNeeded) {
                    Log.d(
                        TAG,
                        "initializeData: isFetchNeeded == true, run the intent from fetching data from remote"
                    )
                    startFetchEarthquakeService()
                }
            }/* else {
                return@executors.diskIO().execute
            }*/

        }
    }


    // Start IntentService of EarthquakeNetworkDataSource
    private fun startFetchEarthquakeService() {
        // call the intent service for retrieving network data daemon
        networkDataSource.startFetchEarthquakeService()
    }


    //----------------------------------------------------------------------------------------------
    //  QUERY
    //----------------------------------------------------------------------------------------------

    // retrieve all the eqs
    fun loadAll(): LiveData<List<Earthquake>> {
        initializeData()
        return eqDb.earthquakeDbDao().loadAll()
    }

    // retrieve all the eqs order by desc magnitude
    fun loadAll_orderby_desc_mag(min_mag: Double): LiveData<List<Earthquake>> {
        initializeData()
        return eqDb.earthquakeDbDao().loadAll_orderby_desc_mag(min_mag)
    }


    // retrieve all the eqs order by asc magnitude
    fun loadAll_orderby_asc_mag(min_mag: Double): LiveData<List<Earthquake>> {
        initializeData()
        return eqDb.earthquakeDbDao().loadAll_orderby_asc_mag(min_mag)
    }


    // retrieve all the eqs order by most recent (time desc)
    fun loadAll_orderby_most_recent(min_mag: Double): LiveData<List<Earthquake>> {
        initializeData()
        return eqDb.earthquakeDbDao().loadAll_orderby_most_recent(min_mag)
    }


    // retrieve all the eqs order by oldest (time asc)
    fun loadAll_orderby_oldest(min_mag: Double): LiveData<List<Earthquake>> {
        initializeData()
        return eqDb.earthquakeDbDao().loadAll_orderby_oldest(min_mag)
    }


    // retrieve all the eqs order by nearest to user
    fun loadAll_orderby_nearest(min_mag: Double): LiveData<List<Earthquake>> {
        initializeData()
        return eqDb.earthquakeDbDao().loadAll_orderby_nearest(min_mag)
    }

    // retrieve all the eqs order by furthest to user
    fun loadAll_orderby_furthest(min_mag: Double): LiveData<List<Earthquake>> {
        initializeData()
        return eqDb.earthquakeDbDao().loadAll_orderby_furthest(min_mag)
    }


    //----------------------------------------------------------------------------------------------
    //  INSERT
    //----------------------------------------------------------------------------------------------
    fun insertEarthquake(earthquake: Earthquake) {
        eqDb.earthquakeDbDao().insertEarthquake(earthquake)
    }


    //----------------------------------------------------------------------------------------------
    //  UPDATE
    //----------------------------------------------------------------------------------------------
    fun updatedAllEqsDistFromUser(equakes: List<Earthquake>?, context: Context) {
        executors.diskIO().execute {
            Log.d(TAG, "Updating eqs distances from current user location. ")

            /* // too slow
                    for(Earthquake eq:equakes) {
                        eqDb.earthquakeDbDao().updatedEqDistanceFromUser(eq.getUserDistance(),
                                eq.getId());
                    }*/

            // Delete old and insert new data
            dropEarthquakeListTable()

            // update with distance from user, distance unit each earthquake
            GenericUtils.setEqDistanceFromCurrentCoords(equakes, context)

            if (equakes != null) { // workaround for #97
                val equakes_array = equakes.toTypedArray()
                eqDb.earthquakeDbDao().renewDataInsert(*equakes_array)
                Log.d(
                    TAG,
                    "Updating eqs distances from current user location. : New values inserted. "
                )
            }
        }

    }


    //----------------------------------------------------------------------------------------------
    //  DELETE
    //----------------------------------------------------------------------------------------------

    // drop table : delete all table content
    fun dropEarthquakeListTable() {
        eqDb.earthquakeDbDao().dropEarthquakeListTable()
    }

    companion object {

        private val TAG = EarthquakeRepository::class.java.simpleName

        private var sInstance: EarthquakeRepository? = null


        // Get repo singleton instance for standard constructor
        fun getInstance(database: EarthquakeDatabase): EarthquakeRepository? {
            if (sInstance == null) {
                synchronized(EarthquakeRepository::class.java) {
                    if (sInstance == null) {
                        sInstance = EarthquakeRepository(database)
                    }
                }
            }
            return sInstance
        }


        // Get repo singleton instance for constructor with data source support
        fun getInstanceWithDataSource(
            earthquakeDatabase: EarthquakeDatabase,
            networkDataSource: EarthquakeNetworkDataSource,
            executors: AppExecutors
        ): EarthquakeRepository? {
            if (sInstance == null) {
                synchronized(EarthquakeRepository::class.java) {
                    if (sInstance == null) {
                        sInstance =
                            EarthquakeRepository(earthquakeDatabase, networkDataSource, executors)
                    }
                }
            }
            return sInstance
        }
    }


}
