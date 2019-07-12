package com.indiewalk.watchdog.earthquake.net;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.UI.MainActivity;
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
        setUserPreferences(earthquakes);

        // delete previous results in db, only newest are valid
        eqDb.earthquakeDbDao().dropEarthquakeListTable();

        // save it in db for later use
        for(Earthquake earthquake : earthquakes){
            eqDb.earthquakeDbDao().insertEarthquake(earthquake);
        }

        // TODO : check if there are specific visualization preferences for showing equakes
        // TODO : do not forget to clear the  list before retrieving data

        // return to main activity for list view
        // TODO : return true
        return earthquakes;
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Update each equakes info with custom distance from user if any.
     * Update with distance unit preferred.
     * ---------------------------------------------------------------------------------------------
     */
     private void setUserPreferences(ArrayList<Earthquake> earthquakes){

         // Check location coordinates from shared preferences.If not set, put default value

         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

         //get preferences for check
         lat_s = sharedPreferences.getString(context.getString(R.string.device_lat),Double.toString(MainActivity.DEFAULT_LAT));
         lng_s = sharedPreferences.getString(context.getString(R.string.device_lng),Double.toString(MainActivity.DEFAULT_LNG));


         // set default coord if there are no one
         SharedPreferences.Editor editor = sharedPreferences.edit();
         if (lat_s.isEmpty() == true) {
             editor.putString(context.getString(R.string.device_lat), Double.toString(MainActivity.DEFAULT_LAT));
             editor.apply();
         }

         if (lng_s.isEmpty() == true) {
             editor.putString(context.getString(R.string.device_lng), Double.toString(MainActivity.DEFAULT_LNG));
             editor.apply();
         }

         // get user lat, lng
         lat_s = sharedPreferences.getString(context.getString(R.string.device_lat),Double.toString(MainActivity.DEFAULT_LAT));
         lng_s = sharedPreferences.getString(context.getString(R.string.device_lng),Double.toString(MainActivity.DEFAULT_LNG));

         // get distance unit choosen
         dist_unit = sharedPreferences.getString(context.getString(R.string.settings_distance_unit_by_key),
                 Double.toString(R.string.settings_distance_unit_by_default));


         for(Earthquake eq : earthquakes){
             double userLat = Double.valueOf(lat_s);
             double userLng = Double.valueOf(lng_s);
             int distance   = (int) MyUtil.haversineDistanceCalc(userLat, eq.getLatitude(),
                                                           userLng, eq.getLongitude());
             // convert in miles if needed
             if (dist_unit.equals(context.getString(R.string.settings_mi_distance_unit_value))){
                 distance = (int) MyUtil.fromKmToMiles(distance);
             }

             Log.i(TAG, "setUserPreferences: eq distance from user : "+distance);
             // set in equake
             eq.setUserDistance(distance);
         }


     }







}
