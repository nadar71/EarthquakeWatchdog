package com.indiewalk.watchdog.earthquake.UI;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.indiewalk.watchdog.earthquake.MapsActivity;
import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.data.Earthquake;
import com.indiewalk.watchdog.earthquake.net.EarthquakeAsyncLoader;
import com.indiewalk.watchdog.earthquake.util.ConsentSDK;
import com.indiewalk.watchdog.earthquake.util.MyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import android.support.design.widget.FloatingActionButton;


public class MainActivityEarthquakesList extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Earthquake>>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        EarthquakeListAdapter.ItemClickListener{

    public static final String TAG = MainActivityEarthquakesList.class.getName();

    // this is the default position, google at mountain view
    public static final double DEFAULT_LAT = 37.4219999;
    public static final double DEFAULT_LNG = -122.0862515;
    public static final String DEFAULT_ADDRESS = "Mountain View,CA";
    public static final String DEFAULT_LAST_UPDATE = "";

    // Key constant for view model parameters
    public static final String LOAD_ALL_NO_ORDER       = "load_all_no_order";
    public static final String ORDER_BY_DESC_MAGNITUDE = "magnitude_desc_ordering";
    public static final String ORDER_BY_ASC_MAGNITUDE  = "magnitude_asc_ordering";
    public static final String ORDER_BY_MOST_RECENT    = "most_recent";
    public static final String ORDER_BY_OLDEST         = "oldest";
    public static final String ORDER_BY_NEAREST        = "nearest";
    public static final String ORDER_BY_FURTHEST       = "furthest";

    private static final String APP_OPENING_COUNTER     = "app-opening-counter";
    private static final String APP_CONSENT_NEED        = "consent_requested";
    private static final boolean DEFAULT_CONSENT_NEED   = true;

    // day limit for accessing map
    public static final int daysLimit = 1;

    private String lastUpdate = "";

    // TODO : Temporary, must use only onPreferenceChanges
    // flag for reloading data from remote due to different time range requested for data in preferences
    public static boolean NEED_REMOTE_UPDATE = false;

    RecyclerView earthquakeListView;
    private List<Earthquake> earthquakes;

    private ProgressBar loadingInProgress;

    private TextView emptyListText;
    private TextView order_value_tv, minMagn_value_tv, lastUp_value_tv, eq_period_value_tv, location_value_tv;
    private View summary_layout;
    private View filter_memo;
    private FloatingActionButton fab ;

    private EarthquakeListAdapter adapter;


    // Id for loader which retrieve data from remote source (not necessary there is only it!)
    private static final int EARTHQUAKE_LOADER_ID = 1;

    // Preferences value
    String minMagnitude, orderBy, lat_s, lng_s, dateFilter, dateFilterLabel, location_address;

    // SharePreferences ref
    SharedPreferences sharedPreferences;

    // admob banner ref
    private AdView mAdView;

