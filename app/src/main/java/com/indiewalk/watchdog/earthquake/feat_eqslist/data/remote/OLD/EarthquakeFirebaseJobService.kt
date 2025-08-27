package com.indiewalk.watchdog.earthquake.feat_eqslist.data.remote.OLD

import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.indiewalk.watchdog.earthquake.EarthquakeApp


class EarthquakeFirebaseJobService : JobService() {

    // Job entry point, called by the Job Dispatcher.
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        Log.d(TAG, "Earthquake remote fetching Job service started")
        val networkDataSource = (EarthquakeApp.getsContext() as EarthquakeApp).networkDatasource
        networkDataSource?.startFetchEarthquakeService()
        jobFinished(jobParameters, false)
        return true
    }


    override fun onStopJob(jobParameters: JobParameters): Boolean {
        return false
    }

    companion object {
        private val TAG = EarthquakeFirebaseJobService::class.java.simpleName
    }
}
