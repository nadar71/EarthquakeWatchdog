package com.indiewalk.watchdog.earthquake.net

import android.app.IntentService
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService

import com.indiewalk.watchdog.earthquake.SingletonProvider


class EarthquakeSyncIntentService : JobIntentService() { // JobIntentService need for issue : #92

    override fun onHandleWork(intent: Intent) {
        // inject the network data source for using its fetching remote data method
        val networkDataSource = (SingletonProvider.getsContext() as SingletonProvider).networkDatasource
        networkDataSource?.fetchEarthquakeWrapper()
    }

    companion object {

        private val TAG = EarthquakeSyncIntentService::class.java.simpleName
        private val JOB_ID = 2

        fun enqueueWork(context: Context, intent: Intent) {
            JobIntentService.enqueueWork(context, EarthquakeSyncIntentService::class.java, JOB_ID, intent)
        }
    }
}



