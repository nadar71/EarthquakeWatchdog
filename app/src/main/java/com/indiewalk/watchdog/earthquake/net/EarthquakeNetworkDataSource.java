package com.indiewalk.watchdog.earthquake.net;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.data.Earthquake;
import com.indiewalk.watchdog.earthquake.util.AppExecutors;
import com.indiewalk.watchdog.earthquake.util.MyUtil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class EarthquakeNetworkDataSource {

    private static final String TAG = EarthquakeNetworkDataSource.class.getSimpleName();

    // Synchronizing Interval with rest service for udpdated eq info
    private static final int    SYNC_INTERVAL_HOURS = 1;
    private static final int    SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    // available time window for job
    private static final int    SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;
    private static final String EARTHQUAKE_SYNC_TAG = "earthquakes-sync";

    // SharePreferences ref
    private SharedPreferences sharedPreferences;

    private String dateFilter;

    // Singleton stuff
    private static final Object LOCK = new Object();
    private static EarthquakeNetworkDataSource sInstance;
    private final Context context;

    private final AppExecutors executors;

    // Livedata for earthquake downloaded
    private final MutableLiveData<Earthquake[]> earthquakesDownloaded;

    private EarthquakeNetworkDataSource(Context context, AppExecutors executors) {
        this.context   = context;
        this.executors = executors;

        earthquakesDownloaded = new MutableLiveData<Earthquake[]>();

        // init shared preferences and get value
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        checkPreferences();
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Get singleton instance
     * ---------------------------------------------------------------------------------------------
     */
    public static EarthquakeNetworkDataSource getInstance(Context context, AppExecutors executors) {
        Log.d(TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new EarthquakeNetworkDataSource(context.getApplicationContext(), executors);
                Log.d(TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Getter for earthquakesDownloaded
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public LiveData<Earthquake[]> getEarthquakesData(){
        return earthquakesDownloaded;
    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Immediate sync using an IntentService for asynchronous execution.
     * Used by repository, of course.
     * ---------------------------------------------------------------------------------------------
     */
    public void startFetchEarthquakeService() {
        Intent intentToFetch = new Intent(context, EarthquakeSyncIntentService.class);
        context.startService(intentToFetch);
        Log.d(TAG, "startFetchEarthquakeService : Fetch erthquakes data Service EarthquakeSyncIntentService created.");
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Schedules job service for regular earthquakes data update.
     * ---------------------------------------------------------------------------------------------
     */
    public void scheduleRecurringFetchEarthquakeSync() {
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        // Create the Job
        Job syncEarthquakeAppJob = dispatcher.newJobBuilder()
                .setService(EarthquakeFirebaseJobService.class)
                .setTag(EARTHQUAKE_SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        // Schedule the Job with the dispatcher
        dispatcher.schedule(syncEarthquakeAppJob);
        Log.d(TAG, "Job scheduled");
    }





    /**
     * ---------------------------------------------------------------------------------------------
     * Real Connection to rest service
     * ---------------------------------------------------------------------------------------------
     */
    public void fetchEarthquakeWrapper(){
        ArrayList<Earthquake> earthquakes;
        String queryUrl = MyUtil.composeQueryUrl(dateFilter);
        earthquakes = new EarthQuakeNetworkRequest().fetchEarthquakeData(queryUrl);

        Earthquake[] arrEarthquakes = (Earthquake[])earthquakes.toArray();

        earthquakesDownloaded.postValue(arrEarthquakes);

    }




    /**
     * ---------------------------------------------------------------------------------------------
     * Get date filter for url request
     * ---------------------------------------------------------------------------------------------
     */
    private void checkPreferences() {

        // recover preferred date filter by param from prefs or set a default from string value
        dateFilter = sharedPreferences.getString(
                context.getString(R.string.settings_date_filter_key),
                context.getString(R.string.settings_date_filter_default));

        // check preferences safety
        safePreferencesValue();

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * making code more robust checking if for same reasons the default value stored are null or
     * not equals to none of the preferences stored values (e.g.  in case of key value change on code
     * but user saved with the previous one with previous app version )
     * ---------------------------------------------------------------------------------------------
     */
    private void safePreferencesValue() {

        SharedPreferences.Editor editor = sharedPreferences.edit();

        // date filter safe
        if (dateFilter.isEmpty() || dateFilter == null) {
            dateFilter = context.getString(R.string.settings_date_filter_default);
            editor.putString(context.getString(R.string.settings_date_filter_key),
                    context.getString(R.string.settings_date_filter_default));
        }

        if ((!dateFilter.equals(context.getString(R.string.settings_date_period_today_value))) &&
                (!dateFilter.equals(context.getString(R.string.settings_date_period_24h_value))) &&
                (!dateFilter.equals(context.getString(R.string.settings_date_period_48h_value))) &&
                (!dateFilter.equals(context.getString(R.string.settings_date_period_week_value))) &&
                (!dateFilter.equals(context.getString(R.string.settings_date_period_month_value)))
        ){
            dateFilter = context.getString(R.string.settings_date_filter_default);
            editor.putString(context.getString(R.string.settings_date_filter_key),
                    context.getString(R.string.settings_date_filter_default));
        }

    }




}
