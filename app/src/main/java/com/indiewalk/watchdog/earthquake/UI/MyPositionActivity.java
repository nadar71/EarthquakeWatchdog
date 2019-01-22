package com.indiewalk.watchdog.earthquake.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.indiewalk.watchdog.earthquake.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import com.indiewalk.watchdog.earthquake.data.Earthquake;
import com.indiewalk.watchdog.earthquake.data.EarthquakeDatabase;
import com.indiewalk.watchdog.earthquake.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;

/**
 * -------------------------------------------------------------------------------------------------
 * Activity which show earthquakes positions, as well user's one.
 * -------------------------------------------------------------------------------------------------
 */
public class MyPositionActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private static final String TAG = MyPositionActivity.class.getSimpleName();

    // Map references
    private   GoogleMap mMap = null;
    protected GoogleApiClient mGoogleApiClient;
    // My last location
    Location mLastLocation;

    // List of all the earthquake in db
    // TODO: retrieve them more efficiently with livedata & c.
    List<Earthquake> earthquakes;

    // Markers associated with earthquake on map
    List<Marker> earthquakesMarkersList;

    // Db reference
    // TODO : create and interface with repository
    EarthquakeDatabase eqDb;

    // Marker for my position defined manually : only must exist at a time
    Marker myManualPositionMarker;

    // Marker for my current position by gps
    Marker myCurrentPositionMarker;

    // vars for set user current location
    private static final String STATE_IN_PERMISSION = "inPermission";
    private static final int REQUEST_PERMS          = 1337;   // random number
    private boolean isInPermission                  = false;
    private LocationManager locMgr                  = null;
    private boolean needsInit                       = false;
    private Criteria crit                           = new Criteria();

    // store current user location coordinates
    Location myLocation;
    double   myLat, myLong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get db instance
        eqDb = EarthquakeDatabase.getDbInstance(getApplicationContext());

        // avoid to request permission again on config change
        if (savedInstanceState != null) {
            isInPermission = savedInstanceState.getBoolean(STATE_IN_PERMISSION, false);
        }

        // showmapwith permission asking
        setupLayoutMap(canGetLocation());

        // retrieve eq currently in db, in different thread
        // TODO: using livedata ?!?
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                earthquakes = eqDb.earthquakeDbDao().loadAllEarthquakeRetrieved();
            }
        });

    }


    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Save in Bundle boolean flag result about requesting location permission
     * @param outState
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IN_PERMISSION, isInPermission);
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Callback on request permission result got
     * @param requestCode
     * @param permissions
     * @param grantResults
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        isInPermission = false;

        if (requestCode == REQUEST_PERMS) {
            if (canGetLocation()) {
                setupLayoutMap(true);
            }
            else {
                finish(); // denied permission, so we're done // TODO : change in a dialog about doing it manually
            }
        }
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Show map; request permissions if needed
     * @param canGetLocation
     * ---------------------------------------------------------------------------------------------
     */
    private void setupLayoutMap(boolean canGetLocation) {
        if (canGetLocation) {
                setContentView(R.layout.activity_my_position);

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

                mapFragment.getMapAsync(this);

        }
        else if (!isInPermission) {
            isInPermission=true;
            // request the permissions
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMS);
        }
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Connection with location service
     * ---------------------------------------------------------------------------------------------
     */
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Check if permissions are already granted
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    private boolean canGetLocation() {
        return(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED);
    }




    /**
     * ---------------------------------------------------------------------------------------------
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     * ---------------------------------------------------------------------------------------------
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.mMap = googleMap;

        // set user locations
        mMap.setMyLocationEnabled(true);
        locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        // TODO : SAVE IT IN SHAREDPREFERENCES

        // set equakes markers on map
        earthquakesMarkersList = new ArrayList<Marker>();

        for(Earthquake earthquake : earthquakes){
            earthquakesMarkersList.add(mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(earthquake.getLatitude(), earthquake.getLongitude()))
                            .title("Location : " + earthquake.getLocation())
                            .snippet("Magnitude : " + earthquake.getMagnitude())
                    // .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home_blue_24dp)))
            ));

            Log.d(TAG, "onMapReady: latitude : "+earthquake.getLatitude()+" logitude : "+ earthquake.getLongitude());
        }

        // set a connection with google maps api
        buildGoogleApiClient();


        /*
        // get my current location coordinates
        myLocation = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        // TODO : MAKE THIS WORKS!
        if (myLocation != null) {
            myLat  = myLocation.getLatitude();
            myLong = myLocation.getLongitude();

            // set marker at my current position
            myCurrentPositionMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(myLat, myLong))
                .title("My Current GPS position")
                .snippet("Snippet")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home_blue_24dp)));
            Toast.makeText(this, "Marker set on :" + myLat + " " + myLong, Toast.LENGTH_SHORT).show();
        }
        */

        // TODO : SAVE IT IN SHAREDPREFERENCES
        // set position by long press
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // First check if myMarker is null
                if (myManualPositionMarker == null) {
                    // Marker was not set yet. Add marker:
                    myManualPositionMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("My positions")
                            .snippet("Your marker snippet"));
                } else {
                    // Marker already exists, just update it's position
                    myManualPositionMarker.setPosition(latLng);
                }

                myManualPositionMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }
        });


    }



    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        Log.d(TAG, "onConnected: Connection with map service DONE");
        if (mLastLocation != null) {
            myLat  = mLastLocation.getLatitude();
            myLong = mLastLocation.getLongitude();
            Log.d(TAG, "onConnected: mycoord, lat : "+myLat+" long : "+myLong);

            LatLng loc = new LatLng(myLat, myLong);
            mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .title("New Marker")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home_blue_24dp)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        }

    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }


    // ---------------------------------------------------------------------------------------------
    //                                          MENU STUFF
    // ---------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.overridePosition_cb:
                if (item.isChecked()) {
                    item.setChecked(false);
                    Log.d(TAG, "onOptionsItemSelected: was Checked !!");
                    // TODO : SHOW DILOG FRAGMENT WITH ISTRUCTION
                } else {
                    item.setChecked(true);
                    Log.d(TAG, "onOptionsItemSelected: was unchecked !!");
                    // TODO : GO TO CURRENT POSITION

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

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }



}
