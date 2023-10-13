package com.indiewalk.watchdog.earthquake.core.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import android.util.Log
import android.view.View

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.AppEarthquake
import com.indiewalk.watchdog.earthquake.presentation.ui.MainActivityEarthquakesList
import com.indiewalk.watchdog.earthquake.domain.model.Earthquake
import it.abenergie.customerarea.core.utility.extensions.TAG

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GenericUtils {
    // URL to query the USGS dataset for earthquake information
    const val USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query"


    // Check if internet connection is on
    val isConnectionOk: Boolean
        get() {
            val connManager = (AppEarthquake.getsContext() as AppEarthquake)
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netinfo = connManager.activeNetworkInfo
            if (netinfo != null && netinfo.isConnected) {
                Log.d(TAG, "Connections is down !")
                return true
            } else
                return false

        }
    // "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=6&limit=10"; // debug


    // Compose a query url starting from preferences parameters
    fun composeQueryUrl(dateFilter: String): String {
        var rootUri = Uri.parse(USGS_REQUEST_URL)
        val builder = rootUri.buildUpon()

        builder.appendQueryParameter("format", "geojson")

        // commented, it creates only problem, will be substituted with user preferred time range
        // builder.appendQueryParameter("limit",numEquakes);

        // calculate 30-days ago date and set as start date
        // String aMonthAgo = MyUtil.oldDate(30).toString();
        // builder.appendQueryParameter("starttime",aMonthAgo);

        val offset = Integer.parseInt(dateFilter)
        val rangeAgo = oldDate(offset)
        builder.appendQueryParameter("starttime", rangeAgo)

        return builder.toString()
    }

    // Convert degree angle in radiant
    private fun fromDegreeToRadiant(deg_angle: Double): Double {
        return deg_angle * Math.PI / 180
    }


    // Returning the distance between 2 points on a sphere throught the Haversine formula
    private fun haversineDistanceCalc(p1Lat: Double, p2Lat: Double, p1Lng: Double, p2Lng: Double): Double {
        val R = 6378137 // Earth’s mean radius in meter
        val dLat = fromDegreeToRadiant(p2Lat - p1Lat)
        val dLng = fromDegreeToRadiant(p2Lng - p1Lng)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(fromDegreeToRadiant(p1Lat)) *
                cos(fromDegreeToRadiant(p2Lat)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val d = R * c
        return d / 1000 // returns the distance in Km
    }


    // Convert km to miles
    fun fromKmToMiles(km: Double): Double {
        val MileInkm = 0.621371192
        return km * MileInkm
    }


    // Format date in a specific way and millisec  format
    fun formatDateFromMsec(dateMillisec: Long): String {
        // Date
        val date = Date(dateMillisec)
        println("date : $date")
        // Format Date
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy")
        return dateFormatter.format(date)
    }


    // Format time in a specific way and millisec  format
    fun formatTimeFromMsec(dateMillisec: Long): String {
        // Time
        val time = Date(dateMillisec)
        println("time : $time")
        // Format Time
        val timeFormatter = SimpleDateFormat("h:mm a")
        return timeFormatter.format(time)
    }


    // Extract only the digit with "." from a String
    fun returnDigit(s: String): String {
        return s.replace("[^0-9?!\\.]+".toRegex(), "")
    }


    // Extract only the char from a String
    fun returnChar(s: String): String {
        return s.replace("[0-9]+".toRegex(), "")
    }


    // Converting vector icon to bitmap one, to get used as marker icon (allow bitmap only)
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


    // Hide nav bar total
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


    // Return Past date by daysOffset
    fun oldDate(daysOffset: Int): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd ")
        val calReturn = Calendar.getInstance()
        calReturn.add(Calendar.DATE, -daysOffset)
        return dateFormat.format(calReturn.time)
    }

    // Restart current activity
    fun restartActivity(activity: Activity) {
        val mIntent = activity.intent
        activity.finish()
        activity.startActivity(mIntent)
    }


    // Return specific color value for specific magnitude values
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


    // Return specific vector image for specific magnitude values
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


    // Update last update field in preferences
    fun setLastUpdateField(context: Context): String {
        // store the last update time
        val lastUpdate = formatDateFromMsec(System.currentTimeMillis()) +
                " " +
                formatTimeFromMsec(System.currentTimeMillis())

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(context.getString(R.string.last_update), lastUpdate)
        editor.apply()

        return lastUpdate
    }


    // Update each equakes info with custom distance from user if any,with distance unit preferred.
    fun setEqDistanceFromCurrentCoords(earthquakes: List<Earthquake>?, context: Context) {

        // if (context == null) return

        // Check location coordinates from shared preferences.If not set, put default value
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

        //get preferences for check
        var lat_s = sharedPreferences.getString(context.getString(R.string.device_lat), java.lang.Double.toString(
            MainActivityEarthquakesList.DEFAULT_LAT))
        var lng_s = sharedPreferences.getString(context.getString(R.string.device_lng), java.lang.Double.toString(
            MainActivityEarthquakesList.DEFAULT_LNG))


        // set default coord if there are no one
        val editor = sharedPreferences.edit()
        if (lat_s!!.isEmpty()) {
            editor.putString(context.getString(R.string.device_lat), java.lang.Double.toString(
                MainActivityEarthquakesList.DEFAULT_LAT))
            editor.apply()
        }

        if (lng_s!!.isEmpty()) {
            editor.putString(context.getString(R.string.device_lng), java.lang.Double.toString(
                MainActivityEarthquakesList.DEFAULT_LNG))
            editor.apply()
        }

        // get user lat, lng
        lat_s = sharedPreferences.getString(context.getString(R.string.device_lat), java.lang.Double.toString(
            MainActivityEarthquakesList.DEFAULT_LAT))
        lng_s = sharedPreferences.getString(context.getString(R.string.device_lng), java.lang.Double.toString(
            MainActivityEarthquakesList.DEFAULT_LNG))

        // get distance unit choosen
        val dist_unit = sharedPreferences.getString(context.getString(R.string.settings_distance_unit_by_key),
                java.lang.Double.toString(R.string.settings_distance_unit_by_default.toDouble()))


        if (earthquakes != null) { // workaround for #97
            for (eq in earthquakes) {
                val userLat = java.lang.Double.valueOf(lat_s)
                val userLng = java.lang.Double.valueOf(lng_s)
                var distance = haversineDistanceCalc(userLat, eq.latitude,
                        userLng, eq.longitude).toInt()
                // convert in miles if needed
                if (dist_unit == context.getString(R.string.settings_mi_distance_unit_value)) {
                    distance = fromKmToMiles(distance.toDouble()).toInt()
                }

                Log.i(TAG, "setEqDistanceFromCurrentCoords: eq distance from user : $distance")
                // set in equake
                eq.userDistance = distance
            }
        } else
            return


    }


    // Overloaded version of setEqDistanceFromCurrentCoords, now using Earthquake[] earthquakes
    fun setEqDistanceFromCurrentCoords(earthquakes: Array<Earthquake>?, context: Context) {
        // Check location coordinates from shared preferences.If not set, put default value
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

        //get preferences for check
        var lat_s = sharedPreferences.getString(context.getString(R.string.device_lat), java.lang.Double.toString(
            MainActivityEarthquakesList.DEFAULT_LAT))
        var lng_s = sharedPreferences.getString(context.getString(R.string.device_lng), java.lang.Double.toString(
            MainActivityEarthquakesList.DEFAULT_LNG))


        // set default coord if there are no one
        val editor = sharedPreferences.edit()
        if (lat_s!!.isEmpty()) {
            editor.putString(context.getString(R.string.device_lat), java.lang.Double.toString(
                MainActivityEarthquakesList.DEFAULT_LAT))
            editor.apply()
        }

        if (lng_s!!.isEmpty()) {
            editor.putString(context.getString(R.string.device_lng), java.lang.Double.toString(
                MainActivityEarthquakesList.DEFAULT_LNG))
            editor.apply()
        }

        // get user lat, lng
        lat_s = sharedPreferences.getString(context.getString(R.string.device_lat), java.lang.Double.toString(
            MainActivityEarthquakesList.DEFAULT_LAT))
        lng_s = sharedPreferences.getString(context.getString(R.string.device_lng), java.lang.Double.toString(
            MainActivityEarthquakesList.DEFAULT_LNG))

        // get distance unit choosen
        val dist_unit = sharedPreferences.getString(context.getString(R.string.settings_distance_unit_by_key),
                java.lang.Double.toString(R.string.settings_distance_unit_by_default.toDouble()))


        if (earthquakes != null) {
            for (eq in earthquakes) {
                val userLat = java.lang.Double.valueOf(lat_s)!!
                val userLng = java.lang.Double.valueOf(lng_s)!!
                var distance = haversineDistanceCalc(userLat, eq.latitude,
                        userLng, eq.longitude).toInt()
                // convert in miles if needed
                if (dist_unit == context.getString(R.string.settings_mi_distance_unit_value)) {
                    distance = fromKmToMiles(distance.toDouble()).toInt()
                }

                Log.i(TAG, "setEqDistanceFromCurrentCoords: eq distance from user : $distance")
                // set in equake
                eq.userDistance = distance
            }
        }

    }


    // Add a day in ol Date format
    fun addDays(date: Date, numDays: Int): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DATE, numDays)
        return cal.time
    }


}
