import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.example.android.quakereport.Earthquake;

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
     * @param url  : url to be queried
     */
    public EarthquakeAsyncLoader(Context context, String url){
        super(context);
        queryUrl =url;
    }

    @Override
    protected void onStartLoading() {
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
        return extractFeatureFromJson(queryUrl);
    }




    //** METHODS USED INSIDE LOADER CALLBACK for retrieving data from RESTFull service
    // TODO : PUT IT IN A separate CLASS

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
     *
     * Return the list of earthquake retrieved from remote
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
            JSONArray features  = jsonObject.getJSONArray("features");

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
