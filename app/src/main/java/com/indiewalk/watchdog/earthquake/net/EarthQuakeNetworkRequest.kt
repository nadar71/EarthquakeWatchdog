package com.indiewalk.watchdog.earthquake.net

import android.util.Log

import com.indiewalk.watchdog.earthquake.data.Earthquake

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.util.ArrayList

/**
 * ---------------------------------------------------------------------------------------------
 * Class which do all the stuff for remote request to RESTFul service
 * USED INSIDE LOADER CALLBACK and...
 * ---------------------------------------------------------------------------------------------
 */

class EarthQuakeNetworkRequest {
    // tmp data structure for returning results
    private var earthquakes: ArrayList<Earthquake>? = null


    /**
     * ---------------------------------------------------------------------------------------------
     * Fetch data from remote service.
     * Use createUrl, makeHttpRequest, extractFeatureFromJson
     * @param requestedUrl
     * @return ArrayList<Earthquake>
     * ---------------------------------------------------------------------------------------------
    </Earthquake> */
    fun fetchEarthquakeData(requestedUrl: String): ArrayList<Earthquake>? {

        //decomment  simulate network latency for debugging
        /*
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        val url = createUrl(requestedUrl)

        if (url == null) {
            Log.e(TAG, "Empty url ")
            return null
        }

        // Perform HTTP request to the URL and receive a JSON response back
        var jsonResponse: String? = ""
        try {
            jsonResponse = makeHttpRequest(url)
        } catch (e: IOException) {
            Log.e(TAG, "IO Problem in jsonResponse.", e)
        }

        Log.i(TAG, "fetchEarthquakeData: calling extractFeatureFromJson")
        // Extract relevant fields from the JSON response and create an {@link Event} object
        if (jsonResponse != null && !jsonResponse.isEmpty() && jsonResponse !== "") {
            earthquakes = extractFeatureFromJson(jsonResponse)
            return earthquakes
        } else {
            return null
        }
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Returns new URL object from the given string URL.
     * ---------------------------------------------------------------------------------------------
     */
    private fun createUrl(stringUrl: String): URL? {
        var url: URL? = null
        try {
            url = URL(stringUrl)
        } catch (e: MalformedURLException) {
            Log.e(TAG, "Error with creating URL", e)
            return null
        }

        return url
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Make an HTTP request to the given URL and return a String as the response.
     * ---------------------------------------------------------------------------------------------
     */
    @Throws(IOException::class)
    private fun makeHttpRequest(url: URL): String {
        var jsonResponse = ""
        var urlConnection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            Log.i(TAG, "makeHttpRequest: open remote connection.")
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.readTimeout = 10000
            urlConnection.connectTimeout = 15000
            // check http response before doing anything
            Log.i(TAG, "makeHttpRequest: check response code")
            val responseCode = checkHttpResponse(urlConnection)
            if (responseCode == 200) {
                urlConnection.connect()
                // get byte input stream
                inputStream = urlConnection.inputStream
                // convert input stream to json
                jsonResponse = readFromStream(inputStream)
                Log.e(TAG, "Json Response : $jsonResponse")
            } else {
                Log.e(TAG, "Response code not 200 : $responseCode")
                jsonResponse = ""
            }
        } catch (e: IOException) {
            Log.e(TAG, "Problem making the HTTP request.", e)
        } finally {
            urlConnection?.disconnect()
            inputStream?.close()
        }
        return jsonResponse
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Convert the [InputStream] into a String which contains the
     * whole JSON response from the server.
     * ---------------------------------------------------------------------------------------------
     */
    @Throws(IOException::class)
    private fun readFromStream(inputStream: InputStream?): String {
        val output = StringBuilder()

        if (inputStream != null) {

            val inputStreamReader = InputStreamReader(inputStream, Charset.forName("UTF-8"))
            val reader = BufferedReader(inputStreamReader)
            var line: String? = reader.readLine()
            while (line != null) {
                output.append(line)
                line = reader.readLine()
            }

        }

        return output.toString()
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return a ArrayList of  [Earthquake] objects by parsing out information
     * about the first earthquake from the input earthquakeJSON string.
     *
     * Return the list of earthquake retrieved from remote
     * ---------------------------------------------------------------------------------------------
     */
    private fun extractFeatureFromJson(earthquakeJSON: String): ArrayList<Earthquake> {
        // Create an empty ArrayList that we can start adding earthquakes to
        earthquakes = ArrayList()

        // Try to parse earthquakeJSON
        try {

            // build up a list of Earthquake objects (from json "features" array) with the corresponding data.
            val jsonObject = JSONObject(earthquakeJSON)

            val features = jsonObject.getJSONArray("features")
            for (i in 0 until features.length()) {
                // get current earthquake record
                val currentEarthquake = features.getJSONObject(i)

                // get properties obj for current equake object
                val properties = currentEarthquake.getJSONObject("properties")

                val mag = properties.getDouble("mag")
                val place = properties.getString("place")
                val time = properties.getLong("time")
                val url = properties.getString("url")

                // get geometry obj for current equake object
                val geometry = currentEarthquake.getJSONObject("geometry")

                // get coordinates array from geometry obj for current equake object
                val coordinates = geometry.getJSONArray("coordinates")

                val longitude = coordinates.getDouble(0)
                val latitude = coordinates.getDouble(1)
                val depth = coordinates.getDouble(2)


                // tmp earthquake obj for single item
                Log.d(TAG, "extractFeatureFromJson: longitude : $longitude latitude : $latitude depth : $depth")
                val tmp = Earthquake(mag, place, time, url, longitude, latitude, depth, 0)

                // add to list of all earthquakes
                earthquakes!!.add(tmp)
            }


        } catch (e: JSONException) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(TAG, "Problem parsing the earthquake JSON results", e)
        }

        // Return the list of earthquakes
        return earthquakes as ArrayList<Earthquake>
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return http response code
     * @param urlConnection
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    private fun checkHttpResponse(urlConnection: HttpURLConnection): Int {
        var responseCode = 0
        try {
            responseCode = urlConnection.responseCode
        } catch (e: IOException) {
            Log.e("$TAG.checkHttpResponse", "Problem getting HTTP response.", e)
        }

        return responseCode
    }

    companion object {

        // log tag definition
        private val TAG = EarthQuakeNetworkRequest::class.java.name
    }

}