    // Cnsent SDK vars
    private static boolean checkConsentActive = true;
    private ConsentSDK consentSDK;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_earthquakes_list);

        // summary fields
        summary_layout      = findViewById(R.id.filter_memo);
        order_value_tv      = findViewById(R.id.order_value_tv);
        minMagn_value_tv    = findViewById(R.id.minMagn_value_tv);
        lastUp_value_tv     = findViewById(R.id.lastUp_value_tv);
        eq_period_value_tv  = findViewById(R.id.eq_period_value_tv);
        location_value_tv   = findViewById(R.id.location_value_tv);

        loadingInProgress   = findViewById(R.id.loading_spinner);
        emptyListText       = findViewById(R.id.empty_view);
        filter_memo         = findViewById(R.id.summary_layout);
        fab                 = findViewById(R.id.info_filter_fb);

        // set consent sdk for gdpr true by default
        // setConsentSDKNeed(true);

        setupActionBar();

        // summary layout start gone
        summary_layout.setVisibility(View.GONE);

        // init shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // list init
        initRecycleView();

        // set preferences value in case of changes
        // checkPreferences();

        // TODO : temporary, must be set in repository init
        // get aware of date filter changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

    }


    @Override
    protected void onStart() {
        super.onStart();

        checkConsentActive = getConsentSDKNeed();

        if (checkConsentActive) {
            // Initialize ConsentSDK
            consentSDK = new ConsentSDK.Builder(this)
                    .addTestDeviceId("7DC1A1E8AEAD7908E42271D4B68FB270") // redminote 5 // Add your test device id "Remove addTestDeviceId on production!"
                    // .addTestDeviceId("9978A5F791A259430A0156313ED9C6A2")
                    .addCustomLogTag("gdpr_TAG") // Add custom tag default: ID_LOG
                    .addPrivacyPolicy("http://www.indie-walkabout.eu/privacy-policy-app") // Add your privacy policy url
                    .addPublisherId("pub-8846176967909254") // Add your admob publisher id
                    .build();


            // To check the consent and load ads
            consentSDK.checkConsent(new ConsentSDK.ConsentCallback() {
                @Override
                public void onResult(boolean isRequestLocationInEeaOrUnknown) {
                    Log.i("gdpr_TAG", "onResult: isRequestLocationInEeaOrUnknown : " + isRequestLocationInEeaOrUnknown);
                    // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
                    mAdView.loadAd(ConsentSDK.Companion.getAdRequest(MainActivityEarthquakesList.this));
                }
            });


            mAdView = findViewById(R.id.adView);

            // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
            mAdView.loadAd(ConsentSDK.Companion.getAdRequest(MainActivityEarthquakesList.this));

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    // Toast.makeText(MainActivityEarthquakesList.this, "Adloaded ok", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                    // Toast.makeText(MainActivityEarthquakesList.this, "Adloaded FAILED TO LOAD "+errorCode, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when when the user is about to return
                    // to the app after tapping on an ad.
                }
            });
        }



    }






    /**
     * ---------------------------------------------------------------------------------------------
     * Check if device location is stored by previous accessing in-map section, and in case retrieve
     * coordinates
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    protected void onResume() {
        super.onResume();

        // update local vars for preferences changes
        checkPreferences();

        // Call loader for retrieving data
        retrieveData();
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Set with the purpose of unregistering preferences changes
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO : temporary, must be set in repository init
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Set icons, title etc. in action bar
     * ---------------------------------------------------------------------------------------------
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.title_reduced));
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setDisplayShowHomeEnabled(true);
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Get if consent gdpr must be asked or not
     * ---------------------------------------------------------------------------------------------
     */
    public boolean getConsentSDKNeed() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(APP_CONSENT_NEED, DEFAULT_CONSENT_NEED);
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Set if consent gdpr must be asked or not
     * ---------------------------------------------------------------------------------------------
     */
    public void setConsentSDKNeed( boolean isNeeded) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(APP_CONSENT_NEED, isNeeded);
        editor.apply();
    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Clicking on item shows action dialog
     * @param v
     * @param position
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    public void onItemClickListener(View v, int position) {
        Log.d(TAG, "onItemClickListener: Item at position : " + position + " touched.");
        showActionDialog(position);
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Recycle view init
     * ---------------------------------------------------------------------------------------------
     */
    private void initRecycleView(){
        // Find a reference to the {@link ListView} in the layout : using listView because
        // at the momento it has only tens of entry.
        earthquakeListView = findViewById(R.id.list);

        // Set LinearLayout
        earthquakeListView.setLayoutManager(new LinearLayoutManager(this));

        // Create a new {@link EarthquakeAdapter} of {@link Earthquakes} objects
        adapter = new EarthquakeListAdapter(this, this);

        // Set the adapter on the {@link ListView} so the list can be populated in the user interface
        earthquakeListView.setAdapter(adapter);


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
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        earthquakeListView.addItemDecoration(decoration);


        // make fab button hide when scrolling list
        earthquakeListView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fab.isShown())
                    fab.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fab.show();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (summary_layout.isShown()) summary_layout.setVisibility(View.GONE);
                else summary_layout.setVisibility(View.VISIBLE);
            }
        });


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
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("Dict", "PreferenceChanged: " + key);
        if (key.equals(getString(R.string.settings_date_filter_key))) {
            NEED_REMOTE_UPDATE = true;
        }
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Show Select action alert dialog
     *
     * @param position ---------------------------------------------------------------------------------------------
     */
    private void showActionDialog(int position) {
        final Context context = MainActivityEarthquakesList.this;
        final int pos = position;
        CharSequence[] items = {"Show on Map", "Details", "Feel it?"};

        new AlertDialog.Builder(MainActivityEarthquakesList.this)
                .setTitle("Action")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        if (item == 0) {       // Show on Map
                            int nDays = Integer.parseInt(dateFilter);
                            if (nDays <= daysLimit) {
                                Earthquake earthquake = earthquakes.get(pos);
                                Intent showEqOnMap = new Intent(context, MapsActivity.class);
                                showEqOnMap.putExtra("ShowEquake", "true");
                                showEqOnMap.putExtra("equake_lat",
                                        Double.toString(earthquake.getLatitude()));
                                showEqOnMap.putExtra("equake_lng",
                                        Double.toString(earthquake.getLongitude()));
                                startActivity(showEqOnMap);
                            }else{
                                Toast.makeText(MainActivityEarthquakesList.this,
                                        getString(R.string.use_of_map_forbidden), Toast.LENGTH_LONG).show();
                            }

                        } else if (item == 1) {  // USGS site Details
                            Earthquake earthquake = earthquakes.get(pos);
                            String url = earthquake.getUrl();
                            Log.i("setOnItemClickListener", "onItemClick: " + url);

                            // Open the related url page of the eq clicked
                            Uri webpage = Uri.parse(url);
                            Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                            startActivity(Intent.createChooser(webIntent, "Open details"));

                        } else if (item == 2) {  // Feel it?
                            Earthquake earthquake = earthquakes.get(pos);
                            String url = earthquake.getUrl() + "/tellus";
                            Log.i("setOnItemClickListener", "onItemClick: " + url);

                            // Open the related url page of the eq clicked
                            Uri webpage = Uri.parse(url);
                            Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                            startActivity(Intent.createChooser(webIntent, "Feel it? Tell us"));
                        }
                    }
                })
                .show();

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Set and check location coordinates from shared preferences.
     * If not set, put default value
     * ---------------------------------------------------------------------------------------------
     */
    private void checkPreferences() {



        // get coords from preferences
        lat_s = sharedPreferences.getString(getString(R.string.device_lat), Double.toString(DEFAULT_LAT));
        lng_s = sharedPreferences.getString(getString(R.string.device_lng), Double.toString(DEFAULT_LNG));


        // set default coord if there are no one
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (lat_s.isEmpty() == true) {
            editor.putString(getString(R.string.device_lat), Double.toString(DEFAULT_LAT));
            editor.apply();
        }
        if (lng_s.isEmpty() == true) {
            editor.putString(getString(R.string.device_lng), Double.toString(DEFAULT_LNG));
            editor.apply();
        }

        // Toast.makeText(this, "Current Location : lat : " + lat_s + " long : " + lng_s, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onResume: Current Location : lat : " + lat_s + " long : " + lng_s);


        // recover last update from preferences
        lastUpdate = sharedPreferences.getString(
                getString(R.string.last_update),
                DEFAULT_LAST_UPDATE);

        // recover min magnitude value from prefs or set a default from string value
        minMagnitude = sharedPreferences.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));


        // recover preferred order by param from prefs or set a default from string value
        orderBy = sharedPreferences.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));


        // recover preferred date filter by param from prefs or set a default from string value
        dateFilter = sharedPreferences.getString(
                getString(R.string.settings_date_filter_key),
                getString(R.string.settings_date_filter_default));

        if ((dateFilter.equals(getString(R.string.settings_date_period_today_value))))
            dateFilterLabel = getString(R.string.settings_date_period_today_label);
        else if ((dateFilter.equals(getString(R.string.settings_date_period_24h_value))))
            dateFilterLabel = getString(R.string.settings_date_period_24h_label);

        else if ((dateFilter.equals(getString(R.string.settings_date_period_48h_value))))
            dateFilterLabel = getString(R.string.settings_date_period_48h_label);
        /*
        else if ((dateFilter.equals(getString(R.string.settings_date_period_3_days_value))))
            dateFilterLabel = getString(R.string.settings_date_period_3_days_label);

        else if ((dateFilter.equals(getString(R.string.settings_date_period_4_days_value))))
            dateFilterLabel = getString(R.string.settings_date_period_4_days_label);
        */
        else if ((dateFilter.equals(getString(R.string.settings_date_period_week_value))))
            dateFilterLabel = getString(R.string.settings_date_period_week_label);

        else if ((dateFilter.equals(getString(R.string.settings_date_period_2_week_value))))
            dateFilterLabel = getString(R.string.settings_date_period_2_week_label);
        /* #68
        else if ((dateFilter.equals(getString(R.string.settings_date_period_month_value))))
            dateFilterLabel = getString(R.string.settings_date_period_month_label);
        */


        // recover min magnitude value from prefs or set a default from string value
        location_address = sharedPreferences.getString(
                getString(R.string.location_address),
                DEFAULT_ADDRESS);

        /* left in case of debug
        // recover preferred equakes num to display
        numEquakes = sharedPreferences.getString(
                getString(R.string.settings_max_equakes_key),
                getString(R.string.settings_max_equakes_default));
        */

        // check preferences safety
        safePreferencesValue(editor);

        // summary above list
        setFilterSummary();
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * making code more robust checking if for same reasons the defaut value stored are null or
     * not equals to none of the preferences stored values (e.g.  in case of key value change on code
     * but user saved with the previous one with previous app version )
     *
     * @param editor ---------------------------------------------------------------------------------------------
     */
    private void safePreferencesValue(SharedPreferences.Editor editor) {

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
        if (minMagnitude.isEmpty() || minMagnitude == null) {
            minMagnitude = getString(R.string.settings_min_magnitude_default);
            editor.putString(getString(R.string.settings_min_magnitude_key), getString(R.string.settings_min_magnitude_default));
        }

        if ((!minMagnitude.equals(getString(R.string.settings_1_0_min_magnitude_value))) &&
                (!minMagnitude.equals(getString(R.string.settings_2_0_min_magnitude_value))) &&
                (!minMagnitude.equals(getString(R.string.settings_3_0_min_magnitude_value))) &&
                (!minMagnitude.equals(getString(R.string.settings_4_0_min_magnitude_value))) &&
                (!minMagnitude.equals(getString(R.string.settings_4_5_min_magnitude_label))) &&
                (!minMagnitude.equals(getString(R.string.settings_5_0_min_magnitude_label))) &&
                (!minMagnitude.equals(getString(R.string.settings_5_5_min_magnitude_value))) &&
                (!minMagnitude.equals(getString(R.string.settings_6_0_min_magnitude_value))) &&
                (!minMagnitude.equals(getString(R.string.settings_6_5_min_magnitude_value)))
        ) {
            minMagnitude = getString(R.string.settings_min_magnitude_default);
            editor.putString(getString(R.string.settings_min_magnitude_key), getString(R.string.settings_min_magnitude_default));
        }

        // orderBy safe
        if (orderBy.isEmpty() || orderBy == null) {
            orderBy = getString(R.string.settings_order_by_default);
            editor.putString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));
        }

        if ((!orderBy.equals(getString(R.string.settings_order_by_desc_magnitude_value))) &&
                (!orderBy.equals(getString(R.string.settings_order_by_asc_magnitude_value))) &&
                (!orderBy.equals(getString(R.string.settings_order_by_most_recent_value))) &&
                (!orderBy.equals(getString(R.string.settings_order_by_oldest_value))) &&
                (!orderBy.equals(getString(R.string.settings_order_by_nearest_value))) &&
                (!orderBy.equals(getString(R.string.settings_order_by_furthest_value)))
        ) {
            orderBy = getString(R.string.settings_order_by_default);
            editor.putString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));
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
        if (dateFilter.isEmpty() || dateFilter == null) {
            dateFilter = getString(R.string.settings_date_filter_default);
            editor.putString(getString(R.string.settings_date_filter_key), getString(R.string.settings_date_filter_default));
        }

        if ((!dateFilter.equals(getString(R.string.settings_date_period_today_value))) &&
                (!dateFilter.equals(getString(R.string.settings_date_period_24h_value))) &&
                (!dateFilter.equals(getString(R.string.settings_date_period_48h_value))) &&
                /*(!dateFilter.equals(getString(R.string.settings_date_period_3_days_value))) &&
                (!dateFilter.equals(getString(R.string.settings_date_period_4_days_value))) && */
                (!dateFilter.equals(getString(R.string.settings_date_period_week_value))) &&
                (!dateFilter.equals(getString(R.string.settings_date_period_2_week_value)))
                // #68 && (!dateFilter.equals(getString(R.string.settings_date_period_month_value)))
        ) {
            dateFilter = getString(R.string.settings_date_filter_default);
            editor.putString(getString(R.string.settings_date_filter_key), getString(R.string.settings_date_filter_default));
        }


        if (location_address.isEmpty() || location_address == null) {
            location_address = DEFAULT_ADDRESS;
            editor.putString(getString(R.string.location_address), location_address);
        }

    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Summarize the filter settings for the eq list shown
     * ---------------------------------------------------------------------------------------------
     */
    private void setFilterSummary() {
        // set up filter summary
        // order by
        if (orderBy.equals(getString(R.string.settings_order_by_desc_magnitude_value)))
            order_value_tv.setText(getString(R.string.settings_order_by_desc_magnitude_label));

        if (orderBy.equals(getString(R.string.settings_order_by_asc_magnitude_value)))
            order_value_tv.setText(getString(R.string.settings_order_by_asc_magnitude_label));

        if (orderBy.equals(getString(R.string.settings_order_by_most_recent_value)))
            order_value_tv.setText(getString(R.string.settings_order_by_most_recent_label));

        if (orderBy.equals(getString(R.string.settings_order_by_oldest_value)))
            order_value_tv.setText(getString(R.string.settings_order_by_oldest_label));

        if (orderBy.equals(getString(R.string.settings_order_by_nearest_value)))
            order_value_tv.setText(getString(R.string.settings_order_by_nearest_label));

        if (orderBy.equals(getString(R.string.settings_order_by_furthest_value)))
            order_value_tv.setText(getString(R.string.settings_order_by_furthest_label));

        // min magnitude
        minMagn_value_tv.setText(minMagnitude);

        // last update time
        lastUp_value_tv.setText(lastUpdate);

        // time range
        if ((dateFilter.equals(getString(R.string.settings_date_period_today_value))))
            eq_period_value_tv.setText(getString(R.string.settings_date_period_today_label));

        else if ((dateFilter.equals(getString(R.string.settings_date_period_24h_value))))
            eq_period_value_tv.setText(getString(R.string.settings_date_period_24h_label));

        else if ((dateFilter.equals(getString(R.string.settings_date_period_48h_value))))
            eq_period_value_tv.setText(getString(R.string.settings_date_period_48h_label));
        /*
        else if ((dateFilter.equals(getString(R.string.settings_date_period_3_days_value))))
            dateFilterLabel = getString(R.string.settings_date_period_3_days_label);

        else if ((dateFilter.equals(getString(R.string.settings_date_period_4_days_value))))
            dateFilterLabel = getString(R.string.settings_date_period_4_days_label);
        */
        else if ((dateFilter.equals(getString(R.string.settings_date_period_week_value))))
            eq_period_value_tv.setText(getString(R.string.settings_date_period_week_label));

        else if ((dateFilter.equals(getString(R.string.settings_date_period_2_week_value))))
            eq_period_value_tv.setText(getString(R.string.settings_date_period_2_week_label));
        /*
        else if ((dateFilter.equals(getString(R.string.settings_date_period_month_value))))
            eq_period_value_tv.setText(getString(R.string.settings_date_period_month_label));
        */

        //location address
        location_value_tv.setText(location_address);
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Get earthquake list, from db or if necessary from restful service
     * ---------------------------------------------------------------------------------------------
     */
    private void retrieveData() {
        Log.i(TAG, "retrieveRemoteData: Requesting fresh data.");


        // show empty list and load in progress
        showLoading();

        // TODO : temporary, data must be updated setting an observer in repository on specific preference
        // check if date filter or other preferences which need remote update has been changed
        if (NEED_REMOTE_UPDATE) {
            retrieveRemoteData();
            NEED_REMOTE_UPDATE = false;
        } else {
            updateList();
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Update the adapter/equakes list, based on user's preferences, using viewmodel/livedata
     * ---------------------------------------------------------------------------------------------
     */
    private void updateList() {
        Log.i(TAG, "updateList Executing ");
        // clear the adapter of previous data
        // adapter.resetEarthquakesEntries();

        checkPreferences();


        // set equakes list showing based on user preferences
        if (orderBy.equals(getString(R.string.settings_order_by_desc_magnitude_value))) {
            MainViewModelFactory factory = new MainViewModelFactory(ORDER_BY_DESC_MAGNITUDE);
            filterDatafromRepository(factory);

        } else if (orderBy.equals(getString(R.string.settings_order_by_asc_magnitude_value))) {
            MainViewModelFactory factory = new MainViewModelFactory(ORDER_BY_ASC_MAGNITUDE);
            filterDatafromRepository(factory);

        } else if (orderBy.equals(getString(R.string.settings_order_by_most_recent_value))) {
            MainViewModelFactory factory = new MainViewModelFactory(ORDER_BY_MOST_RECENT);
            filterDatafromRepository(factory);

        } else if (orderBy.equals(getString(R.string.settings_order_by_oldest_value))) {
            MainViewModelFactory factory = new MainViewModelFactory(ORDER_BY_OLDEST);
            filterDatafromRepository(factory);

        } else if (orderBy.equals(getString(R.string.settings_order_by_nearest_value))) {
            MainViewModelFactory factory = new MainViewModelFactory(ORDER_BY_NEAREST);
            filterDatafromRepository(factory);

        } else if (orderBy.equals(getString(R.string.settings_order_by_furthest_value))) {
            MainViewModelFactory factory = new MainViewModelFactory(ORDER_BY_FURTHEST);
            filterDatafromRepository(factory);
        }


    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Make repository request for specific data and ordering
     *
     * @param factory
     * ---------------------------------------------------------------------------------------------
     */
    private void filterDatafromRepository(MainViewModelFactory factory) {
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

        final MainViewModel viewModel = ViewModelProviders
                .of(this, factory)
                .get(MainViewModel.class);


        LiveData<List<Earthquake>> equakes = viewModel.getEqList();
        equakes.observe(this, new Observer<List<Earthquake>>() {
            @Override
            public void onChanged(@Nullable List<Earthquake> earthquakeEntries) {
                if (earthquakeEntries != null && !earthquakeEntries.isEmpty()) { // data ready in db
                    earthquakes = earthquakeEntries;
                    updateAdapter(earthquakeEntries);
                    // used to update the last update field, updated by datasource at 1st start
                    checkPreferences();
                    showEarthquakeListView();
                } else {                                                         // waiting for data
                    // While waiting that the repository getting aware that the eqs list is empty
                    // and ask for a remote update
                    if (MyUtil.INSTANCE.isConnectionOk()) {
                        showLoading();
                    } else {
                        showNoInternetConnection();
                    }
                }
            }
        });
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Notify and update adapter data
     *
     * @param earthquakeEntries
     * ---------------------------------------------------------------------------------------------
     */
    private void updateAdapter(@Nullable List<Earthquake> earthquakeEntries) {
        adapter.setEarthquakesEntries(earthquakeEntries);
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Retrieve Remote Data.
     * Check internet connection availability first
     * ---------------------------------------------------------------------------------------------
     */
    private void retrieveRemoteData() {

        if (MyUtil.INSTANCE.isConnectionOk()) {
            showLoading();
            // LoaderManager reference
            LoaderManager loaderManager = getLoaderManager();
            // Init loader : id above, bundle = null , this= current activity for LoaderCallbacks
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);

        } else {
            showNoInternetConnection();
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Show No Internet Connection view
     * ---------------------------------------------------------------------------------------------
     */
    private void showNoInternetConnection() {
        // hide progress bar
        loadingInProgress.setVisibility(View.GONE);
        filter_memo.setVisibility(View.INVISIBLE);
        earthquakeListView.setVisibility(View.GONE);
        emptyListText.setVisibility(View.VISIBLE);
        emptyListText.setText(R.string.no_internet_connection);
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Show loading in progress view, hiding earthquake list
     * ---------------------------------------------------------------------------------------------
     */
    private void showLoading() {
        loadingInProgress.setVisibility(View.VISIBLE);
        filter_memo.setVisibility(View.INVISIBLE);
        earthquakeListView.setVisibility(View.GONE);
        emptyListText.setVisibility(View.VISIBLE);
        emptyListText.setText(R.string.searching);
    }

    /**
     * Show hearthquake list after loading/retrieving data completed
     */
    private void showEarthquakeListView() {
        loadingInProgress.setVisibility(View.INVISIBLE);
        filter_memo.setVisibility(View.VISIBLE);
        earthquakeListView.setVisibility(View.VISIBLE);
        emptyListText.setVisibility(View.INVISIBLE);
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
    @Override
    public Loader<List<Earthquake>> onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "onCreateLoader: Create a new Loader");
        String urlReq = MyUtil.INSTANCE.composeQueryUrl(dateFilter);
        Log.i(TAG, "onCreateLoader: urlReq : " + urlReq);
        // create a new loader for the url
        return new EarthquakeAsyncLoader(this, urlReq);
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
    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakesReturnedByLoader) {
        Log.i(TAG, "onLoadFinished: Loader return back with data");

        // hide progress bar
        // loadingInProgress.setVisibility(View.GONE);

        // Set empty state text to display "No earthquakes found."
        emptyListText.setText(R.string.no_earthquakes);

        // --> update UI when loader finished
        if (setEartquakesList(earthquakesReturnedByLoader)) {
            updateList();

            // update preferences
            lastUpdate = MyUtil.INSTANCE.setLastUpdateField(this);

            lastUp_value_tv.setText(lastUpdate);

            // show filter summary
            filter_memo.setVisibility(View.VISIBLE);

            Toast alert = Toast.makeText(MainActivityEarthquakesList.this,
                    getString(R.string.data_update_toast) + dateFilterLabel
                    , Toast.LENGTH_LONG);
            alert.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            alert.show();

        } else {
            Log.i(TAG, "Problem with earthquake list, is empty. Check the request. ");
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
    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        Log.i(TAG, "onLoaderReset: Reset Loader previous data");
        // reset loader to clean up previous data
        adapter.resetEarthquakesEntries();
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Used by onLoadFinished to populate the ArrayList fetched
     *
     * @param earthquakes
     * @return true/false
     * ---------------------------------------------------------------------------------------------
     */
    protected boolean setEartquakesList(List<Earthquake> earthquakes) {
        if ((earthquakes != null) && (earthquakes.isEmpty() == false)) {
            // this.earthquakes = earthquakes;
            // updateList();
            return true;
        } else {
            Log.i(TAG, "The earthquake list is empty. Check the request. ");
            Toast.makeText(this, "The earthquake list is empty. Check the request. ", Toast.LENGTH_LONG).show();
            return false;
        }

    }


    // ---------------------------------------------------------------------------------------------
    //                                          MENU STUFF
    // ---------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.quick_settings:
                showDialog();
                return true;
            case R.id.general_settings:
                Intent settingsIntent = new Intent(this, SettingSimpleActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.set_myposition_action:
                int nDays = Integer.parseInt(dateFilter);
                if (nDays <= daysLimit) {
                    Intent setMapIntent = new Intent(this, MapsActivity.class);
                    startActivity(setMapIntent);
                }else{
                    Toast.makeText(this, getString(R.string.use_of_map_forbidden), Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.refresh_action:
                showLoading();
                retrieveRemoteData();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Dialog with spinner for quick settings
     * ---------------------------------------------------------------------------------------------
     */
    private void showDialog() {
        android.support.v7.app.AlertDialog.Builder builder =
                new android.support.v7.app.AlertDialog.Builder(MainActivityEarthquakesList.this);
        View view = getLayoutInflater().inflate(R.layout.quick_settings_dialog, null);
        /*
        CheckBox saveFlag = (CheckBox)findViewById(R.id.dialog_checkBox);
        //TODO : update with choice to save or not list when passing to acc
        saveFlag.setVisibility(View.INVISIBLE);
        */
        builder.setTitle(getResources().getString(R.string.quick_settings_title));

        // 1 -  spinner order by choice : spinner_order_by
        final Spinner spinner_order_by = (Spinner) view.findViewById(R.id.order_by_spinner);
        // list of labels for order by spinner list
        final List<String> order_list = new ArrayList<>(); // add header
        order_list.add(getResources().getString(R.string.spinner_defaultchoice_label));
        order_list.addAll(Arrays.asList(getResources().getStringArray(R.array.settings_order_by_labels)));

        // list of  values corresponding positionally in list to the labels
        final List<String> order_list_values = new ArrayList<>(); // add header
        order_list_values.add(getResources().getString(R.string.spinner_defaultchoice_value));
        order_list_values.addAll(Arrays.asList(getResources().getStringArray(R.array.settings_order_by_values)));

        // put labels in spinner
        ArrayAdapter<String> adapter_01 = new ArrayAdapter<String>(MainActivityEarthquakesList.this,
                android.R.layout.simple_spinner_item,
                order_list);
        adapter_01.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner_order_by.setAdapter(adapter_01);


        // 2 - spinner choose min magnitude : spinner_min_magnitude
        final Spinner spinner_min_magnitude = (Spinner) view.findViewById(R.id.min_magnitude_spinner);
        // list of labels for magnitude min  spinner list
        final List<String> magn_list = new ArrayList<>(); // add header
        magn_list.add(getResources().getString(R.string.spinner_defaultchoice_label));
        magn_list.addAll(Arrays.asList(getResources().getStringArray(R.array.settings_array_min_magnitude_labels)));

        // list of  values corresponding positionally in list to the labels
        final List<String> magn_list_values = new ArrayList<>(); // add header
        magn_list_values.add(getResources().getString(R.string.spinner_defaultchoice_value));
        magn_list_values.addAll(Arrays.asList(getResources().getStringArray(R.array.settings_array_min_magnitude_values)));

        // put labels in spinner
        ArrayAdapter<String> adapter_02 = new ArrayAdapter<String>(MainActivityEarthquakesList.this,
                android.R.layout.simple_spinner_item,
                magn_list);
        adapter_02.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner_min_magnitude.setAdapter(adapter_02);


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // boolean restartActivity = false;   // need to restart the activity
                // boolean updateList      = false;   // need to sort  the list without restarting activity
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // check choices
                // convert labels to values

                String spinner_order_by_choice = convertFromLabelsToKeyvalues(
                        spinner_order_by.getSelectedItem().toString(),
                        order_list, order_list_values);


                String spinner_min_magn_choice = convertFromLabelsToKeyvalues(
                        spinner_min_magnitude.getSelectedItem().toString(),
                        magn_list, magn_list_values);

                // set value selected for filter in preference keys
                if (!spinner_order_by_choice.equals(getResources().getString(R.string.spinner_defaultchoice_value))) {
                    editor.putString(getString(R.string.settings_order_by_key),
                            spinner_order_by_choice);
                    editor.apply();
                    // updateList = true;
                }

                if (!spinner_min_magn_choice.equalsIgnoreCase(getResources().getString(R.string.spinner_defaultchoice_value))) {
                    editor.putString(getString(R.string.settings_min_magnitude_key),
                            spinner_min_magn_choice);
                    editor.apply();
                    // restartActivity = true;
                }

                Log.i(TAG, "onClick: ");
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

                dialog.dismiss();
                MyUtil.INSTANCE.restartActivity(MainActivityEarthquakesList.this);


            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setView(view);
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();

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
    private String convertFromLabelsToKeyvalues(String selectedItem,
                                                final List<String> labelsList,
                                                final List<String> valuesList) {
        int position = labelsList.indexOf(selectedItem);
        return valuesList.get(position);
    }


}