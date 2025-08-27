package com.indiewalk.watchdog.earthquake.feat_eqslist.data.remote.OLD

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService

import com.indiewalk.watchdog.earthquake.EarthquakeApp


class EarthquakeSyncIntentService : JobIntentService() { // JobIntentService need for issue : #92

    override fun onHandleWork(intent: Intent) {
        // inject the network data source for using its fetching remote data method
        val networkDataSource = (EarthquakeApp.getsContext() as EarthquakeApp).networkDatasource
        networkDataSource?.fetchEarthquakeWrapper()
    }

    companion object {

        private val TAG = EarthquakeSyncIntentService::class.java.simpleName
        private val JOB_ID = 2

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                EarthquakeSyncIntentService::class.java,
                JOB_ID,
                intent
            )
        }
    }
}



