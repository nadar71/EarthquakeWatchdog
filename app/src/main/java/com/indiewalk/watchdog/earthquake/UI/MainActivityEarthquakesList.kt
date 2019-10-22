package com.indiewalk.watchdog.earthquake.UI

import android.app.AlertDialog
import android.app.LoaderManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.Loader
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast


import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.indiewalk.watchdog.earthquake.MapsActivity
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.data.Earthquake
import com.indiewalk.watchdog.earthquake.net.EarthquakeAsyncLoader
import com.indiewalk.watchdog.earthquake.util.ConsentSDK
import com.indiewalk.watchdog.earthquake.util.MyUtil

import java.util.ArrayList
import java.util.Arrays

import android.support.v7.widget.DividerItemDecoration.VERTICAL
import android.support.design.widget.FloatingActionButton


class MainActivityEarthquakesList : AppCompatActivity(),
        LoaderManager.LoaderCallbacks<List<Earthquake>>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        EarthquakeListAdapter.ItemClickListener {

    private var lastUpdate: String? = ""

    internal lateinit var earthquakeListView: RecyclerView
    private var earthquakes: List<Earthquake>? = null

    private var loadingInProgress: ProgressBar? = null

    private var emptyListText: TextView? = null
    private var order_value_tv: TextView? = null
    private var minMagn_value_tv: TextView? = null
    private var lastUp_value_tv: TextView? = null
    private var eq_period_value_tv: TextView? = null
    private var location_value_tv: TextView? = null
    private var summary_layout: View? = null
    private var filter_memo: View? = null
    private var fab: FloatingActionButton? = null

    private var adapter: EarthquakeListAdapter? = null

    // Preferences value
    internal var minMagnitude: String? = null
    internal var orderBy: String? = null
    internal var lat_s: String? = null
    internal var lng_s: String? = null
    internal var dateFilter: String? = null
    internal lateinit var dateFilterLabel: String
    internal var location_address: String? = null

    // SharePreferences ref
    internal lateinit var sharedPreferences: SharedPreferences

    // admob banner ref
    private var mAdView: AdView? = null
    private var consentSDK: ConsentSDK? = null


    /**
     * ---------------------------------------------------------------------------------------------
     * Get if consent gdpr must be asked or not
     * ---------------------------------------------------------------------------------------------
     */
    /**
     * ---------------------------------------------------------------------------------------------
     * Set if consent gdpr must be asked or not
     * ---------------------------------------------------------------------------------------------
     */
    var consentSDKNeed: Boolean
        get() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            return prefs.getBoolean(APP_CONSENT_NEED, DEFAULT_CONSENT_NEED)
        }
        set(isNeeded) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = prefs.edit()
            editor.putBoolean(APP_CONSENT_NEED, isNeeded)
            editor.apply()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_earthquakes_list)

        // summary fields
        summary_layout = findViewById(R.id.filter_memo)
        order_value_tv = findViewById(R.id.order_value_tv)
        minMagn_value_tv = findViewById(R.id.minMagn_value_tv)
        lastUp_value_tv = findViewById(R.id.lastUp_value_tv)
        eq_period_value_tv = findViewById(R.id.eq_period_value_tv)
        location_value_tv = findViewById(R.id.location_value_tv)

        loadingInProgress = findViewById(R.id.loading_spinner)
        emptyListText = findViewById(R.id.empty_view)
        filter_memo = findViewById(R.id.summary_layout)
        fab = findViewById(R.id.info_filter_fb)

        // set consent sdk for gdpr true by default
        // setConsentSDKNeed(true);

        setupActionBar()

        // summary layout start gone
        summary_layout!!.visibility = View.GONE

        // init shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // list init
        initRecycleView()

        // set preferences value in case of changes
        // checkPreferences();

        // TODO : temporary, must be set in repository init
        // get aware of date filter changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)

    }


    override fun onStart() {
        super.onStart()

        checkConsentActive = consentSDKNeed

        if (checkConsentActive) {
            // Initialize ConsentSDK
            consentSDK = ConsentSDK.Builder(this)
                    .addTestDeviceId("7DC1A1E8AEAD7908E42271D4B68FB270") // redminote 5 // Add your test device id "Remove addTestDeviceId on production!"
                    // .addTestDeviceId("9978A5F791A259430A0156313ED9C6A2")
                    .addCustomLogTag("gdpr_TAG") // Add custom tag default: ID_LOG
                    .addPrivacyPolicy("http://www.indie-walkabout.eu/privacy-policy-app") // Add your privacy policy url
                    .addPublisherId("pub-8846176967909254") // Add your admob publisher id
                    .build()


            // To check the consent and load ads
            consentSDK!!.checkConsent(object : ConsentSDK.ConsentCallback() {
                override fun onResult(isRequestLocationInEeaOrUnknown: Boolean) {
                    Log.i("gdpr_TAG", "onResult: isRequestLocationInEeaOrUnknown : $isRequestLocationInEeaOrUnknown")
                    // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
                    mAdView!!.loadAd(ConsentSDK.getAdRequest(this@MainActivityEarthquakesList))
                }
            })


            mAdView = findViewById(R.id.mAdView)

            // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
            mAdView!!.loadAd(ConsentSDK.getAdRequest(this@MainActivityEarthquakesList))

            mAdView!!.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    // Toast.makeText(MainActivityEarthquakesList.this, "Adloaded ok", Toast.LENGTH_SHORT).show();
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    // Code to be executed when an ad request fails.
                    // Toast.makeText(MainActivityEarthquakesList.this, "Adloaded FAILED TO LOAD "+errorCode, Toast.LENGTH_LONG).show();
                }

                override fun onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                override fun onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                override fun onAdClosed() {
                    // Code to be executed when when the user is about to return
                    // to the app after tapping on an ad.
                }
            }
        }


    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Check if device location is stored by previous accessing in-map section, and in case retrieve
     * coordinates
     * ---------------------------------------------------------------------------------------------
     */
    override fun onResume() {
        super.onResume()

        // update local vars for preferences changes
        checkPreferences()

        // Call loader for retrieving data
        retrieveData()
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Set with the purpose of unregistering preferences changes
     * ---------------------------------------------------------------------------------------------
     */
    override fun onDestroy() {
        super.onDestroy()
        // TODO : temporary, must be set in repository init
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this)
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Set icons, title etc. in action bar
     * ---------------------------------------------------------------------------------------------
     */
    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar!!.title = getString(R.string.title_reduced)
        actionBar.setIcon(R.mipmap.ic_launcher)
        actionBar.setDisplayShowHomeEnabled(true)
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Clicking on item shows action dialog
     * @param v
     * @param position
     * ---------------------------------------------------------------------------------------------
     */
    override fun onItemClickListener(v: View, position: Int) {
        Log.d(TAG, "onItemClickListener: Item at position : $position touched.")
        showActionDialog(position)
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Recycle view init
     * ---------------------------------------------------------------------------------------------
     */
    private fun initRecycleView() {
        // Find a reference to the {@link ListView} in the layout : using listView because
        // at the momento it has only tens of entry.
        earthquakeListView = findViewById(R.id.list)

        // Set LinearLayout
        earthquakeListView.layoutManager = LinearLayoutManager(this)

        // Create a new {@link EarthquakeAdapter} of {@link Earthquakes} objects
        adapter = EarthquakeListAdapter(this, this)

        // Set the adapter on the {@link ListView} so the list can be populated in the user interface
        earthquakeListView.adapter = adapter


        /*
        // Clicking on item shows an action dialog
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showActionDialog(position);
            }
        });
        */

        // Divider decorator
        val decoration = DividerItemDecoration(applicationContext, VERTICAL)
        earthquakeListView.addItemDecoration(decoration)


        // make fab button hide when scrolling list
        earthquakeListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && fab!!.isShown)
                    fab!!.hide()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fab!!.show()
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        fab!!.setOnClickListener {
            if (summary_layout!!.isShown)
                summary_layout!!.visibility = View.GONE
            else
                summary_layout!!.visibility = View.VISIBLE
        }


    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Registering preferences change only for data filter changes
     *
     * @param sharedPreferences
     * @param key
     * ---------------------------------------------------------------------------------------------
     */
    // TODO : temporary, must be set in repository init
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        Log.d("Dict", "PreferenceChanged: $key")
        if (key == getString(R.string.settings_date_filter_key)) {
            NEED_REMOTE_UPDATE = true
        }
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Show Select action alert dialog
     *
     * @param position ---------------------------------------------------------------------------------------------
     */
    private fun showActionDialog(position: Int) {
        val context = this@MainActivityEarthquakesList
        val items = arrayOf<CharSequence>("Show on Map", "Details", "Feel it?")

        AlertDialog.Builder(this@MainActivityEarthquakesList)
                .setTitle("Action")
                .setItems(items) { dialog, item ->
                    if (item == 0) {       // Show on Map
                        val nDays = Integer.parseInt(dateFilter)
                        if (nDays <= daysLimit) {
                            val earthquake = earthquakes!![position]
                            val showEqOnMap = Intent(context, MapsActivity::class.java)
                            showEqOnMap.putExtra("ShowEquake", "true")
                            showEqOnMap.putExtra("equake_lat",
                                    java.lang.Double.toString(earthquake.latitude))
                            showEqOnMap.putExtra("equake_lng",
                                    java.lang.Double.toString(earthquake.longitude))
                            startActivity(showEqOnMap)
                        } else {
                            Toast.makeText(this@MainActivityEarthquakesList,
                                    getString(R.string.use_of_map_forbidden), Toast.LENGTH_LONG).show()
                        }

                    } else if (item == 1) {  // USGS site Details
                        val earthquake = earthquakes!![position]
                        val url = earthquake.url
                        Log.i("setOnItemClickListener", "onItemClick: " + url!!)

                        // Open the related url page of the eq clicked
                        val webpage = Uri.parse(url)
                        val webIntent = Intent(Intent.ACTION_VIEW, webpage)
                        startActivity(Intent.createChooser(webIntent, "Open details"))

                    } else if (item == 2) {  // Feel it?
                        val earthquake = earthquakes!![position]
                        val url = earthquake.url!! + "/tellus"
                        Log.i("setOnItemClickListener", "onItemClick: $url")

                        // Open the related url page of the eq clicked
                        val webpage = Uri.parse(url)
                        val webIntent = Intent(Intent.ACTION_VIEW, webpage)
                        startActivity(Intent.createChooser(webIntent, "Feel it? Tell us"))
                    }
                }
                .show()

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Set and check location coordinates from shared preferences.
     * If not set, put default value
     * ---------------------------------------------------------------------------------------------
     */
    private fun checkPreferences() {


        // get coords from preferences
        lat_s = sharedPreferences.getString(getString(R.string.device_lat), java.lang.Double.toString(DEFAULT_LAT))
        lng_s = sharedPreferences.getString(getString(R.string.device_lng), java.lang.Double.toString(DEFAULT_LNG))


        // set default coord if there are no one
        val editor = sharedPreferences.edit()
        if (lat_s!!.isEmpty() == true) {
            editor.putString(getString(R.string.device_lat), java.lang.Double.toString(DEFAULT_LAT))
            editor.apply()
        }
        if (lng_s!!.isEmpty() == true) {
            editor.putString(getString(R.string.device_lng), java.lang.Double.toString(DEFAULT_LNG))
            editor.apply()
        }

        // Toast.makeText(this, "Current Location : lat : " + lat_s + " long : " + lng_s, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onResume: Current Location : lat : $lat_s long : $lng_s")


        // recover last update from preferences
        lastUpdate = sharedPreferences.getString(
                getString(R.string.last_update),
                DEFAULT_LAST_UPDATE)

        // recover min magnitude value from prefs or set a default from string value
        minMagnitude = sharedPreferences.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default))


        // recover preferred order by param from prefs or set a default from string value
        orderBy = sharedPreferences.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default))


        // recover preferred date filter by param from prefs or set a default from string value
        dateFilter = sharedPreferences.getString(
                getString(R.string.settings_date_filter_key),
                getString(R.string.settings_date_filter_default))

        if (dateFilter == getString(R.string.settings_date_period_today_value))
            dateFilterLabel = getString(R.string.settings_date_period_today_label)
        else if (dateFilter == getString(R.string.settings_date_period_24h_value))
            dateFilterLabel = getString(R.string.settings_date_period_24h_label)
        else if (dateFilter == getString(R.string.settings_date_period_48h_value))
            dateFilterLabel = getString(R.string.settings_date_period_48h_label)
        else if (dateFilter == getString(R.string.settings_date_period_week_value))
            dateFilterLabel = getString(R.string.settings_date_period_week_label)
        else if (dateFilter == getString(R.string.settings_date_period_2_week_value))
            dateFilterLabel = getString(R.string.settings_date_period_2_week_label)/*
        else if ((dateFilter.equals(getString(R.string.settings_date_period_3_days_value))))
            dateFilterLabel = getString(R.string.settings_date_period_3_days_label);

        else if ((dateFilter.equals(getString(R.string.settings_date_period_4_days_value))))
            dateFilterLabel = getString(R.string.settings_date_period_4_days_label);
        */
        /* #68
        else if ((dateFilter.equals(getString(R.string.settings_date_period_month_value))))
            dateFilterLabel = getString(R.string.settings_date_period_month_label);
        */


        // recover min magnitude value from prefs or set a default from string value
        location_address = sharedPreferences.getString(
                getString(R.string.location_address),
                DEFAULT_ADDRESS)

        /* left in case of debug
        // recover preferred equakes num to display
        numEquakes = sharedPreferences.getString(
                getString(R.string.settings_max_equakes_key),
                getString(R.string.settings_max_equakes_default));
        */

        // check preferences safety
        safePreferencesValue(editor)

        // summary above list
        setFilterSummary()
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * making code more robust checking if for same reasons the defaut value stored are null or
     * not equals to none of the preferences stored values (e.g.  in case of key value change on code
     * but user saved with the previous one with previous app version )
     *
     * @param editor ---------------------------------------------------------------------------------------------
     */
    private fun safePreferencesValue(editor: SharedPreferences.Editor) {

        // lastUpdate
        /* Left for debug; don't need this, use repository check in datasource
        if (lastUpdate.isEmpty() || lastUpdate == null) {
            // it's the first start, need to load data
            NEED_REMOTE_UPDATE = true;
            dateFilter = getString(R.string.settings_date_filter_default);
            editor.putString(getString(R.string.settings_date_filter_key), getString(R.string.settings_date_filter_default));
        }
        */

        // minMagnitude safe
        if (minMagnitude!!.isEmpty() || minMagnitude == null) {
            minMagnitude = getString(R.string.settings_min_magnitude_default)
            editor.putString(getString(R.string.settings_min_magnitude_key), getString(R.string.settings_min_magnitude_default))
        }

        if (minMagnitude != getString(R.string.settings_1_0_min_magnitude_value) &&
                minMagnitude != getString(R.string.settings_2_0_min_magnitude_value) &&
                minMagnitude != getString(R.string.settings_3_0_min_magnitude_value) &&
                minMagnitude != getString(R.string.settings_4_0_min_magnitude_value) &&
                minMagnitude != getString(R.string.settings_4_5_min_magnitude_label) &&
                minMagnitude != getString(R.string.settings_5_0_min_magnitude_label) &&
                minMagnitude != getString(R.string.settings_5_5_min_magnitude_value) &&
                minMagnitude != getString(R.string.settings_6_0_min_magnitude_value) &&
                minMagnitude != getString(R.string.settings_6_5_min_magnitude_value)) {
            minMagnitude = getString(R.string.settings_min_magnitude_default)
            editor.putString(getString(R.string.settings_min_magnitude_key), getString(R.string.settings_min_magnitude_default))
        }

        // orderBy safe
        if (orderBy!!.isEmpty() || orderBy == null) {
            orderBy = getString(R.string.settings_order_by_default)
            editor.putString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default))
        }

        if (orderBy != getString(R.string.settings_order_by_desc_magnitude_value) &&
                orderBy != getString(R.string.settings_order_by_asc_magnitude_value) &&
                orderBy != getString(R.string.settings_order_by_most_recent_value) &&
                orderBy != getString(R.string.settings_order_by_oldest_value) &&
                orderBy != getString(R.string.settings_order_by_nearest_value) &&
                orderBy != getString(R.string.settings_order_by_furthest_value)) {
            orderBy = getString(R.string.settings_order_by_default)
            editor.putString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default))
        }


        /* left in case of debug
        // numEquakes safe
        if (numEquakes.isEmpty() || numEquakes == null) {
            numEquakes = getString(R.string.settings_max_equakes_default);
            editor.putString(getString(R.string.settings_max_equakes_key), getString(R.string.settings_max_equakes_default));
        }

        if ((!numEquakes.equals(getString(R.string.settings_max_30_equakes_value))) &&
            (!numEquakes.equals(getString(R.string.settings_max_60_equakes_value))) &&
            (!numEquakes.equals(getString(R.string.settings_max_90_equakes_value))) &&
            (!numEquakes.equals(getString(R.string.settings_max_120_equakes_value)))
        ){
            orderBy = getString(R.string.settings_max_equakes_default);
            editor.putString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));
        }
        */


        // date filter safe
        if (dateFilter!!.isEmpty() || dateFilter == null) {
            dateFilter = getString(R.string.settings_date_filter_default)
            editor.putString(getString(R.string.settings_date_filter_key), getString(R.string.settings_date_filter_default))
        }

        if (dateFilter != getString(R.string.settings_date_period_today_value) &&
                dateFilter != getString(R.string.settings_date_period_24h_value) &&
                dateFilter != getString(R.string.settings_date_period_48h_value) &&
                /*(!dateFilter.equals(getString(R.string.settings_date_period_3_days_value))) &&
                (!dateFilter.equals(getString(R.string.settings_date_period_4_days_value))) && */
                dateFilter != getString(R.string.settings_date_period_week_value) &&
                dateFilter != getString(R.string.settings_date_period_2_week_value)) {
            dateFilter = getString(R.string.settings_date_filter_default)
            editor.putString(getString(R.string.settings_date_filter_key), getString(R.string.settings_date_filter_default))
        }// #68 && (!dateFilter.equals(getString(R.string.settings_date_period_month_value)))


        if (location_address!!.isEmpty() || location_address == null) {
            location_address = DEFAULT_ADDRESS
            editor.putString(getString(R.string.location_address), location_address)
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Summarize the filter settings for the eq list shown
     * ---------------------------------------------------------------------------------------------
     */
    private fun setFilterSummary() {
        // set up filter summary
        // order by
        if (orderBy == getString(R.string.settings_order_by_desc_magnitude_value))
            order_value_tv!!.text = getString(R.string.settings_order_by_desc_magnitude_label)

        if (orderBy == getString(R.string.settings_order_by_asc_magnitude_value))
            order_value_tv!!.text = getString(R.string.settings_order_by_asc_magnitude_label)

        if (orderBy == getString(R.string.settings_order_by_most_recent_value))
            order_value_tv!!.text = getString(R.string.settings_order_by_most_recent_label)

        if (orderBy == getString(R.string.settings_order_by_oldest_value))
            order_value_tv!!.text = getString(R.string.settings_order_by_oldest_label)

        if (orderBy == getString(R.string.settings_order_by_nearest_value))
            order_value_tv!!.text = getString(R.string.settings_order_by_nearest_label)

        if (orderBy == getString(R.string.settings_order_by_furthest_value))
            order_value_tv!!.text = getString(R.string.settings_order_by_furthest_label)

        // min magnitude
        minMagn_value_tv!!.text = minMagnitude

        // last update time
        lastUp_value_tv!!.text = lastUpdate

        // time range
        if (dateFilter == getString(R.string.settings_date_period_today_value))
            eq_period_value_tv!!.text = getString(R.string.settings_date_period_today_label)
        else if (dateFilter == getString(R.string.settings_date_period_24h_value))
            eq_period_value_tv!!.text = getString(R.string.settings_date_period_24h_label)
        else if (dateFilter == getString(R.string.settings_date_period_48h_value))
            eq_period_value_tv!!.text = getString(R.string.settings_date_period_48h_label)
        else if (dateFilter == getString(R.string.settings_date_period_week_value))
            eq_period_value_tv!!.text = getString(R.string.settings_date_period_week_label)
        else if (dateFilter == getString(R.string.settings_date_period_2_week_value))
            eq_period_value_tv!!.text = getString(R.string.settings_date_period_2_week_label)/*
        else if ((dateFilter.equals(getString(R.string.settings_date_period_3_days_value))))
            dateFilterLabel = getString(R.string.settings_date_period_3_days_label);

        else if ((dateFilter.equals(getString(R.string.settings_date_period_4_days_value))))
            dateFilterLabel = getString(R.string.settings_date_period_4_days_label);
        */
        /*
        else if ((dateFilter.equals(getString(R.string.settings_date_period_month_value))))
            eq_period_value_tv.setText(getString(R.string.settings_date_period_month_label));
        */

        //location address
        location_value_tv!!.text = location_address
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Get earthquake list, from db or if necessary from restful service
     * ---------------------------------------------------------------------------------------------
     */
    private fun retrieveData() {
        Log.i(TAG, "retrieveRemoteData: Requesting fresh data.")


        // show empty list and load in progress
        showLoading()

        // TODO : temporary, data must be updated setting an observer in repository on specific preference
        // check if date filter or other preferences which need remote update has been changed
        if (NEED_REMOTE_UPDATE) {
            retrieveRemoteData()
            NEED_REMOTE_UPDATE = false
        } else {
            updateList()
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Update the adapter/equakes list, based on user's preferences, using viewmodel/livedata
     * ---------------------------------------------------------------------------------------------
     */
    private fun updateList() {
        Log.i(TAG, "updateList Executing ")
        // clear the adapter of previous data
        // adapter.resetEarthquakesEntries();

        checkPreferences()


        // set equakes list showing based on user preferences
        if (orderBy == getString(R.string.settings_order_by_desc_magnitude_value)) {
            val factory = MainViewModelFactory(ORDER_BY_DESC_MAGNITUDE)
            filterDatafromRepository(factory)

        } else if (orderBy == getString(R.string.settings_order_by_asc_magnitude_value)) {
            val factory = MainViewModelFactory(ORDER_BY_ASC_MAGNITUDE)
            filterDatafromRepository(factory)

        } else if (orderBy == getString(R.string.settings_order_by_most_recent_value)) {
            val factory = MainViewModelFactory(ORDER_BY_MOST_RECENT)
            filterDatafromRepository(factory)

        } else if (orderBy == getString(R.string.settings_order_by_oldest_value)) {
            val factory = MainViewModelFactory(ORDER_BY_OLDEST)
            filterDatafromRepository(factory)

        } else if (orderBy == getString(R.string.settings_order_by_nearest_value)) {
            val factory = MainViewModelFactory(ORDER_BY_NEAREST)
            filterDatafromRepository(factory)

        } else if (orderBy == getString(R.string.settings_order_by_furthest_value)) {
            val factory = MainViewModelFactory(ORDER_BY_FURTHEST)
            filterDatafromRepository(factory)
        }


    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Make repository request for specific data and ordering
     *
     * @param factory
     * ---------------------------------------------------------------------------------------------
     */
    private fun filterDatafromRepository(factory: MainViewModelFactory) {
        // Get eq list through LiveData
        // NB : Here it check if there are eqs at first start :
        //
        // 1 - The MainViewModel get repository instance, then
        //     try to recover eqs list using it
        // 2 - In each repository dao method, there is a call to repo initializeData :
        //     Here it checks if there are data or need to be remote requested
        //
        // Moreover, getting repo instance with getRepositoryWithDataSource it activates
        // an observerforever on networkData = networkDataSource.getEarthquakesData()
        // in case of data change due to scheduled update

        val viewModel = ViewModelProviders
                .of(this, factory)
                .get(MainViewModel::class.java)


        val equakes = viewModel.eqList
        equakes?.observe(this, Observer { earthquakeEntries ->
            if (earthquakeEntries != null && !earthquakeEntries.isEmpty()) { // data ready in db
                earthquakes = earthquakeEntries
                updateAdapter(earthquakeEntries)
                // used to update the last update field, updated by datasource at 1st start
                checkPreferences()
                showEarthquakeListView()
            } else {                                                         // waiting for data
                // While waiting that the repository getting aware that the eqs list is empty
                // and ask for a remote update
                if (MyUtil.isConnectionOk) {
                    showLoading()
                } else {
                    showNoInternetConnection()
                }
            }
        })
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Notify and update adapter data
     *
     * @param earthquakeEntries
     * ---------------------------------------------------------------------------------------------
     */
    private fun updateAdapter(earthquakeEntries: List<Earthquake>?) {
        adapter!!.earthquakesEntries = earthquakeEntries as MutableList<Earthquake>?
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Retrieve Remote Data.
     * Check internet connection availability first
     * ---------------------------------------------------------------------------------------------
     */
    private fun retrieveRemoteData() {

        if (MyUtil.isConnectionOk) {
            showLoading()
            // LoaderManager reference
            val loaderManager = loaderManager
            // Init loader : id above, bundle = null , this= current activity for LoaderCallbacks
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, Bundle.EMPTY, this)

        } else {
            showNoInternetConnection()
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Show No Internet Connection view
     * ---------------------------------------------------------------------------------------------
     */
    private fun showNoInternetConnection() {
        // hide progress bar
        loadingInProgress!!.visibility = View.GONE
        filter_memo!!.visibility = View.INVISIBLE
        earthquakeListView.visibility = View.GONE
        emptyListText!!.visibility = View.VISIBLE
        emptyListText!!.setText(R.string.no_internet_connection)
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Show loading in progress view, hiding earthquake list
     * ---------------------------------------------------------------------------------------------
     */
    private fun showLoading() {
        loadingInProgress!!.visibility = View.VISIBLE
        filter_memo!!.visibility = View.INVISIBLE
        earthquakeListView.visibility = View.GONE
        emptyListText!!.visibility = View.VISIBLE
        emptyListText!!.setText(R.string.searching)
    }

    /**
     * Show hearthquake list after loading/retrieving data completed
     */
    private fun showEarthquakeListView() {
        loadingInProgress!!.visibility = View.INVISIBLE
        filter_memo!!.visibility = View.VISIBLE
        earthquakeListView.visibility = View.VISIBLE
        emptyListText!!.visibility = View.INVISIBLE
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Create loader
     *
     * @param id
     * @param args
     * @return
     *
     * ---------------------------------------------------------------------------------------------
     */
    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<Earthquake>> {
        Log.i(TAG, "onCreateLoader: Create a new Loader")
        val urlReq = MyUtil.composeQueryUrl(dateFilter!!)
        Log.i(TAG, "onCreateLoader: urlReq : $urlReq")
        // create a new loader for the url
        return EarthquakeAsyncLoader(this, urlReq)
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Loader finished
     *
     * @param loader
     * @param earthquakesReturnedByLoader
     *
     * ---------------------------------------------------------------------------------------------
     */
    // it has been already stored in db; must only return
    override fun onLoadFinished(loader: Loader<List<Earthquake>>, earthquakesReturnedByLoader: List<Earthquake>) {
        Log.i(TAG, "onLoadFinished: Loader return back with data")

        // hide progress bar
        // loadingInProgress.setVisibility(View.GONE);

        // Set empty state text to display "No earthquakes found."
        emptyListText!!.setText(R.string.no_earthquakes)

        // --> update UI when loader finished
        if (setEartquakesList(earthquakesReturnedByLoader)) {
            updateList()

            // update preferences
            lastUpdate = MyUtil.setLastUpdateField(this)

            lastUp_value_tv!!.text = lastUpdate

            // show filter summary
            filter_memo!!.visibility = View.VISIBLE

            val alert = Toast.makeText(this@MainActivityEarthquakesList,
                    getString(R.string.data_update_toast) + dateFilterLabel, Toast.LENGTH_LONG)
            alert.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
            alert.show()

        } else {
            Log.i(TAG, "Problem with earthquake list, is empty. Check the request. ")
        }


    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Loader reset
     *
     * @param loader
     *
     * ---------------------------------------------------------------------------------------------
     */
    override fun onLoaderReset(loader: Loader<List<Earthquake>>?) {
        Log.i(TAG, "onLoaderReset: Reset Loader previous data")
        // reset loader to clean up previous data
        adapter!!.resetEarthquakesEntries()
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Used by onLoadFinished to populate the ArrayList fetched
     *
     * @param earthquakes
     * @return true/false
     * ---------------------------------------------------------------------------------------------
     */
    protected fun setEartquakesList(earthquakes: List<Earthquake>?): Boolean {
        if (earthquakes != null && earthquakes.isEmpty() == false) {
            // this.earthquakes = earthquakes;
            // updateList();
            return true
        } else {
            Log.i(TAG, "The earthquake list is empty. Check the request. ")
            Toast.makeText(this, "The earthquake list is empty. Check the request. ", Toast.LENGTH_LONG).show()
            return false
        }

    }


    // ---------------------------------------------------------------------------------------------
    //                                          MENU STUFF
    // ---------------------------------------------------------------------------------------------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_settings -> return true
            R.id.quick_settings -> {
                showDialog()
                return true
            }
            R.id.general_settings -> {
                val settingsIntent = Intent(this, SettingSimpleActivity::class.java)
                startActivity(settingsIntent)
                return true
            }
            R.id.set_myposition_action -> {
                val nDays = Integer.parseInt(dateFilter)
                if (nDays <= daysLimit) {
                    val setMapIntent = Intent(this, MapsActivity::class.java)
                    startActivity(setMapIntent)
                } else {
                    Toast.makeText(this, getString(R.string.use_of_map_forbidden), Toast.LENGTH_LONG).show()
                }
                return true
            }
            R.id.refresh_action -> {
                showLoading()
                retrieveRemoteData()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Dialog with spinner for quick settings
     * ---------------------------------------------------------------------------------------------
     */
    private fun showDialog() {
        val builder = android.support.v7.app.AlertDialog.Builder(this@MainActivityEarthquakesList)
        val view = layoutInflater.inflate(R.layout.quick_settings_dialog, null)
        /*
        CheckBox saveFlag = (CheckBox)findViewById(R.id.dialog_checkBox);
        //TODO : update with choice to save or not list when passing to acc
        saveFlag.setVisibility(View.INVISIBLE);
        */
        builder.setTitle(resources.getString(R.string.quick_settings_title))

        // 1 -  spinner order by choice : spinner_order_by
        val spinner_order_by = view.findViewById<View>(R.id.order_by_spinner) as Spinner
        // list of labels for order by spinner list
        val order_list = ArrayList<String>() // add header
        order_list.add(resources.getString(R.string.spinner_defaultchoice_label))
        order_list.addAll(Arrays.asList(*resources.getStringArray(R.array.settings_order_by_labels)))

        // list of  values corresponding positionally in list to the labels
        val order_list_values = ArrayList<String>() // add header
        order_list_values.add(resources.getString(R.string.spinner_defaultchoice_value))
        order_list_values.addAll(Arrays.asList(*resources.getStringArray(R.array.settings_order_by_values)))

        // put labels in spinner
        val adapter_01 = ArrayAdapter(this@MainActivityEarthquakesList,
                android.R.layout.simple_spinner_item,
                order_list)
        adapter_01.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner_order_by.adapter = adapter_01


        // 2 - spinner choose min magnitude : spinner_min_magnitude
        val spinner_min_magnitude = view.findViewById<View>(R.id.min_magnitude_spinner) as Spinner
        // list of labels for magnitude min  spinner list
        val magn_list = ArrayList<String>() // add header
        magn_list.add(resources.getString(R.string.spinner_defaultchoice_label))
        magn_list.addAll(Arrays.asList(*resources.getStringArray(R.array.settings_array_min_magnitude_labels)))

        // list of  values corresponding positionally in list to the labels
        val magn_list_values = ArrayList<String>() // add header
        magn_list_values.add(resources.getString(R.string.spinner_defaultchoice_value))
        magn_list_values.addAll(Arrays.asList(*resources.getStringArray(R.array.settings_array_min_magnitude_values)))

        // put labels in spinner
        val adapter_02 = ArrayAdapter(this@MainActivityEarthquakesList,
                android.R.layout.simple_spinner_item,
                magn_list)
        adapter_02.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner_min_magnitude.adapter = adapter_02


        builder.setPositiveButton("Ok") { dialog, which ->
            // boolean restartActivity = false;   // need to restart the activity
            // boolean updateList      = false;   // need to sort  the list without restarting activity
            val editor = sharedPreferences.edit()

            // check choices
            // convert labels to values

            val spinner_order_by_choice = convertFromLabelsToKeyvalues(
                    spinner_order_by.selectedItem.toString(),
                    order_list, order_list_values)


            val spinner_min_magn_choice = convertFromLabelsToKeyvalues(
                    spinner_min_magnitude.selectedItem.toString(),
                    magn_list, magn_list_values)

            // set value selected for filter in preference keys
            if (spinner_order_by_choice != resources.getString(R.string.spinner_defaultchoice_value)) {
                editor.putString(getString(R.string.settings_order_by_key),
                        spinner_order_by_choice)
                editor.apply()
                // updateList = true;
            }

            if (!spinner_min_magn_choice.equals(resources.getString(R.string.spinner_defaultchoice_value), ignoreCase = true)) {
                editor.putString(getString(R.string.settings_min_magnitude_key),
                        spinner_min_magn_choice)
                editor.apply()
                // restartActivity = true;
            }

            Log.i(TAG, "onClick: ")
            // process choices

            /*
                if (restartActivity == false){
                    if (updateList == true) {
                        dialog.dismiss();
                        updateList();
                    }
                } else if (restartActivity == true){
                    // restart activity
                    dialog.dismiss();
                    MyUtil.restartActivity(MainActivityEarthquakesList.this);
                }
                */

            dialog.dismiss()
            MyUtil.restartActivity(this@MainActivityEarthquakesList)
        }


        builder.setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }

        builder.setView(view)
        val dialog = builder.create()
        dialog.show()

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Get spinner values corrisponding to a specific label in spinner list
     *
     * @param selectedItem
     * @param labelsList
     * @param valuesList
     * @return ---------------------------------------------------------------------------------------------
     */
    private fun convertFromLabelsToKeyvalues(selectedItem: String,
                                             labelsList: List<String>,
                                             valuesList: List<String>): String {
        val position = labelsList.indexOf(selectedItem)
        return valuesList[position]
    }

    companion object {

        val TAG = MainActivityEarthquakesList::class.java.name

        // this is the default position, google at mountain view
        val DEFAULT_LAT = 37.4219999
        val DEFAULT_LNG = -122.0862515
        val DEFAULT_ADDRESS = "Mountain View,CA"
        val DEFAULT_LAST_UPDATE = ""

        // Key constant for view model parameters
        val LOAD_ALL_NO_ORDER = "load_all_no_order"
        val ORDER_BY_DESC_MAGNITUDE = "magnitude_desc_ordering"
        val ORDER_BY_ASC_MAGNITUDE = "magnitude_asc_ordering"
        val ORDER_BY_MOST_RECENT = "most_recent"
        val ORDER_BY_OLDEST = "oldest"
        val ORDER_BY_NEAREST = "nearest"
        val ORDER_BY_FURTHEST = "furthest"

        private val APP_OPENING_COUNTER = "app-opening-counter"
        private val APP_CONSENT_NEED = "consent_requested"
        private val DEFAULT_CONSENT_NEED = true

        // day limit for accessing map
        val daysLimit = 1

        // TODO : Temporary, must use only onPreferenceChanges
        // flag for reloading data from remote due to different time range requested for data in preferences
        var NEED_REMOTE_UPDATE = false


        // Id for loader which retrieve data from remote source (not necessary there is only it!)
        private val EARTHQUAKE_LOADER_ID = 1

        // Cnsent SDK vars
        private var checkConsentActive = true
    }


}