package com.indiewalk.watchdog.earthquake.net

import android.util.Log

import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.indiewalk.watchdog.earthquake.SingletonProvider


class EarthquakeFirebaseJobService : JobService() {


    /**
     * ---------------------------------------------------------------------------------------------
     * Job entry point, called by the Job Dispatcher.
     * @return  whether there is more work remaining.
     * ---------------------------------------------------------------------------------------------
     */
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        Log.d(TAG, "Earthquake remote fetching Job service started")

        val networkDataSource = (SingletonProvider.getsContext() as SingletonProvider).networkDatasource


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
