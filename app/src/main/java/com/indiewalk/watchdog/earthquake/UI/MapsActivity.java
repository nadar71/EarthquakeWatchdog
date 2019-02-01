package com.indiewalk.watchdog.earthquake.UI;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.ayoubfletcher.consentsdk.ConsentSDK;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.data.Earthquake;
import com.indiewalk.watchdog.earthquake.data.EarthquakeDatabase;
import com.indiewalk.watchdog.earthquake.util.AppExecutors;
import com.indiewalk.watchdog.earthquake.util.MyUtil;


import android.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * -------------------------------------------------------------------------------------------------
 * Show earthquakes positions, as well user's one.
 * -------------------------------------------------------------------------------------------------
 */
public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();

    Context context = MapsActivity.this;

    // Map references
    GoogleMap           mGoogleMap;

    // Map frag
    SupportMapFragment  mapFrag;

    // Device location req references
    LocationRequest     mLocationRequest;
    Location            mLastLocation;
    FusedLocationProviderClient mFusedLocationClient = null;

    // Marker for my current position by gps
    Marker              myCurrentPositionMarker = null;

    // List of all the earthquake in db
    // TODO: retrieve them more efficiently with livedata & c.
    List<Earthquake> earthquakes;

    // Markers associated with earthquake on map
    List<Marker> earthquakesMarkersList;

    // Db reference
    // TODO : create and interface with repository
    EarthquakeDatabase eqDb;

    // Maps type list
    private static final CharSequence[] MAP_TYPE_ITEMS =
            {"Road Map", "Hybrid", "Satellite", "Terrain"};

    // searching location progress dialog
    ProgressDialog dialog;

    // SharedPrefences ref actvity global
    SharedPreferences sharedPreferences;

    // Manual Localization flag
    boolean manualLocIsOn = false;

    // Manual localization menu item ref
    MenuItem locCheckbox;

    // User location coords
    String lat_s, lng_s;

    // admob banner ref
    private AdView mAdView;


    /**
     * ---------------------------------------------------------------------------------------------
     * Load equakes list, set up ma and position
     * @param savedInstanceState
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // -----------------------------------------------------------------------------------------
        // Init admob
        // Sample AdMob banner ID:         ca-app-pub-3940256099942544~3347511713
        // THIS APP REAL AdMob banner ID:  ca-app-pub-8846176967909254~9979565057
        // -----------------------------------------------------------------------------------------
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        // load ads banner
        mAdView = findViewById(R.id.adView);

        /*
        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("7DC1A1E8AEAD7908E42271D4B68FB270")
                .build();
        mAdView.loadAd(adRequest);
        */

        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        mAdView.loadAd(ConsentSDK.getAdRequest(MapsActivity.this));

        // get db instance
        eqDb = EarthquakeDatabase.getDbInstance(getApplicationContext());

        // retrieve eq currently in db, in different thread
        // TODO: implements livedata/viewmodel/repository
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                earthquakes = eqDb.earthquakeDbDao().loadAll();
            }
        });

        // init shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // back btn
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Show on map
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        // check if there was a previous user location manual setted
        checkManualLocation();

        if ( manualLocIsOn == false) {
            // standard title
            getSupportActionBar().setTitle(getString(R.string.title_activity_maps_standard));

            // localize user
            localizeUser();

        } else {
            // manual loc on title
            getSupportActionBar().setTitle(Html.fromHtml("<font color='#66ff66'>"+
                    getString(R.string.title_activity_maps_manual_localization_on) +"</font>"));
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Launch User localization
     * ---------------------------------------------------------------------------------------------
     */
    private void localizeUser() {
        // retrieve position and show it on map
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Start your GPS Reading progress bar
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait!");
        dialog.show();
    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Stop locating device when activity is on pause for battery saving
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Show map, equakes markers, device marker
     * @param googleMap
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // set equakes markers on map
        earthquakesMarkersList = new ArrayList<Marker>();

        for(Earthquake earthquake : earthquakes) {
            // convert eq position icon to bitmap
            BitmapDescriptor eqMarkerIcon = MyUtil.getBitmapFromVector(context, R.drawable.ic_earthquake_pointer,
                    MyUtil.getMagnitudeColor(earthquake.getMagnitude(),context));

            earthquakesMarkersList.add(mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(earthquake.getLatitude(), earthquake.getLongitude()))
                            .title("Location : " + earthquake.getLocation())
                            .snippet("Magnitude : " + earthquake.getMagnitude())
                            .icon(eqMarkerIcon)
            ));



            Log.d(TAG, "onMapReady: latitude : " + earthquake.getLatitude() + " longitude : " + earthquake.getLongitude());
        }




        // set device location request params
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000); // two minute interval
        mLocationRequest.setFastestInterval(120000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // localize user only if manual localization is not set
        if ( manualLocIsOn == false) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Location Permission already granted
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mGoogleMap.setMyLocationEnabled(true);

                    // set user marker in case of previous coordinates not default
                    checkPrevious();

                    // ask for gps if not enabled
                    showGpsRequestAlert();
                } else {
                    //Request Location Permission
                    checkLocationPermission();

                    // ask for gps if not enabled
                    showGpsRequestAlert();
                }
            } else {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);

                // set user marker in case of previous coordinates not default
                checkPrevious();

                // ask for gps if not enabled
                showGpsRequestAlert();
            }

        // in case on manual localization on
        } else {

            // get previous set position
            lat_s = sharedPreferences.getString(getString(R.string.device_lat),Double.toString(MainActivity.DEFAULT_LAT));
            lng_s = sharedPreferences.getString(getString(R.string.device_lng),Double.toString(MainActivity.DEFAULT_LNG));

            LatLng latLng = new LatLng(Double.parseDouble(lat_s),Double.parseDouble(lng_s));

            // set marker
            myCurrentPositionMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Manual custom position")
                    .snippet("Test"));
            myCurrentPositionMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            // allow to change location
            manualLocalizationAlert();

            // fly to the new location
            animateCameraTo(Double.parseDouble(lat_s), Double.parseDouble(lng_s),3f);
        }

        // zoom on a particular equake if request came from main activity
        zoomOnEquake();


    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Ask for activating gps if not active.
     * NB : use only after localization permissions are granted
     * ---------------------------------------------------------------------------------------------
     */
    public void showGpsRequestAlert() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        LocationManager locationManager;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE );

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("GPS setting!")
                       .setCancelable(false)
                       .setMessage("GPS is not enabled, Do you want to go to settings menu? ")
                       .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.show();
        }
    }



    /*
    public void suggestManualLocalization() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle(getString(R.string.sugges_manual_loc_title))
                    .setCancelable(false)
                    .setMessage(getString(R.string.sugges_manual_loc_text))
                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog.show();
    }
    */


    /**
     * ---------------------------------------------------------------------------------------------
     * Suggest manual localization in case localization permission are not allowed
     * ---------------------------------------------------------------------------------------------
     */
    private void suggestManualLocalization(){
        // check if I can show the dialog
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String allowShow = sharedPreferences.getString(getString(R.string.settings_dontshow_me_again), "false");

        if (allowShow.equals("true") ) {
            return;
        }

        // show dialog
        android.support.v7.app.AlertDialog.Builder  builder =
                new android.support.v7.app.AlertDialog.Builder(MapsActivity.this);
        View view = getLayoutInflater().inflate(R.layout.suggest_manual_loc_dialog, null);
        final CheckBox dontaskagainFlag = (CheckBox) view.findViewById(R.id.dont_ask);

        builder.setTitle(getString(R.string.sugges_manual_loc_title))
                   .setCancelable(false)
                   .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (dontaskagainFlag.isChecked()) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(getString(R.string.settings_dontshow_me_again),
                                        "true" );
                                editor.apply();
                            }
                            dialog.cancel();
                        }
                    });

        builder.setView(view);
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Check if there is already a user location set from previous access to avoid late while
     * the gps is connecting
     * ---------------------------------------------------------------------------------------------
     */
    private void checkPrevious(){
        // init shared preferences
        // SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        lat_s = sharedPreferences.getString(getString(R.string.device_lat),
                Double.toString(MainActivity.DEFAULT_LAT));
        lng_s = sharedPreferences.getString(getString(R.string.device_lng),
                Double.toString(MainActivity.DEFAULT_LNG));

        // if there is already user location different from default location
        if ( (!lat_s.equals(MainActivity.DEFAULT_LAT)) && (!lng_s.equals(MainActivity.DEFAULT_LNG)) ) {
            // position the user location's marker
            userLocationMarker(Double.parseDouble(lat_s), Double.parseDouble(lng_s));
        }

    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Check if there a location manually set
     * ---------------------------------------------------------------------------------------------
     */
    private void checkManualLocation(){

        String manualLocFlag =  sharedPreferences.getString(getString(R.string.manual_Localization_On),
                "false");

        // if there is already user location different from default location
        if (manualLocFlag.equals("true") ) {
            manualLocIsOn = true;
        } else {
            manualLocIsOn = false;
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Zoom on an equake
     * ---------------------------------------------------------------------------------------------
     */
    private void zoomOnEquake() {

        Intent mainIntent = getIntent();
        String flag         = mainIntent.getStringExtra("ShowEquake");
        if (flag != null) {
            double equake_lat_d = Double.parseDouble(mainIntent.getStringExtra("equake_lat"));
            double equake_lng_d = Double.parseDouble(mainIntent.getStringExtra("equake_lng"));
            // LatLng latLng = new LatLng(equake_lat_d, equake_lng_d);
            // mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
            // animateTo(equake_lat_d, equake_lng_d, 5, 5, 30, 300);
            animateCameraTo(equake_lat_d, equake_lng_d, 8f);

        }

    }




    /**
     * ---------------------------------------------------------------------------------------------
     * Set user locations coordinates
     * ---------------------------------------------------------------------------------------------
     */
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;

                //Place device current location marker
                double userLat = location.getLatitude();
                double userLng = location.getLongitude();


                // position the user location's marker
                userLocationMarker(userLat, userLng);


                // save user location's coordinates in shared preferences
                SharedPreferences locationPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = locationPreferences.edit();
                editor.putString(getString(R.string.device_lat), Double.toString(userLat));
                editor.putString(getString(R.string.device_lng), Double.toString(userLng));
                editor.apply();

                // stop progress bar
                dialog.dismiss();

                // zoom on a particular equake if request came from main activity
                zoomOnEquake();

            }
        }


    };




    /**
     * ---------------------------------------------------------------------------------------------
     * Position User location marker
     * @param userLat
     * @param userLng
     * ---------------------------------------------------------------------------------------------
     */
    private void userLocationMarker(double userLat, double userLng) {

        LatLng latLng = new LatLng(userLat, userLng);

        // reset previous
        if (myCurrentPositionMarker != null) {
            myCurrentPositionMarker.remove();
        }

        // convert user position icon to bitmap
        BitmapDescriptor locationMarkerIcon = MyUtil.getBitmapFromVector(context, R.drawable.ic_home_white_24dp,
                ContextCompat.getColor(context, R.color.marker_color));

        myCurrentPositionMarker = mGoogleMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("Your Current Position : lat : " + userLat + " long : " + userLng)
                                    .icon(locationMarkerIcon));

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 1));
    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Animating camera to a target point
     * @param lat
     * @param lon
     * ---------------------------------------------------------------------------------------------
     */
    private void animateCameraTo(double lat, double lon, float z){
        final CameraPosition target =
                new CameraPosition.Builder().target(new LatLng(lat, lon))
                        .zoom(z)
                        .bearing(0)
                        .tilt(25)
                        .build();

        /*
        m_handler = new Handler();
        m_handler.postDelayed(new Runnable(){ // delay to allow load tiles
            @Override
            public void run() {
        */

            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(target),
                    new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    // Toast.makeText(getBaseContext(), "Animation to target complete",
                    // Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancel() {
                    // Toast.makeText(getBaseContext(), "Animation to target canceled",
                    // Toast.LENGTH_SHORT).show();
                }
            });

        // }},500);

    }




    /**
     * ---------------------------------------------------------------------------------------------
     * Ask for user location permissions
     * ---------------------------------------------------------------------------------------------
     */
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {

        if ( manualLocIsOn == false) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    new AlertDialog.Builder(this)
                            .setTitle("Location Permission Needed")
                            .setMessage("This app needs the Location permission, please accept to use location functionality")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(MapsActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            MY_PERMISSIONS_REQUEST_LOCATION);
                                }
                            })
                            .create()
                            .show();


                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                }
            }
        }
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * @param requestCode
     * @param permissions
     * @param grantResults
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);

                        // request gps if is off
                        showGpsRequestAlert();
                    }

                } else {

                    // stop progress bar
                    dialog.dismiss();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Localization permission denied", Toast.LENGTH_LONG).show();

                    // Suggest manual localization
                    suggestManualLocalization();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Choose the map type
     * ---------------------------------------------------------------------------------------------
     */
    private void showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Select Map Type";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = mGoogleMap.getMapType() - 1;

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Locally create a finalised object.

                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 1:
                                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case 3:
                                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            default:
                                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        dialog.dismiss();
                    }
                }
        );

        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Set user costum location manually by long pressing
     * ---------------------------------------------------------------------------------------------
     */
    private void setManualLocalization(){

        //stop location updates, doing manually
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        // set position by long press
        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                /*
                // First check if myMarker is null
                if (myCurrentPositionMarker == null) {
                    // Marker was not set yet. Add marker:
                    myCurrentPositionMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("My positions")
                            .snippet("Your marker snippet"));
                } else {
                    // Marker already exists, just update it's position
                    myCurrentPositionMarker.setPosition(latLng);
                }
                */

                // reset manual position marker
                if (myCurrentPositionMarker != null) {
                    myCurrentPositionMarker.remove();
                }

                myCurrentPositionMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("My positions")
                        .snippet("Your marker snippet"));

                myCurrentPositionMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                // set flag for manual_Localization_On
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.manual_Localization_On), "true");
                editor.apply();


                editor.putString(getString(R.string.device_lat),Double.toString(latLng.latitude));
                editor.apply();

                editor.putString(getString(R.string.device_lng),Double.toString(latLng.longitude));
                editor.apply();

                manualLocIsOn = true;

                // change title
                getSupportActionBar().setTitle(Html.fromHtml("<font color='#66ff66'>"+
                        getString(R.string.title_activity_maps_manual_localization_on) +"</font>"));
            }
        });

    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Unset user manual location
     * ---------------------------------------------------------------------------------------------
     */
    private void unSetManualLocalization(){

        // reset manual position marker
        if (myCurrentPositionMarker != null) {
            myCurrentPositionMarker.remove();
        }

        // set flag for manual_Localization_On off
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.manual_Localization_On), "false");
        editor.apply();

        manualLocIsOn = false;

        /*
        // try to set user localization by gps
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Start your GPS Reading progress bar
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait!");
        dialog.show();
        */

        // change title
        getSupportActionBar().setTitle("");

        // restart activity
        MyUtil.restartActivity(MapsActivity.this);

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Show alert with instruction for manual localization
     * ---------------------------------------------------------------------------------------------
     */
    private void manualLocalizationAlert(){
        new AlertDialog.Builder(this)
                .setTitle("Manual Location setting active (Not using gps)")
                .setMessage("To set/override your position manually, long press on a map point. ")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setManualLocalization();
                    }
                })
                .create()
                .show();

    }





    // ---------------------------------------------------------------------------------------------
    //                                          MENU STUFF
    // ---------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_action, menu);
        locCheckbox = menu.findItem(R.id.overridePosition_cb);

        // in case of previous manual loc setting, check and open menu
        if ( manualLocIsOn == true) {
            // set checkbox manual loc set checked
            locCheckbox.setChecked(true);

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.overridePosition_cb:
                if (item.isChecked()) {
                    item.setChecked(false);
                    unSetManualLocalization();
                    Log.d(TAG, "onOptionsItemSelected: was unChecked !!");

                } else {
                    item.setChecked(true);
                    manualLocalizationAlert();
                }
                return true;

            case R.id.showMarkers_cb:
                if (item.isChecked()) {
                    item.setChecked(false);
                    for (Marker earthquakesMarker : earthquakesMarkersList) {
                        earthquakesMarker.setVisible(true);
                    }
                } else {
                    item.setChecked(true);
                    for (Marker earthquakesMarker : earthquakesMarkersList) {
                        earthquakesMarker.setVisible(false);
                    }
                }
                return true;

            case R.id.chooseMapType_d:
                showMapTypeSelectorDialog();
                return true;


            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }









}
