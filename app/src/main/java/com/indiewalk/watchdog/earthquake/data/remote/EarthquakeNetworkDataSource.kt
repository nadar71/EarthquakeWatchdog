package com.indiewalk.watchdog.earthquake.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log

import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Lifetime
import com.firebase.jobdispatcher.Trigger
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.domain.model.Earthquake
import com.indiewalk.watchdog.earthquake.core.util.AppExecutors
import com.indiewalk.watchdog.earthquake.core.util.MyUtil

import java.util.ArrayList
import java.util.concurrent.TimeUnit

class EarthquakeNetworkDataSource private constructor(private val context: Context, private val executors: AppExecutors) {

    // SharePreferences ref
    private val sharedPreferences: SharedPreferences

    private var dateFilter: String? = null

    // Livedata for earthquake downloaded
    private val earthquakesDownloaded: MutableLiveData<Array<Earthquake>>

    /**
     * ---------------------------------------------------------------------------------------------
     * Getter for earthquakesDownloaded
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    val earthquakesData: LiveData<Array<Earthquake>>
        get() = earthquakesDownloaded

    init {

        earthquakesDownloaded = MutableLiveData()

        // init shared preferences and get value
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        checkPreferences()
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Immediate sync using an IntentService for asynchronous execution.
     * Used by repository, of course.
     * ---------------------------------------------------------------------------------------------
     */
    fun startFetchEarthquakeService() {

        // need for issue : #92
        val intentForFetching = Intent(context, EarthquakeSyncIntentService::class.java)
        EarthquakeSyncIntentService.enqueueWork(context, intentForFetching)

        Log.d(TAG, "startFetchEarthquakeService : Fetch erthquakes data Service EarthquakeSyncIntentService created.")
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Schedules job service for regular earthquakes data update.
     * ---------------------------------------------------------------------------------------------
     */
    fun scheduleRecurringFetchEarthquakeSync() {
        val driver = GooglePlayDriver(context)
        val dispatcher = FirebaseJobDispatcher(driver)

        // Create the Job
        val syncEarthquakeAppJob = dispatcher.newJobBuilder()
                .setService(EarthquakeFirebaseJobService::class.java)
                .setTag(EARTHQUAKE_SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build()

        // Schedule the Job with the dispatcher
        dispatcher.schedule(syncEarthquakeAppJob)
        Log.d(TAG, "Job scheduled")
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Real Connection to rest service
     * ---------------------------------------------------------------------------------------------
     */
    fun fetchEarthquakeWrapper() {
        val earthquakes: ArrayList<Earthquake>?
        val queryUrl = MyUtil.composeQueryUrl(dateFilter!!)
        earthquakes = EarthQuakeNetworkRequest().fetchEarthquakeData(queryUrl)

        // Update last update field in preferences
        MyUtil.setLastUpdateField(context)

        val arrEarthquakes = earthquakes!!.toTypedArray()

        // post data to livedata
        earthquakesDownloaded.postValue(arrEarthquakes)
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Get date filter for url request
     * ---------------------------------------------------------------------------------------------
     */
    private fun checkPreferences() {

        // recover preferred date filter by param from prefs or set a default from string value
        dateFilter = sharedPreferences.getString(
                context.getString(R.string.settings_date_filter_key),
                context.getString(R.string.settings_date_filter_default))

        // check preferences safety
        safePreferencesValue()

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * making code more robust checking if for same reasons the default value stored are null or
     * not equals to none of the preferences stored values (e.g.  in case of key value change on code
     * but user saved with the previous one with previous app version )
     * ---------------------------------------------------------------------------------------------
     */
    private fun safePreferencesValue() {

        val editor = sharedPreferences.edit()

        // date filter safe
        if (dateFilter!!.isEmpty() || dateFilter == null) {
            dateFilter = context.getString(R.string.settings_date_filter_default)
            editor.putString(context.getString(R.string.settings_date_filter_key),
                    context.getString(R.string.settings_date_filter_default))
        }

        if (dateFilter != context.getString(R.string.settings_date_period_today_value) &&
                dateFilter != context.getString(R.string.settings_date_period_24h_value) &&
                dateFilter != context.getString(R.string.settings_date_period_48h_value) &&
                dateFilter != context.getString(R.string.settings_date_period_week_value) &&
                dateFilter != context.getString(R.string.settings_date_period_2_week_value)) {
            dateFilter = context.getString(R.string.settings_date_filter_default)
            editor.putString(context.getString(R.string.settings_date_filter_key),
                    context.getString(R.string.settings_date_filter_default))
        }// #68 && (!dateFilter.equals(context.getString(R.string.settings_date_period_month_value)))

    }

    companion object {

        private val TAG = EarthquakeNetworkDataSource::class.java.simpleName

        // Synchronizing Interval with rest service for udpdated eq info
        private val SYNC_INTERVAL_HOURS = 1
        private val SYNC_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS.toLong()).toInt() // 60; //
        // available time window for job
        private val SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3
        private val EARTHQUAKE_SYNC_TAG = "earthquakes-sync"

        // Singleton stuff
        private val LOCK = Any()
        private var sInstance: EarthquakeNetworkDataSource? = null

        /**
         * ---------------------------------------------------------------------------------------------
         * Get singleton instance
         * ---------------------------------------------------------------------------------------------
         */
        fun getInstance(context: Context, executors: AppExecutors): EarthquakeNetworkDataSource? {
            Log.d(TAG, "Getting the network data source")
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = EarthquakeNetworkDataSource(context.applicationContext, executors)
                    Log.d(TAG, "Made new network data source")
                }
            }
            return sInstance
        }
    }


}
