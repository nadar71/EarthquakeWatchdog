package com.indiewalk.watchdog.earthquake.net;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.JobIntentService;

import com.indiewalk.watchdog.earthquake.SingletonProvider;


public class EarthquakeSyncIntentService extends JobIntentService { // JobIntentService need for issue : #92

    private static final String TAG = EarthquakeSyncIntentService.class.getSimpleName();
    private static final int JOB_ID = 2;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, EarthquakeSyncIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        // inject the network data source for using its fetching remote data method
        EarthquakeNetworkDataSource networkDataSource = ((SingletonProvider) SingletonProvider.getsContext()).getNetworkDatasource();
        networkDataSource.fetchEarthquakeWrapper();
    }
}



