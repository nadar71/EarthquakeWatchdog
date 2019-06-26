
package com.indiewalk.watchdog.earthquake.UI;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
// Loader lib stuff
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.app.AlertDialog;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.indiewalk.watchdog.earthquake.MapsActivity;
import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.net.EarthquakeAsyncLoader;
import com.indiewalk.watchdog.earthquake.data.Earthquake;
import com.indiewalk.watchdog.earthquake.util.ConsentSDK;
import com.indiewalk.watchdog.earthquake.util.MyUtil;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;





public class MainActivity extends AppCompatActivity  implements LoaderCallbacks<List<Earthquake>> {

    public static final String TAG = MainActivity.class.getName();

    // this is the default position, google at mountain view
    public static final double DEFAULT_LAT = 37.4219999;
    public static final double DEFAULT_LNG = -122.0862515;

    // Key constant for view model parameters
    public static final String LOAD_ALL_NO_ORDER       = "load_all_no_order";
    public static final String ORDER_BY_DESC_MAGNITUDE = "magnitude_desc_ordering";
    public static final String ORDER_BY_ASC_MAGNITUDE  = "magnitude_asc_ordering";
    public static final String ORDER_BY_MOST_RECENT    = "most_recent";
    public static final String ORDER_BY_NEAREST        = "nearest";
    public static final String ORDER_BY_FARTHEST       = "farthest";

    // private MainViewModel mViewModel;


    ListView earthquakeListView;
    private List<Earthquake> earthquakes;

    //Progress bar
    private ProgressBar loadingInProgress;

    // Empty view while loading
    private TextView emptyListText;

    // ListView Adapter
    private EarthquakeAdapter adapter;

    // URL to query the USGS dataset for earthquake information 
    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query";
            // "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=6&limit=10"; // debug


    // Id for loader which retrieve data from remote source (not necessary there is only it!)
    private static final int EARTHQUAKE_LOADER_ID = 1;

    // Preferences value
    String minMagnitude, orderBy,lat_s, lng_s, numEquakes;

    // SharePreferences ref
    SharedPreferences sharedPreferences;

