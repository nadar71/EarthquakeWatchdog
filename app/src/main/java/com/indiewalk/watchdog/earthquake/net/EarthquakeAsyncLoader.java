package com.indiewalk.watchdog.earthquake.net;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.UI.MainActivityEarthquakesList;
import com.indiewalk.watchdog.earthquake.data.Earthquake;
import com.indiewalk.watchdog.earthquake.data.EarthquakeDatabase;
import com.indiewalk.watchdog.earthquake.util.MyUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * -------------------------------------------------------------------------------------------------
 * Class for loading async equake data using loader
 * -------------------------------------------------------------------------------------------------
 */
public class EarthquakeAsyncLoader extends AsyncTaskLoader<List<Earthquake>> {

    // log tag definition
    private static final String TAG = EarthquakeAsyncLoader.class.getName();

    private Context context;

    // db instance reference
    private EarthquakeDatabase eqDb;

    // query url
    private String queryUrl;

    // tmp list for getting the intermediate result in extractFeatureFromJson
    private ArrayList<Earthquake> earthquakes = null;

    // Preferences value
    private String lat_s, lng_s, dist_unit;

    /**
     * ---------------------------------------------------------------------------------------------
     * Loader constructor, pass the
     * @param context  : context of the activity
     * @param url      : url to be queried for
     * ---------------------------------------------------------------------------------------------
     */
    public EarthquakeAsyncLoader(Context context, String url){
        super(context);
        this.context  = context;
        queryUrl      = url;

        // init db instance to save data
        eqDb = EarthquakeDatabase.getDbInstance(context.getApplicationContext());
    }

    @Override
    protected void onStartLoading() {
        Log.i(TAG, "onStartLoading: forceLoad on this loader.");
        forceLoad();
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Background thread
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    public List<Earthquake> loadInBackground() {
        if(queryUrl == null){
            return null;
        }

        // create instance of request and collect the result in ArrayList<Earthquake>
        earthquakes = new EarthQuakeNetworkRequest().fetchEarthquakeData(queryUrl);
        Log.i(TAG, "loadInBackground: loadInBackground ended, returning data requested.");

        // update with distance from user, distance unit each earthquake
        MyUtil.setEqDistanceFromCurrentCoords(earthquakes, context);

        // delete previous results in db, only newest are valid
        eqDb.earthquakeDbDao().dropEarthquakeListTable();

        // save it in db for later use
        for(Earthquake earthquake : earthquakes){
            eqDb.earthquakeDbDao().insertEarthquake(earthquake);
        }

        return earthquakes;
    }










}
