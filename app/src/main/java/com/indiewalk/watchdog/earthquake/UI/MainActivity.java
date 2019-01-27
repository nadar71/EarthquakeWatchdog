
package com.indiewalk.watchdog.earthquake.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
// Loader lib stuff
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.app.AlertDialog;

import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.android.utils.FlipperUtils;
import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin;
import com.facebook.soloader.SoLoader;
import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.net.EarthquakeAsyncLoader;
import com.indiewalk.watchdog.earthquake.data.Earthquake;


import java.util.ArrayList;
import java.util.List;


// ** for DEBUG reason
import com.indiewalk.watchdog.earthquake.BuildConfig;
import com.indiewalk.watchdog.earthquake.util.MyUtil;


public class MainActivity extends AppCompatActivity  implements LoaderCallbacks<List<Earthquake>> {

    public static final String TAG = MainActivity.class.getName();

    public static final double DEFAULT_LAT = 37.4219999;
    public static final double DEFAULT_LNG = -122.0862515;

    ListView earthquakeListView;
    private List<Earthquake> earthquakes;

    //Progress bar
    private ProgressBar loadingInProgress;

    // Empty view
    private TextView emptyListText;

    // ListView Adapter
    private EarthquakeAdapter adapter;

    // URL to query the USGS dataset for earthquake information 
    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query";
            // "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=6&limit=10"; // debug


    // Constant  id for loader which retrieve data from remote source (not necessary there is only it!)
    private static final int EARTHQUAKE_LOADER_ID = 1;

    // Preferences value
    String minMagnitude, orderBy,lat_s, lng_s;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);



        // Using flipper for debugging share preferences file
        SoLoader.init(this, false);
        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            final FlipperClient client = AndroidFlipperClient.getInstance(this);
            client.addPlugin(new InspectorFlipperPlugin(getApplicationContext(), DescriptorMapping.withDefaults()));
            client.addPlugin(new SharedPreferencesFlipperPlugin(getApplicationContext(), "my_shared_preference_file"));
            client.start();
        }




        // TODO : ASK FOR GEOLOCALIZATION PERMISSION
            // TODO : IF OK  : COMPUTE THE DISTANCE FROM EQ
            // TODO : IF NOT : COMPUTE DISTANCE FROM DEFAULT : the default is that from emulator, check with 8.0 avd

        // Find a reference to the {@link ListView} in the layout : using listView because it has only tens of
        // entry, otherwise RecycleView would be better
        earthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new {@link EarthquakeAdapter} of {@link Earthquakes} objects
        adapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        // Set the adapter on the {@link ListView} so the list can be populated in the user interface
        earthquakeListView.setAdapter(adapter);




        // Click on item
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                showActionDialog(position);
                /*
                // get url data
                Earthquake earthquake = earthquakes.get(position);
                String url = earthquake.getUrl();
                Log.i("setOnItemClickListener", "onItemClick: "+url);

                // Open the related url page of the eq clicked
                Uri webpage = Uri.parse(url);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(Intent.createChooser(webIntent, "Open details"));
                */
            }
        });

        // check preferences for changes
        checkPreferences();

        // Call loader for retrieving data
        retrieveRemoteData();


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

        // hide navbar
        /*
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            this.getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            // View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                            // View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                            // View.SYSTEM_UI_FLAG_FULLSCREEN|
                            // View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            );
        }
        */


        // check preferences for changes
        checkPreferences();

    }


    /**
     * Check location coordinates from shared preferences.
     * If not set, put defaut value
     */
    private void checkPreferences() {
        // init shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        Toast.makeText(this, "Current Location : lat : " + lat_s + " long : " + lng_s, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onResume: Current Location : lat : " + lat_s + " long : " + lng_s);


        // recover min magnitude value from prefs or set a default from string value
        minMagnitude = sharedPreferences.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));


        // recover preferred order by param from prefs or set a default from string value
        orderBy = sharedPreferences.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
    }

    

    /**
     * ---------------------------------------------------------------------------------------------
     * Retrieve Remote Data.
     * Check internet connectin availability first
     * ---------------------------------------------------------------------------------------------
     */
    private void retrieveRemoteData() {
        Log.i(TAG, "retrieveRemoteData: Requesting fresh data.");
        // set progress bar
        loadingInProgress = findViewById(R.id.loading_spinner);

        // set Empty View in case of List empty
        emptyListText = findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(emptyListText);
        emptyListText.setText(R.string.searching);

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
    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakesReturnedByLoader) {
        Log.i(TAG, "onLoadFinished: Loader return back with data");

        // hide progress bar
        loadingInProgress.setVisibility(View.GONE);

        // Set empty state text to display "No earthquakes found."
        emptyListText.setText(R.string.no_earthquakes);

        // clear the adapter of previous data
        adapter.clear();

        // update UI when loader finishes its job
        if (setEartquakesList(earthquakesReturnedByLoader)==true) {
            adapter.addAll(earthquakes);
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
            this.earthquakes = earthquakes;
            return true;
        }else{
            Log.i(TAG, "The earthquake list is empty. Check the request. ");
            Toast.makeText(this, "The earthquake list is empty. Check the request. ", Toast.LENGTH_LONG).show();
            return false;
        }
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Set user's distance for each equake
     * @param earthquakes
     * ---------------------------------------------------------------------------------------------
     */
    private void setDistanceFromUser(List<Earthquake> earthquakes){
        // check that location coords are set
        checkPreferences();

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
        builder.appendQueryParameter("limit","30");
        builder.appendQueryParameter("minmag",minMagnitude);
        builder.appendQueryParameter("orderby",orderBy);

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



}