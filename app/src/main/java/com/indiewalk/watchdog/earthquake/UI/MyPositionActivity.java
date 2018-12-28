package com.indiewalk.watchdog.earthquake.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

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

public class MyPositionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MyPositionActivity.class.getSimpleName();

    // GoogleMap ref
    private GoogleMap mMap = null;

    // List of all the earthquake in db
    // TODO: retrieve them more efficiently with livedata & c.
    List<Earthquake> earthquakes;

    // Markers associated with earthquake on map
    List<Marker> earthquakesMarkers;

    // Db reference
    EarthquakeDatabase eqDb;

    // Marker for my position defined manually : only must exist at a time
    Marker myManualPositionMarker;

    // Marker for my current position by gps
    Marker myCurrentPositionMarker;

    // vars for set user current location
    private static final String STATE_IN_PERMISSION = "inPermission";
    private static final int REQUEST_PERMS          = 1337;
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


    /**
     * Save in Bundle boolean flag result about requesting location permission
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IN_PERMISSION, isInPermission);
    }


    /**
     * Callback on request permission result got
     * @param requestCode
     * @param permissions
     * @param grantResults
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
     * Show map and current user position, if any
     * @param canGetLocation
     */
    private void setupLayoutMap(boolean canGetLocation) {
        if (canGetLocation) {
            // if (readyToGo()) {
                setContentView(R.layout.activity_my_position);

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            // }
        }
        else if (!isInPermission) {
            isInPermission=true;

            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMS);
        }
    }


    /**
     * Set the permissions
     * @return
     */
    private boolean canGetLocation() {
        return(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED);
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.mMap = googleMap;


        /*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */


        // set user locations
        mMap.setMyLocationEnabled(true);
        locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        crit.setAccuracy(Criteria.ACCURACY_FINE);


        // set the current eq coordinates, based on temporal filter
        for(Earthquake earthquake : earthquakes){
            /*
            earthquakesMarkers.add(
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(earthquake.getLatitude(), earthquake.getLongitude()))
                            .title("Location : " + earthquake.getLocation())
                            .snippet("Magnitude : " + earthquake.getMagnitude())
                            // .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home_blue_24dp)))

                    ));

            */
            Log.d(TAG, "onMapReady: latitude : "+earthquake.getLatitude()+" logitude : "+ earthquake.getLongitude());
        }





        // get my current location coordinates
        myLocation = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (myLocation != null) {
            myLat = myLocation.getLatitude();
            myLong = myLocation.getLongitude();

            // set marker at my current position
            myCurrentPositionMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(myLat, myLong))
                .title("My Current GPS position")
                .snippet("Snippet")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home_blue_24dp)));
            Toast.makeText(this, "Marker set on :" + myLat + " " + myLong, Toast.LENGTH_SHORT).show();

        }


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
}
