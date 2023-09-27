package com.indiewalk.watchdog.earthquake.presentation.components.adapter


import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.AppEarthquake
import com.indiewalk.watchdog.earthquake.domain.model.Earthquake
import com.indiewalk.watchdog.earthquake.data.repository.EarthquakeRepository
import com.indiewalk.watchdog.earthquake.core.util.MyUtil
import com.indiewalk.watchdog.earthquake.presentation.ui.MainActivityEarthquakesList


import java.text.DecimalFormat

class EarthquakeListAdapter
/**
 * ----------------------------------------------------------------------------------
 * Constructor :
 * @param context  the current Context
 * @param listener the ItemClickListener
 * ----------------------------------------------------------------------------------
 */
(private val context: Context, // Handle item clicks
 private val eqItemClickListener: ItemClickListener
) : RecyclerView.Adapter<EarthquakeListAdapter.EarthquakeViewRowHolder>() {

    private val TAG = EarthquakeListAdapter::class.java.name

    private val repository: EarthquakeRepository?

    internal var earthquakesEntries: MutableList<Earthquake>? = null

    private var primaryLocation: String? = null

    private var locationOffset: String? = null

    private var magnitude: Double = 0.toDouble()
    private val magnitudeColor: Int = 0

    private var dist_unit: String? = null


    init {

        locationOffset = context.resources.getString(R.string.locationOffset_label)

        // set preferred distance unit
        checkPreferences()

        repository = (AppEarthquake.getsContext() as AppEarthquake).repositoryWithDataSource
    }


    /**
     * ----------------------------------------------------------------------------------
     * Inflate list's each view/row layout.
     * @return new EarthquakeViewRowHolder
     * ----------------------------------------------------------------------------------
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarthquakeViewRowHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_ii, parent, false)

        return EarthquakeViewRowHolder(view)
    }


    /**
     * ----------------------------------------------------------------------------------
     * Connect each earthquake ViewHolder obj with their data
     * @param holder   ViewHolder to bind Cursor data to
     * @param position data position in Cursor
     * ----------------------------------------------------------------------------------
     */
    override fun onBindViewHolder(holder: EarthquakeViewRowHolder, position: Int) {

        // get the item in the current position
        val currentEartquakeItem = earthquakesEntries!![position]

        magnitude = currentEartquakeItem.getMagnitude()!!
        holder.magnitudeView.text = formatMagnitude(magnitude)

        // set proper image color for magnitude
        holder.magnitudeView.background = MyUtil.getMagnitudeImg(magnitude, context)

        // display locations formatted using {@link extractLocations}
        extractLocations(currentEartquakeItem.location!!)
        holder.locationOffsetView.text = locationOffset
        holder.primaryLocationView.text = primaryLocation

        // display date formatted using {@link formatDateFromMsec}
        holder.dateView.text = MyUtil.formatDateFromMsec(currentEartquakeItem.timeInMillisec)

        // display time formatted using {@link formatTimeFromMsec}
        holder.timeView.text = MyUtil.formatTimeFromMsec(currentEartquakeItem.timeInMillisec)

        // set distance label based on user location type
        val check = checkPreferences()
        if (check == true) { // custom location
            holder.distanceFromUser.text = "            " + currentEartquakeItem.userDistance + " " + dist_unit
            holder.distanceFromUser_label.text = context.getString(R.string.distance_from_user_location)
        } else if (check == false) {
            holder.distanceFromUser.text = currentEartquakeItem.userDistance.toString() + " " + dist_unit
            holder.distanceFromUser_label.text = context.getString(R.string.distance_from_default_location)
        }

    }


    override fun getItemCount(): Int {
        return if (earthquakesEntries == null) {
            0
        } else earthquakesEntries!!.size

    }


    /**
     * ----------------------------------------------------------------------------------
     * Set data for RecycleView as earthquakesEntries list.
     * Used by calling activity to init/update the adapter
     * @param earthquakesEntries
     * ----------------------------------------------------------------------------------
     */
    fun setEarthquakesEntries(earthquakesEntries: MutableList<Earthquake>) {
        // if  (this.earthquakesEntries != null) this.earthquakesEntries.clear();
        this.earthquakesEntries = earthquakesEntries

        //data changed, refresh the view : notify the related observers
        notifyDataSetChanged()
    }


    /**
     * ----------------------------------------------------------------------------------
     * Reset adapter earthquakesEntries list.
     * ----------------------------------------------------------------------------------
     */
    fun resetEarthquakesEntries() {
        if (earthquakesEntries != null) earthquakesEntries!!.clear()
    }

    // ----------------------------------------------------------------------------------
    // Implemented in calling class if needed
    // ----------------------------------------------------------------------------------
    interface ItemClickListener {
        fun onItemClickListener(v: View, position: Int)
    }


    /**
     * ----------------------------------------------------------------------------------
     * Return a eq item in list at defined position
     * ----------------------------------------------------------------------------------
     */
    fun getEqItemAtPosition(position: Int): Earthquake {
        return earthquakesEntries!![position]
    }


    /**
     * ----------------------------------------------------------------------------------
     * Get all the eq items list
     * ----------------------------------------------------------------------------------
     */
    fun getEarthquakesEntries(): List<Earthquake>? {
        return earthquakesEntries
    }


    inner class EarthquakeViewRowHolder// EarthquakeViewRowHolder Constructor
    // @param itemView view inflated in onCreateViewHolder
    (itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var magnitudeView: TextView
        var locationOffsetView: TextView
        var primaryLocationView: TextView
        var dateView: TextView
        var timeView: TextView
        var distanceFromUser: TextView
        var distanceFromUser_label: TextView


        init {

            magnitudeView = itemView.findViewById(R.id.magnitudeText)
            locationOffsetView = itemView.findViewById(R.id.locationOffsetText)
            primaryLocationView = itemView.findViewById(R.id.primaryLocationText)
            dateView = itemView.findViewById(R.id.dateText)
            timeView = itemView.findViewById(R.id.timeText)
            distanceFromUser = itemView.findViewById(R.id.distanceFromMe_tv)
            distanceFromUser_label = itemView.findViewById(R.id.distanceFromMeLabel_tv)

            // row click listener
            itemView.setOnClickListener(this)
        }


        override fun onClick(view: View) {
            // int elementId = earthquakesEntries.get(getAdapterPosition()).getId();
            eqItemClickListener.onItemClickListener(view, this.layoutPosition)
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Check for distance unit preference, and if a user location is different from the default one
     * ---------------------------------------------------------------------------------------------
     */
    private fun checkPreferences(): Boolean {
        // init shared preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

        // set distance unit choosen
        dist_unit = sharedPreferences.getString(context.getString(R.string.settings_distance_unit_by_key),
                context.getString(R.string.settings_distance_unit_by_default))

        Log.i(TAG, "EarthquakeAdapter : dist unit : " + dist_unit!!)

        val lat_s = sharedPreferences.getString(context.getString(R.string.device_lat),
                java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LAT))
        val lng_s = sharedPreferences.getString(context.getString(R.string.device_lng),
                java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LNG))

        // if there is user location different from default location
        return if (lat_s != java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LAT) && lng_s != java.lang.Double.toString(
                MainActivityEarthquakesList.DEFAULT_LNG
            )) {
            true // custom location
        } else {
            false // default location, Google inc. Mountain view
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Split string  location into  offsetLocation and primaryLocation
     * @param location
     * ---------------------------------------------------------------------------------------------
     */
    fun extractLocations(location: String) {
        // Check if location contains string "of".
        // In case yes, store the substring before "of" in offsetLocation, and the part after in primaryLocation
        // On the contrary, put location in primaryLocation
        if (location.contains("of")) {  // case of e.g. "85km SSW of xxxx"
            val splitResult = location.split("of".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            locationOffset = splitResult[0] + "of"
            // convert place distance to desidered distance unit
            locationOffset = convertPlaceDist(locationOffset!!)

            primaryLocation = splitResult[1]
        } else {
            locationOffset = context.resources.getString(R.string.locationOffset_label)
            primaryLocation = location
        }
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Convert place distance to desidered distance unit
     * @param loc
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    fun convertPlaceDist(loc: String): String {
        var loc = loc
        // convert distance in distance unit as preference
        val distance = MyUtil.returnDigit(loc)
        var dist_i = -1
        var dist_f = -1f

        // check the case the distance is as int or as float
        try {
            dist_i = Integer.parseInt(distance)
        } catch (e: NumberFormatException) {
            try {
                dist_f = java.lang.Float.parseFloat(distance)
            } catch (f: NumberFormatException) {
                dist_i = 0
            }

        }

        if (dist_i > 0) {
            dist_i = MyUtil.fromKmToMiles(dist_i.toDouble()).toInt()
        }

        if (dist_f > 0) {
            dist_i = MyUtil.fromKmToMiles(dist_f.toDouble()).toInt()
        }

        // get rid of the original distance unit
        loc = MyUtil.returnChar(loc)
                .replace("Km".toRegex(), "")
                .replace("km".toRegex(), "")
                .replace("KM".toRegex(), "")
                .replace("Mi".toRegex(), "")
                .replace("mi".toRegex(), "")
                .replace("MI".toRegex(), "")

        // add the preferred distance unit
        //distance = distWithUnit(dist_i);
        return "$distance $dist_unit$loc"
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Convert magnitude to format 0.0 and in string type
     * @param mag
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    fun formatMagnitude(mag: Double): String {
        val formatter = DecimalFormat("0.0")
        return formatter.format(mag)
    }
}
