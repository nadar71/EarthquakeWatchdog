package com.indiewalk.watchdog.earthquake.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Log
import android.view.View

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.SingletonProvider
import com.indiewalk.watchdog.earthquake.UI.MainActivityEarthquakesList
import com.indiewalk.watchdog.earthquake.data.Earthquake
import com.indiewalk.watchdog.earthquake.net.EarthquakeFirebaseJobService

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Calendar

object MyUtil {


    private val TAG = MyUtil::class.java.simpleName

    // URL to query the USGS dataset for earthquake information
    private val USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query"


    /**
     * ---------------------------------------------------------------------------------------------
     * Check if internet connection is on
     * ---------------------------------------------------------------------------------------------
     */
    // check connection
    // reference to connection manager
    // network status retrieving
    val isConnectionOk: Boolean
        get() {
            val connManager = (SingletonProvider.getsContext() as SingletonProvider)
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netinfo = connManager.activeNetworkInfo
            if (netinfo != null && netinfo.isConnected) {
                Log.d(TAG, "Connections is down !")
                return true
            } else
                return false

        }
    // "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=6&limit=10"; // debug


    /**
     * ---------------------------------------------------------------------------------------------
     * Compose a query url starting from preferences parameters
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    fun composeQueryUrl(dateFilter: String): String {
        val rootUri = Uri.parse(USGS_REQUEST_URL)
        val builder = rootUri.buildUpon()

        builder.appendQueryParameter("format", "geojson")

        // commented, it creates only problem, will be substituted with user preferred time range
        // builder.appendQueryParameter("limit",numEquakes);

        // calculate 30-days ago date and set as start date
        // String aMonthAgo = MyUtil.oldDate(30).toString();
        // builder.appendQueryParameter("starttime",aMonthAgo);

        val offset = Integer.parseInt(dateFilter)
        val rangeAgo = MyUtil.oldDate(offset)
        builder.appendQueryParameter("starttime", rangeAgo)

        return builder.toString()
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Convert degree angle in radiant
     * @param deg_angle
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    fun fromDegreeToRadiant(deg_angle: Double): Double {
        return deg_angle * Math.PI / 180
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Returning the distance between 2 points on a sphere throught the Haversine formula
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    fun haversineDistanceCalc(p1Lat: Double, p2Lat: Double, p1Lng: Double, p2Lng: Double): Double {
        val R = 6378137 // Earth’s mean radius in meter
        val dLat = fromDegreeToRadiant(p2Lat - p1Lat)
        val dLng = fromDegreeToRadiant(p2Lng - p1Lng)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(fromDegreeToRadiant(p1Lat)) *
                Math.cos(fromDegreeToRadiant(p2Lat)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val d = R * c
        return d / 1000 // returns the distance in Km
    }


    /**
     * Convert km to miles
     * @param km
     * @return
     */
    fun fromKmToMiles(km: Double): Double {
        val MileInkm = 0.621371192
        return km * MileInkm
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Format date in a specific way and millisec  format
     * @param dateMillisec
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    fun formatDateFromMsec(dateMillisec: Long): String {
        // Date
        val date = Date(dateMillisec)
        println("date : $date")

        // Format Date
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy")
        return dateFormatter.format(date)
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Format time in a specific way and millisec  format
     * @param dateMillisec
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    fun formatTimeFromMsec(dateMillisec: Long): String {
        // Time
        val time = Date(dateMillisec)
        println("time : $time")

        // Format Time
        val timeFormatter = SimpleDateFormat("h:mm a")
        return timeFormatter.format(time)
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Extract only the digit with "." from a String
     * ---------------------------------------------------------------------------------------------
     */
    fun returnDigit(s: String): String {
        return s.replace("[^0-9?!\\.]+".toRegex(), "")
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Extract only the char from a String
     * ---------------------------------------------------------------------------------------------
     */
    fun returnChar(s: String): String {
        return s.replace("[0-9]+".toRegex(), "")
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Converting vector icon to bitmap one, to get used as marker icon (allow bitmap only)
     * @param context
     * @param vectorResourceId
     * @param tintColor
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    fun getBitmapFromVector(context: Context,
                            @DrawableRes vectorResourceId: Int,
                            @ColorInt tintColor: Int): BitmapDescriptor {

        val vectorDrawable = ResourcesCompat.getDrawable(
                context.resources, vectorResourceId, null)
                ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, tintColor)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Hide nav bar total
     * @param activity
     * ---------------------------------------------------------------------------------------------
     */
    fun hideNavBar(activity: Activity) {
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return Past date by daysOffset
     * ---------------------------------------------------------------------------------------------
     */
    fun oldDate(daysOffset: Int): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd ")
        val calReturn = Calendar.getInstance()
        calReturn.add(Calendar.DATE, -daysOffset)
        return dateFormat.format(calReturn.time)
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Restart current activity
     * ---------------------------------------------------------------------------------------------
     */
    fun restartActivity(activity: Activity) {
        val mIntent = activity.intent
        activity.finish()
        activity.startActivity(mIntent)
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return specific color value for specific magnitude values
     * @param magnitude
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    fun getMagnitudeColor(magnitude: Double, context: Context): Int {
        val mag = magnitude.toInt()
        Log.i("getMagnitudeColor", "Color: $mag")
        when (mag) {
            1, 0 -> return ContextCompat.getColor(context, R.color.magnitude1)
            2 -> return ContextCompat.getColor(context, R.color.magnitude2)
            3 -> return ContextCompat.getColor(context, R.color.magnitude3)
            4 -> return ContextCompat.getColor(context, R.color.magnitude4)
            5 -> return ContextCompat.getColor(context, R.color.magnitude5)
            6 -> return ContextCompat.getColor(context, R.color.magnitude6)
            7 -> return ContextCompat.getColor(context, R.color.magnitude7)
            8 -> return ContextCompat.getColor(context, R.color.magnitude8)
            9 -> return ContextCompat.getColor(context, R.color.magnitude9)
            10 -> return ContextCompat.getColor(context, R.color.magnitude10plus)
            else -> {
            }
        }
        return -1
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return specific vector image for specific magnitude values
     * @param magnitude
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    fun getMagnitudeImg(magnitude: Double, context: Context): Drawable? {
        val mag = magnitude.toInt()
        Log.i("getMagnitudeColor", "Color: $mag")
        when (mag) {
            1, 0 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_1)
            2 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_2)
            3 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_3)
            4 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_4)
            5 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_5)
            6 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_6)
            7 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_7)
            8 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_8)
            9 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_9)
            10 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_10)
            else -> {
            }
        }
        return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer)
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Update last update field in preferences
     * ---------------------------------------------------------------------------------------------
     */
    fun setLastUpdateField(context: Context): String {
        // store the last update time
        val lastUpdate = MyUtil.formatDateFromMsec(System.currentTimeMillis()) +
                " " +
                MyUtil.formatTimeFromMsec(System.currentTimeMillis())

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(context.getString(R.string.last_update), lastUpdate)
        editor.apply()

        return lastUpdate
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Update each equakes info with custom distance from user if any.
     * Update with distance unit preferred.
     * Using List<Earthquake> earthquakes
     * ---------------------------------------------------------------------------------------------
    </Earthquake> */
    fun setEqDistanceFromCurrentCoords(earthquakes: List<Earthquake>?, context: Context) {

        // Check location coordinates from shared preferences.If not set, put default value
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

        //get preferences for check
        var lat_s = sharedPreferences.getString(context.getString(R.string.device_lat), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LAT))
        var lng_s = sharedPreferences.getString(context.getString(R.string.device_lng), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LNG))


        // set default coord if there are no one
        val editor = sharedPreferences.edit()
        if (lat_s!!.isEmpty() == true) {
            editor.putString(context.getString(R.string.device_lat), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LAT))
            editor.apply()
        }

        if (lng_s!!.isEmpty() == true) {
            editor.putString(context.getString(R.string.device_lng), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LNG))
            editor.apply()
        }

        // get user lat, lng
        lat_s = sharedPreferences.getString(context.getString(R.string.device_lat), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LAT))
        lng_s = sharedPreferences.getString(context.getString(R.string.device_lng), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LNG))

        // get distance unit choosen
        val dist_unit = sharedPreferences.getString(context.getString(R.string.settings_distance_unit_by_key),
                java.lang.Double.toString(R.string.settings_distance_unit_by_default.toDouble()))


        if (earthquakes != null) { // workaround for #97
            for (eq in earthquakes) {
                val userLat = java.lang.Double.valueOf(lat_s)!!
                val userLng = java.lang.Double.valueOf(lng_s)!!
                var distance = MyUtil.haversineDistanceCalc(userLat, eq.latitude,
                        userLng, eq.longitude).toInt()
                // convert in miles if needed
                if (dist_unit == context.getString(R.string.settings_mi_distance_unit_value)) {
                    distance = MyUtil.fromKmToMiles(distance.toDouble()).toInt()
                }

                Log.i(TAG, "setEqDistanceFromCurrentCoords: eq distance from user : $distance")
                // set in equake
                eq.userDistance = distance
            }
        } else
            return


    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Overloaded version of setEqDistanceFromCurrentCoords, now using Earthquake[] earthquakes
     * ---------------------------------------------------------------------------------------------
     */
    fun setEqDistanceFromCurrentCoords(earthquakes: Array<Earthquake>?, context: Context) {

        // Check location coordinates from shared preferences.If not set, put default value

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

        //get preferences for check
        var lat_s = sharedPreferences.getString(context.getString(R.string.device_lat), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LAT))
        var lng_s = sharedPreferences.getString(context.getString(R.string.device_lng), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LNG))


        // set default coord if there are no one
        val editor = sharedPreferences.edit()
        if (lat_s!!.isEmpty() == true) {
            editor.putString(context.getString(R.string.device_lat), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LAT))
            editor.apply()
        }

        if (lng_s!!.isEmpty() == true) {
            editor.putString(context.getString(R.string.device_lng), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LNG))
            editor.apply()
        }

        // get user lat, lng
        lat_s = sharedPreferences.getString(context.getString(R.string.device_lat), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LAT))
        lng_s = sharedPreferences.getString(context.getString(R.string.device_lng), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LNG))

        // get distance unit choosen
        val dist_unit = sharedPreferences.getString(context.getString(R.string.settings_distance_unit_by_key),
                java.lang.Double.toString(R.string.settings_distance_unit_by_default.toDouble()))


        for (eq in earthquakes) {
            val userLat = java.lang.Double.valueOf(lat_s)!!
            val userLng = java.lang.Double.valueOf(lng_s)!!
            var distance = MyUtil.haversineDistanceCalc(userLat, eq.latitude,
                    userLng, eq.longitude).toInt()
            // convert in miles if needed
            if (dist_unit == context.getString(R.string.settings_mi_distance_unit_value)) {
                distance = MyUtil.fromKmToMiles(distance.toDouble()).toInt()
            }

            Log.i(TAG, "setEqDistanceFromCurrentCoords: eq distance from user : $distance")
            // set in equake
            eq.userDistance = distance
        }

    }


}
