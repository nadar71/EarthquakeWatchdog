package com.example.android.quakereport;

import android.os.AsyncTask;
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


public class EarthquakeFetchDataAsyncTask extends AsyncTask<String, Void, ArrayList<Earthquake>> {

    // log tag definition
    private static final String LOG_TAG = EarthquakeFetchDataAsyncTask.class.getName();
    private EarthquakeActivity earthquakeActivity;
    private ArrayList<Earthquake> earthquakes = null;


    /**
     * Constructor to get the invoking activity reference
     * @param activity
     */
    public EarthquakeFetchDataAsyncTask(EarthquakeActivity activity){
        this.earthquakeActivity = activity;
        Log.i(LOG_TAG, "MainActivity reference set.");
    }

    @Override
    protected ArrayList<Earthquake> doInBackground(String... urls) {
        // Create URL object
        String requestedUrl = urls[0];
        URL url = createUrl(requestedUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Problem in jsonResponse.", e);
        }

        Log.i(LOG_TAG, "doInBackground: calling extractFeatureFromJson");
        // Extract relevant fields from the JSON response and create an {@link Event} object
        earthquakes = extractFeatureFromJson(jsonResponse);

        // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
        return earthquakes;

    }

    /**
     * Return the list of eartquakes fetched
     */
    @Override
    protected void onPostExecute(ArrayList<Earthquake> earthquakes) {
        earthquakeActivity.setEartquakesList(earthquakes);
        Log.i(LOG_TAG+".onPostExecute", "setEartquakesList called.");
        earthquakeActivity.updateListView();
        Log.i(LOG_TAG+".onPostExecute", "updateListView.");
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL", e);
            return null;
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            // check http response before doing anything
            Log.i(LOG_TAG, "makeHttpRequest: check response code");
            int responseCode = checkHttpResponse(urlConnection);
            if (responseCode==200) {
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }else{
                Log.e(LOG_TAG, "Response code not 200 : "+responseCode);
                jsonResponse = "";
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
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
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
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
     * Return an {@link Earthquake} object by parsing out information
     * about the first earthquake from the input earthquakeJSON string.
     */
    private ArrayList<Earthquake> extractFeatureFromJson(String earthquakeJSON) {
        // Create an empty ArrayList that we can start adding earthquakes to
        earthquakes = new ArrayList<>();

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // build up a list of Earthquake objects with the corresponding data.
            JSONObject jsonObject = new JSONObject(earthquakeJSON);
            JSONArray  features  = jsonObject.getJSONArray("features");

            for(int i=0;i<features.length();i++){
                // get current earthquake record
                JSONObject currentEarthquake = features.getJSONObject(i);
                // get properties obj for current eq
                JSONObject properties = currentEarthquake.getJSONObject("properties");

                double mag   = properties.getDouble("mag");
                String place = properties.getString("place");
                long time    = properties.getLong("time");
                String url   = properties.getString("url");

                // tmp earthquake obj for single item
                Earthquake  tmp = new Earthquake(mag, place, time, url);

                // add to list of all earthquakes
                earthquakes.add(tmp);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }


    /**
     * Return http response code
     * @param urlConnection
     * @return
     */
    private int checkHttpResponse(HttpURLConnection urlConnection){
        int responseCode = 0;
        try {
            responseCode = urlConnection.getResponseCode();
        }catch(IOException e){
            Log.e(LOG_TAG+".checkHttpResponse", "Problem getting HTTP response.", e);
        }
        return responseCode;
    }
}






