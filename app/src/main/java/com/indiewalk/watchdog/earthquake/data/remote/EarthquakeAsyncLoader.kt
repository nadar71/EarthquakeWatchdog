package com.indiewalk.watchdog.earthquake.data.remote

import android.content.AsyncTaskLoader
import android.content.Context
import android.util.Log

import com.indiewalk.watchdog.earthquake.domain.model.Earthquake
import com.indiewalk.watchdog.earthquake.data.local.db.EarthquakeDatabase
import com.indiewalk.watchdog.earthquake.core.util.GenericUtils
import it.abenergie.customerarea.core.utility.extensions.TAG

import java.util.ArrayList


// -------------------------------------------------------------------------------------------------
// Loading async equake data using loader
// -------------------------------------------------------------------------------------------------

class EarthquakeAsyncLoader(private val localContext: Context, // query url
 private val queryUrl: String?) : AsyncTaskLoader<List<Earthquake>>(localContext) {

    // db instance reference
    private val eqDb: EarthquakeDatabase?

    // tmp list for getting the intermediate result in extractFeatureFromJson
    private var earthquakes: ArrayList<Earthquake>? = null

    // Preferences value
    private val lat_s: String? = null
    private val lng_s: String? = null
    private val dist_unit: String? = null

    init {
        // init db instance to save data
        eqDb = EarthquakeDatabase.getDbInstance(localContext.applicationContext)
    }

    override fun onStartLoading() {
        Log.i(TAG, "onStartLoading: forceLoad on this loader.")
        forceLoad()
    }


    // Background thread
    override fun loadInBackground(): List<Earthquake>? {
        if (queryUrl == null) {
            return null
        }

        // create instance of request and collect the result in ArrayList<Earthquake>
        earthquakes = EarthQuakeNetworkRequest().fetchEarthquakeData(queryUrl)
        Log.i(TAG, "loadInBackground: loadInBackground ended, returning data requested.")

        // update with distance from user, distance unit each earthquake
        GenericUtils.setEqDistanceFromCurrentCoords(earthquakes, localContext)

        // delete previous results in db, only newest are valid
        eqDb!!.earthquakeDbDao().dropEarthquakeListTable()

        // save it in db for later use
        for (earthquake in earthquakes!!) {
            eqDb.earthquakeDbDao().insertEarthquake(earthquake)
        }
        return earthquakes
    }


}
