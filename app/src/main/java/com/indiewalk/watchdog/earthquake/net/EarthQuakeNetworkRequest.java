package com.indiewalk.watchdog.earthquake.net;

import android.util.Log;

import com.indiewalk.watchdog.earthquake.data.Earthquake;

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

/**
 * ---------------------------------------------------------------------------------------------
 * Class which do all the stuff for remote request to RESTFul service
 * USED INSIDE LOADER CALLBACK
 * ---------------------------------------------------------------------------------------------
 */

public class EarthQuakeNetworkRequest {

    // log tag definition
    private static final String TAG = EarthQuakeNetworkRequest.class.getName();
    // tmp data structure for returning results
    private ArrayList<Earthquake> earthquakes = null;


    /**
     * ---------------------------------------------------------------------------------------------
     * Fetch data from remote service.
     * Use createUrl, makeHttpRequest, extractFeatureFromJson
     * @param requestedUrl
     * @return ArrayList<Earthquake>
     * ---------------------------------------------------------------------------------------------
     */
    public ArrayList<Earthquake> fetchEarthquakeData(String requestedUrl){

        //decomment  simulate network latency for debugging
        /*
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        URL url = createUrl(requestedUrl);

        if (url==null) {
            Log.e(TAG, "Empty url ");
            return null;
        }

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "IO Problem in jsonResponse.", e);
        }

        Log.i(TAG, "fetchEarthquakeData: calling extractFeatureFromJson");
        // Extract relevant fields from the JSON response and create an {@link Event} object
        if ( (jsonResponse != null) && (!jsonResponse.isEmpty()) && (jsonResponse != "")   ) {
            earthquakes = extractFeatureFromJson(jsonResponse);
            return earthquakes;
        } else{
            return null;
        }
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Returns new URL object from the given string URL.
     * ---------------------------------------------------------------------------------------------
     */
    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error with creating URL", e);
            return null;
        }
        return url;
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Make an HTTP request to the given URL and return a String as the response.
     * ---------------------------------------------------------------------------------------------
     */
    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            Log.i(TAG, "makeHttpRequest: open remote connection.");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            // check http response before doing anything
            Log.i(TAG, "makeHttpRequest: check response code");
            int responseCode = checkHttpResponse(urlConnection);
            if (responseCode==200) {
                urlConnection.connect();
                // get byte input stream
                inputStream = urlConnection.getInputStream();
                // convert input stream to json
                jsonResponse = readFromStream(inputStream);
                Log.e(TAG, "Json Response : "+jsonResponse);
            }else{
                Log.e(TAG, "Response code not 200 : "+responseCode);
                jsonResponse = "";
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     * ---------------------------------------------------------------------------------------------
     */
    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();

        if (inputStream != null) {

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }

        }

        return output.toString();
    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Return a ArrayList of  {@link Earthquake} objects by parsing out information
     * about the first earthquake from the input earthquakeJSON string.
     *
     * Return the list of earthquake retrieved from remote
     * ---------------------------------------------------------------------------------------------
     */
    private ArrayList<Earthquake> extractFeatureFromJson(String earthquakeJSON) {
        // Create an empty ArrayList that we can start adding earthquakes to
        earthquakes = new ArrayList<>();

        // Try to parse earthquakeJSON
        try {

            // build up a list of Earthquake objects (from json "features" array) with the corresponding data.
            JSONObject jsonObject = new JSONObject(earthquakeJSON);

            JSONArray features    = jsonObject.getJSONArray("features");
            for(int i=0;i<features.length();i++){
                // get current earthquake record
                JSONObject currentEarthquake = features.getJSONObject(i);

                // get properties obj for current equake object
                JSONObject properties = currentEarthquake.getJSONObject("properties");

                double mag   = properties.getDouble("mag");
                String place = properties.getString("place");
                long   time  = properties.getLong("time");
                String url   = properties.getString("url");

                // get geometry obj for current equake object
                JSONObject geometry = currentEarthquake.getJSONObject("geometry");

                // get coordinates array from geometry obj for current equake object
                JSONArray coordinates = geometry.getJSONArray("coordinates");

                double longitude   = coordinates.getDouble(0);
                double latitude    = coordinates.getDouble(1);
                double depth       = coordinates.getDouble(2);


                // tmp earthquake obj for single item
                Log.d(TAG, "extractFeatureFromJson: longitude : "+longitude+" latitude : "+latitude+" depth : "+depth);
                Earthquake  tmp = new Earthquake(mag, place, time, url, longitude, latitude, depth,0);

                // add to list of all earthquakes
                earthquakes.add(tmp);
            }




        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(TAG, "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return http response code
     * @param urlConnection
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    private int checkHttpResponse(HttpURLConnection urlConnection){
        int responseCode = 0;
        try {
            responseCode = urlConnection.getResponseCode();
        }catch(IOException e){
            Log.e(TAG +".checkHttpResponse", "Problem getting HTTP response.", e);
        }
        return responseCode;
    }

}
