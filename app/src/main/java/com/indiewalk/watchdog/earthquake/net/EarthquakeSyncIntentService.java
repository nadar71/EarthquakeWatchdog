package com.indiewalk.watchdog.earthquake.net;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;

import com.indiewalk.watchdog.earthquake.SingletonProvider;

public class EarthquakeSyncIntentService extends IntentService {

    private static final String TAG = EarthquakeSyncIntentService.class.getSimpleName();

    public EarthquakeSyncIntentService() {
        super("EarthquakeSyncIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1,new Notification());  // need for issue : #92
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // inject the network data source for using its fetching remote data method
        EarthquakeNetworkDataSource networkDataSource = ((SingletonProvider) SingletonProvider.getsContext()).getNetworkDatasource();
        networkDataSource.fetchEarthquakeWrapper();
    }
}


/*
public class EarthquakeSyncIntentService extends JobIntentService { // JobIntentService need for issue : #92

    private static final String TAG = EarthquakeSyncIntentService.class.getSimpleName();

    public static final int JOB_ID = 1;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, EarthquakeSyncIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        // inject the network data source for using its fetching remote data method
        EarthquakeNetworkDataSource networkDataSource = ((SingletonProvider) SingletonProvider.getsContext()).getNetworkDatasource();
        networkDataSource.fetchEarthquakeWrapper();
    }
}
*/


