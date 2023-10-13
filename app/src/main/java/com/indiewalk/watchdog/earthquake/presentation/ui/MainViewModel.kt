package com.indiewalk.watchdog.earthquake.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log

import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.AppEarthquake
import com.indiewalk.watchdog.earthquake.domain.model.Earthquake
import com.indiewalk.watchdog.earthquake.data.repository.EarthquakeRepository
import it.abenergie.customerarea.core.utility.extensions.TAG

class MainViewModel : ViewModel {
    var context: AppEarthquake? = null
    var eqList: LiveData<List<Earthquake>>? = null
        private set

    // Livedata var on Earthquake obj to populate through ViewModel
    // ** not used for the moment
    private val earthquakeSingleEntry: LiveData<Earthquake>? = null

    // repository ref
    private var eqRepository: EarthquakeRepository? = null

    // Preferences value
    private var minMagnitude: String? = null

    // SharePreferences ref
    private var sharedPreferences: SharedPreferences? = null

    // min mgnitudine value
    private var dMinMagnitude: Double = 0.0


    constructor() {
        context = AppEarthquake.getsContext() as AppEarthquake?

        // init repository
        eqRepository = (AppEarthquake.getsContext() as AppEarthquake).repository
        eqList = eqRepository!!.earthquakesList

        // init shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }


    constructor(listType: String) {
        Log.d(TAG, "Actively retrieving the collections from repository")

        context = AppEarthquake.getsContext() as AppEarthquake?

        // get repository instance
        // eqRepository = ((AppEarthquake) AppEarthquake.getsContext()).getRepository();
        eqRepository = (AppEarthquake.getsContext() as AppEarthquake).repositoryWithDataSource

        // init shared preferences and get value
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        checkPreferences()
        dMinMagnitude = java.lang.Double.parseDouble(minMagnitude)


        // choose the type of food list to load from db
        if (listType == MainActivityEarthquakesList.ORDER_BY_DESC_MAGNITUDE) {
            Log.d(TAG, "setupAdapter: ORDER_BY_DESC_MAGNITUDE : $listType")
            eqList = eqRepository!!.loadAll_orderby_desc_mag(dMinMagnitude)

        }
        if (listType == MainActivityEarthquakesList.ORDER_BY_ASC_MAGNITUDE) {
            Log.d(TAG, "setupAdapter: ORDER_BY_ASC_MAGNITUDE : $listType")
            eqList = eqRepository!!.loadAll_orderby_asc_mag(dMinMagnitude)

        } else if (listType == MainActivityEarthquakesList.ORDER_BY_MOST_RECENT) {
            Log.d(TAG, "setupAdapter: ORDER_BY_MOST_RECENT : $listType")
            eqList = eqRepository!!.loadAll_orderby_most_recent(dMinMagnitude)

        } else if (listType == MainActivityEarthquakesList.ORDER_BY_OLDEST) {
            Log.d(TAG, "setupAdapter: ORDER_BY_OLDEST : $listType")
            eqList = eqRepository!!.loadAll_orderby_oldest(dMinMagnitude)

        } else if (listType == MainActivityEarthquakesList.ORDER_BY_NEAREST) {
            Log.d(TAG, "setupAdapter: ORDER_BY_NEAREST : $listType")
            eqList = eqRepository!!.loadAll_orderby_nearest(dMinMagnitude)

        } else if (listType == MainActivityEarthquakesList.ORDER_BY_FURTHEST) {
            Log.d(TAG, "setupAdapter: ORDER_BY_FURTHEST : $listType")
            eqList = eqRepository!!.loadAll_orderby_furthest(dMinMagnitude)

        } else if (listType == MainActivityEarthquakesList.LOAD_ALL_NO_ORDER) {
            Log.d(TAG, "setupAdapter: LOAD_ALL_NO_ORDER : $listType")
            eqList = eqRepository!!.loadAll()
        }

    }


    // Set and check location coordinates from shared preferences.If not set, put default value
    private fun checkPreferences() {
        // recover min magnitude value from prefs or set a default from string value
        minMagnitude = sharedPreferences!!.getString(
            context!!.getString(R.string.settings_min_magnitude_key),
            context!!.getString(R.string.settings_min_magnitude_default)
        )

        // check preferences safety
        safePreferencesValue()

    }


    // making code more robust checking if for same reasons the default value stored are null or
    // not equals to none of the preferences stored values (e.g.  in case of key value change on code
    // but user saved with the previous one with previous app version )
    private fun safePreferencesValue() {

        val editor = sharedPreferences!!.edit()

        // minMagnitude safe
        if (minMagnitude!!.isEmpty() || minMagnitude == null) {
            setMinMagDefault(editor)
        }

        if (minMagnitude != context!!.getString(R.string.settings_1_0_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_2_0_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_3_0_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_4_0_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_4_5_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_5_0_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_5_5_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_6_0_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_6_5_min_magnitude_value)
        ) {
            setMinMagDefault(editor)
        }

    }

    // Set min_magnitude to default
    private fun setMinMagDefault(editor: SharedPreferences.Editor) {
        minMagnitude = context!!.getString(R.string.settings_min_magnitude_default)
        editor.putString(
            context!!.getString(R.string.settings_min_magnitude_key),
            context!!.getString(R.string.settings_min_magnitude_default)
        )
    }


}
