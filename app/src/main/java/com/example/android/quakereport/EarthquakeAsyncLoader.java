package com.example.android.quakereport;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for loading async eq data through loader
 */

public class EarthquakeAsyncLoader extends AsyncTaskLoader<List<Earthquake>> {

    // log tag definition
    private static final String LOG_TAG = EarthquakeAsyncLoader.class.getName();

    // query url
    private String queryUrl;

    // tmp list for getting the internediate result in extractFeatureFromJson
    private ArrayList<Earthquake> earthquakes = null;

    /**
     * Loader constructor, pass the
     * @param context  : context of the activity
     * @param url  : url to be queried for
     */
    public EarthquakeAsyncLoader(Context context, String url){
        super(context);
        queryUrl =url;
    }

    @Override
    protected void onStartLoading() {
        Log.i(LOG_TAG, "onStartLoading: forceLoad on this loader.");
        forceLoad();
    }


    /**
     * Background thread
     * @return
     */
    @Override
    public List<Earthquake> loadInBackground() {
        if(queryUrl == null){
            return null;
        }
        // create instance of request and collect the result
        earthquakes = new EarthQuakeQuery().fetchEarthquakeData(queryUrl);
        Log.i(LOG_TAG, "loadInBackground: loadInBackground ended, returning data requested.");
        return earthquakes;
    }





}