    // admob banner ref
    private AdView mAdView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Initialize ConsentSDK
        ConsentSDK consentSDK = new ConsentSDK.Builder(this)
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
                Log.i("gdpr_TAG", "onResult: isRequestLocationInEeaOrUnknown : "+isRequestLocationInEeaOrUnknown);
                // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
                mAdView.loadAd(ConsentSDK.getAdRequest(MainActivity.this));
            }
        });



        mAdView = findViewById(R.id.adView);

        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        mAdView.loadAd(ConsentSDK.getAdRequest(MainActivity.this));


        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                // Toast.makeText(MainActivity.this, "Adloaded ok", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                // Toast.makeText(MainActivity.this, "Adloaded FAILED TO LOAD "+errorCode, Toast.LENGTH_LONG).show();
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


        // init shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Find a reference to the {@link ListView} in the layout : using listView because
        // at the momento it has only tens of entry.
        // TODO : It will upgrade to RecycleView when a time interval would be available in preferences
        earthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new {@link EarthquakeAdapter} of {@link Earthquakes} objects
        adapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        // Set the adapter on the {@link ListView} so the list can be populated in the user interface
        earthquakeListView.setAdapter(adapter);



        // Clicking on item shows an action dialog
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showActionDialog(position);
            }
        });

        // check preferences for changes
        checkPreferences();

        // Call loader for retrieving data
        // retrieveRemoteData();
        retrieveData();


    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Show Select action alert dialog
     * @param position
     * ---------------------------------------------------------------------------------------------
     */
    private void showActionDialog(int position) {
        final Context context = MainActivity.this;
        final int pos = position;
        CharSequence[] items = {"Show on Map", "Details", "Feel it?"};

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Action")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        if(item==0){       // Show on Map
                            Earthquake earthquake = earthquakes.get(pos);
                            Intent showEqOnMap = new Intent(context, MapsActivity.class);
                            showEqOnMap.putExtra("ShowEquake","true");
                            showEqOnMap.putExtra("equake_lat",Double.toString(earthquake.getLatitude()));
                            showEqOnMap.putExtra("equake_lng",Double.toString(earthquake.getLongitude()));
                            startActivity(showEqOnMap);
                        }
                        else if(item==1){  // USGS site Details
                            Earthquake earthquake = earthquakes.get(pos);
                            String url = earthquake.getUrl();
                            Log.i("setOnItemClickListener", "onItemClick: "+url);

                            // Open the related url page of the eq clicked
                            Uri webpage = Uri.parse(url);
                            Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                            startActivity(Intent.createChooser(webIntent, "Open details"));

                        }
                        else if(item==2){  // Feel it?
                            Earthquake earthquake = earthquakes.get(pos);
                            String url = earthquake.getUrl() + "/tellus";
                            Log.i("setOnItemClickListener", "onItemClick: "+url);

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
     * Check if device location is stored by previous accessing in-map section, and in case retrieve
     * coordinates
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    protected void onResume() {
        super.onResume();
        // check preferences for changes
        checkPreferences();

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Set and check location coordinates from shared preferences.
     * If not set, put default value
     * ---------------------------------------------------------------------------------------------
     */
    private void checkPreferences() {

        lat_s = sharedPreferences.getString(getString(R.string.device_lat),Double.toString(DEFAULT_LAT));
        lng_s = sharedPreferences.getString(getString(R.string.device_lng),Double.toString(DEFAULT_LNG));


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


        // recover min magnitude value from prefs or set a default from string value
        minMagnitude = sharedPreferences.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));


        // recover preferred order by param from prefs or set a default from string value
        orderBy = sharedPreferences.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));



        // recover preferred equakes num to display
        numEquakes = sharedPreferences.getString(
                getString(R.string.settings_max_equakes_key),
                getString(R.string.settings_max_equakes_default));

        // check preferences safety
        safePreferencesValue(editor);

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * making code more robust checking if for same reasons the defaut value stored are null or
     * not equals to none of the preferences stored values (e.g.  in case of key value change on code
     * but user saved with the previous one with previous app version )
     * @param editor
     * ---------------------------------------------------------------------------------------------
     */
    private void safePreferencesValue(SharedPreferences.Editor editor) {

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
        ){
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
            (!orderBy.equals(getString(R.string.settings_order_by_nearest_value))) &&
            (!orderBy.equals(getString(R.string.settings_order_by_farthest_value)))
        ){
            orderBy = getString(R.string.settings_order_by_default);
            editor.putString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));
        }

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
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Get earthquake list, from db or if necessary from restful service
     * ---------------------------------------------------------------------------------------------
     */
    private void retrieveData() {
        Log.i(TAG, "retrieveRemoteData: Requesting fresh data.");
        // set progress bar
        loadingInProgress = findViewById(R.id.loading_spinner);

        // set Empty View in case of List empty
        emptyListText = findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(emptyListText);
        emptyListText.setText(R.string.searching);


        updateList();

        /*
        // check connection
        // reference to connection manager
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // network status retrieving
        NetworkInfo netinfo = connManager.getActiveNetworkInfo();

        if(netinfo != null && netinfo.isConnected()){
            // LoaderManager reference
            LoaderManager loaderManager = getLoaderManager();
            // Init loader : id above, bundle = null , this= current activity for LoaderCallbacks
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        }else{

            // hide progress bar
            loadingInProgress.setVisibility(View.GONE);
            emptyListText.setText(R.string.no_internet_connection);
        }
        */
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Update the adapter/equakes list, based on user's preferences, using viewmodel/livedata
     * ---------------------------------------------------------------------------------------------
     */
    private void updateList() {
        Log.i(TAG, "updateList Executing ");
        // clear the adapter of previous data
        adapter.clear();

        checkPreferences();


        // set equakes list showing based on user preferences
        if (orderBy.equals(getString(R.string.settings_order_by_desc_magnitude_value))){
            MainViewModelFactory factory= new MainViewModelFactory(ORDER_BY_DESC_MAGNITUDE);
            filterDatafromRepository(factory);

        } else if (orderBy.equals(getString(R.string.settings_order_by_asc_magnitude_value))){
            MainViewModelFactory factory= new MainViewModelFactory(ORDER_BY_ASC_MAGNITUDE);
            filterDatafromRepository(factory);

        } else if (orderBy.equals(getString(R.string.settings_order_by_most_recent_value))){
            MainViewModelFactory factory= new MainViewModelFactory(ORDER_BY_MOST_RECENT);
            filterDatafromRepository(factory);

        }  else if (orderBy.equals(getString(R.string.settings_order_by_nearest_value))){
            MainViewModelFactory factory= new MainViewModelFactory(ORDER_BY_NEAREST);
            filterDatafromRepository(factory);

        }  else if (orderBy.equals(getString(R.string.settings_order_by_farthest_value))){
            MainViewModelFactory factory= new MainViewModelFactory(ORDER_BY_FARTHEST);
            filterDatafromRepository(factory);

        }



    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Make repository request for specific data and ordering
     * @param factory
     * ---------------------------------------------------------------------------------------------
     */
    private void filterDatafromRepository(MainViewModelFactory factory) {
        // Get eq list through LiveData
        final MainViewModel viewModel = ViewModelProviders.of(this,factory).get(MainViewModel.class);

        LiveData<List<Earthquake>> equakes = viewModel.getEqList();
        equakes.observe(this, new Observer<List<Earthquake>>() {
            @Override
            public void onChanged(@Nullable List<Earthquake> earthquakeEntries) {
                if (earthquakeEntries != null && !earthquakeEntries.isEmpty()) {
                    earthquakes = earthquakeEntries;
                    adapter.addAll(earthquakeEntries);
                    adapter.notifyDataSetChanged();
                    loadingInProgress.setVisibility(View.GONE);

                } else {
                    // make  a request for remote data using loader
                    // TODO : incapsulate this check inside repository
                    retrieveRemoteData();
                }
            }
        });
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Retrieve Remote Data.
     * Check internet connection availability first
     * ---------------------------------------------------------------------------------------------
     */
    private void retrieveRemoteData() {
        // check connection
        // reference to connection manager
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // network status retrieving
        NetworkInfo netinfo = connManager.getActiveNetworkInfo();

        if(netinfo != null && netinfo.isConnected()){
            loadingInProgress.setVisibility(View.VISIBLE);
            // LoaderManager reference
            LoaderManager loaderManager = getLoaderManager();
            // Init loader : id above, bundle = null , this= current activity for LoaderCallbacks
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        }else{
            // hide progress bar
            loadingInProgress.setVisibility(View.GONE);
            emptyListText.setText(R.string.no_internet_connection);
            // earthquakeListView.setVisibility(View.GONE);
        }

    }




    /**
     * ---------------------------------------------------------------------------------------------
     * Create loader
     * @param id
     * @param args
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    public Loader<List<Earthquake>> onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "onCreateLoader: Create a new Loader");
        String urlReq = composeQueryUrl();
        Log.i(TAG, "onCreateLoader: urlReq : "+urlReq);
        // create a new loader for the url
        return new EarthquakeAsyncLoader(this, urlReq );
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Loader finished
     * @param loader
     * @param earthquakesReturnedByLoader
     * ---------------------------------------------------------------------------------------------
     */
    // TODO : in the future, use livedata/viewmodel for this: loader don't need to return the equake list structure
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
            // updateList();
            // Restarting the activity to make livedata allow set up
            // the new (eventually) retrieved data in the order defined in preferences
            MyUtil.restartActivity(MainActivity.this);
        } else {
            Log.i(TAG, "Problem with earthquake list, is empty. Check the request. ");
        }


    }





    /**
     * ---------------------------------------------------------------------------------------------
     * Loader reset
     * @param loader
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        Log.i(TAG, "onLoaderReset: Reset Loader previous data");
        // reset loader to clean up previous data
        adapter.clear();
    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Used by onLoadFinished to populate the ArrayList fetched
     * @param earthquakes
     * @return true/false
     * ---------------------------------------------------------------------------------------------
     */
    protected boolean setEartquakesList(List<Earthquake> earthquakes){
        if (  (earthquakes != null) && (earthquakes.isEmpty() == false)  ){
            // this.earthquakes = earthquakes;
            // updateList();
            return true;
        }else{
            Log.i(TAG, "The earthquake list is empty. Check the request. ");
            Toast.makeText(this, "The earthquake list is empty. Check the request. ", Toast.LENGTH_LONG).show();
            return false;
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Compose a query url starting from preferences parameters
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public String composeQueryUrl(){
        Uri rootUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder builder = rootUri.buildUpon();

        builder.appendQueryParameter("format","geojson");

        // commented, it creates only problem, will be substituted with user preferred time range
        // builder.appendQueryParameter("limit",numEquakes);

        // calculate 30-days ago date and set as start date
        // String aMonthAgo = MyUtil.oldDate(30).toString();
        // builder.appendQueryParameter("starttime",aMonthAgo);

        // TODO : must make this part update by user preferences
        // calculate 7-days ago date and set as start date
        String aWeekAgo = MyUtil.oldDate(7).toString();
        builder.appendQueryParameter("starttime",aWeekAgo);


        /*
        builder.appendQueryParameter("minmag",minMagnitude); // TODO : delete

        if (!orderBy.equals(getString(R.string.settings_order_by_nearest_value))
                && !orderBy.equals(getString(R.string.settings_order_by_farthest_value)) ){
            orderBy = getString(R.string.settings_order_by_default);
            builder.appendQueryParameter("orderby", orderBy);     // TODO : delete
        }
        */


        return  builder.toString();
    }


    // ---------------------------------------------------------------------------------------------
    //                                          MENU STUFF
    // ---------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
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
                // Intent setMapIntent = new Intent(this, MyPositionActivity.class);
                Intent setMapIntent = new Intent(this, MapsActivity.class);
                startActivity(setMapIntent);
                return true;
            case R.id.refresh_action:
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
    private void showDialog(){
        android.support.v7.app.AlertDialog.Builder  builder =
                new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
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
        final List<String> order_list  = new ArrayList<>(); // add header
        order_list.add(getResources().getString(R.string.spinner_defaultchoice_label));
        order_list.addAll(Arrays.asList(getResources().getStringArray(R.array.settings_order_by_labels)));

        // list of  values corresponding positionally in list to the labels
        final List<String> order_list_values = new ArrayList<>(); // add header
        order_list_values.add(getResources().getString(R.string.spinner_defaultchoice_value));
        order_list_values.addAll(Arrays.asList(getResources().getStringArray(R.array.settings_order_by_values)));

        // put labels in spinner
        ArrayAdapter<String> adapter_01 = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item,
                order_list);
        adapter_01.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner_order_by.setAdapter(adapter_01);



        // 2 - spinner choose min magnitude : spinner_min_magnitude
        final Spinner spinner_min_magnitude = (Spinner) view.findViewById(R.id.min_magnitude_spinner);
        // list of labels for magnitude min  spinner list
        final List<String> magn_list  = new ArrayList<>(); // add header
        magn_list.add(getResources().getString(R.string.spinner_defaultchoice_label));
        magn_list.addAll(Arrays.asList(getResources().getStringArray(R.array.settings_min_magnitude_labels)));

        // list of  values corresponding positionally in list to the labels
        final List<String> magn_list_values = new ArrayList<>(); // add header
        magn_list_values.add(getResources().getString(R.string.spinner_defaultchoice_value));
        magn_list_values.addAll(Arrays.asList(getResources().getStringArray(R.array.settings_min_magnitude_values)));

        // put labels in spinner
        ArrayAdapter<String> adapter_02 = new ArrayAdapter<String>(MainActivity.this,
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
                if(!spinner_order_by_choice.equals(getResources().getString(R.string.spinner_defaultchoice_value))){
                    editor.putString(getString(R.string.settings_order_by_key),
                            spinner_order_by_choice );
                    editor.apply();
                    // updateList = true;
                }

                if(!spinner_min_magn_choice.equalsIgnoreCase(getResources().getString(R.string.spinner_defaultchoice_value))) {
                    editor.putString(getString(R.string.settings_min_magnitude_key),
                            spinner_min_magn_choice );
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
                    MyUtil.restartActivity(MainActivity.this);
                }
                */

                dialog.dismiss();
                MyUtil.restartActivity(MainActivity.this);



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
     * @param selectedItem
     * @param labelsList
     * @param valuesList
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    private String convertFromLabelsToKeyvalues(String selectedItem,
                                                final List<String> labelsList,
                                                final List<String> valuesList){
        int position = labelsList.indexOf(selectedItem);
        return valuesList.get(position);
    }



}