package com.indiewalk.watchdog.earthquake.data.remote

import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.indiewalk.watchdog.earthquake.AppEarthquake


class EarthquakeFirebaseJobService : JobService() {

    // Job entry point, called by the Job Dispatcher.
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        Log.d(TAG, "Earthquake remote fetching Job service started")
        val networkDataSource = (AppEarthquake.getsContext() as AppEarthquake).networkDatasource
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
