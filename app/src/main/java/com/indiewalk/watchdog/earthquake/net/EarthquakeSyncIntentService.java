package com.indiewalk.watchdog.earthquake.net;

import android.app.IntentService;
import android.content.Intent;

import com.indiewalk.watchdog.earthquake.SingletonProvider;

public class EarthquakeSyncIntentService extends IntentService {

    private static final String TAG = EarthquakeSyncIntentService.class.getSimpleName();

    public EarthquakeSyncIntentService() {
        super("EarthquakeSyncIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        // inject the network data source for using its fetching remote data method
        EarthquakeNetworkDataSource networkDataSource = ((SingletonProvider) SingletonProvider.getsContext()).getNetworkDatasource();
        networkDataSource.fetchEarthquakeWrapper();
    }
}


