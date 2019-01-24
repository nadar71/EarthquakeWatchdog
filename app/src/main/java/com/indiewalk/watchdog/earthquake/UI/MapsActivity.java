package com.indiewalk.watchdog.earthquake.UI;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.data.Earthquake;
import com.indiewalk.watchdog.earthquake.data.EarthquakeDatabase;
import com.indiewalk.watchdog.earthquake.util.AppExecutors;


import android.app.AlertDialog;

import java.util.ArrayList;
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
    FusedLocationProviderClient mFusedLocationClient;

    // Marker for my current position by gps
    Marker              myCurrentPositionMarker;

    // List of all the earthquake in db
    // TODO: retrieve them more efficiently with livedata & c.
    List<Earthquake> earthquakes;

    // Markers associated with earthquake on map
    List<Marker> earthquakesMarkersList;

    // Db reference
    // TODO : create and interface with repository
    EarthquakeDatabase eqDb;



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

        // get db instance
        eqDb = EarthquakeDatabase.getDbInstance(getApplicationContext());

        // retrieve eq currently in db, in different thread
        // TODO: using livedata ?!?
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                earthquakes = eqDb.earthquakeDbDao().loadAllEarthquakeRetrieved();
            }
        });

        getSupportActionBar().setTitle("Map Location Activity");

        // retrieve position and show it on map
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

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
            earthquakesMarkersList.add(mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(earthquake.getLatitude(), earthquake.getLongitude()))
                            .title("Location : " + earthquake.getLocation())
                            .snippet("Magnitude : " + earthquake.getMagnitude())
            ));

            Log.d(TAG, "onMapReady: latitude : " + earthquake.getLatitude() + " logitude : " + earthquake.getLongitude());
        }


        // set device location request params
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000); // two minute interval
        mLocationRequest.setFastestInterval(120000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }
    }


    /**
     * ---------------------------------------------------------------------------------------------
     *
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
                if (myCurrentPositionMarker != null) {
                    myCurrentPositionMarker.remove();
                }

                //Place device current location marker
                double userLat = location.getLatitude();
                double userLng = location.getLongitude();
                LatLng latLng = new LatLng(userLat, userLng);

                // convert user position icon to bitmap
                BitmapDescriptor locationMarkerIcon = getBitmapFromVector(context, R.drawable.ic_home_blue_24dp,
                        ContextCompat.getColor(context, R.color.marker_color));

                myCurrentPositionMarker = mGoogleMap.addMarker(new MarkerOptions()
                                            .position(latLng)
                                            .title("Your Current Position : lat : " + userLat + " long : " + userLng)
                                            .icon(locationMarkerIcon));

                //move map camera
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 1));


                // save coordinates in shared preferences
                SharedPreferences locationPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = locationPreferences.edit();
                editor.putString(getString(R.string.device_lat), Double.toString(userLat));
                editor.putString(getString(R.string.device_lng), Double.toString(userLng));
                editor.apply();

            }
        }
    };




    /**
     * ---------------------------------------------------------------------------------------------
     * Converting vector icon to bitmap one, to get used as marker icon (allow bitmap only)
     * @param context
     * @param vectorResourceId
     * @param tintColor
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public static BitmapDescriptor getBitmapFromVector(@NonNull Context context,
                                                       @DrawableRes int vectorResourceId,
                                                       @ColorInt int tintColor) {

        Drawable vectorDrawable = ResourcesCompat.getDrawable(
                context.getResources(), vectorResourceId, null);

        if (vectorDrawable == null) {
            Log.e(TAG, "Requested vector resource was not found");
            return BitmapDescriptorFactory.defaultMarker();
        }

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, tintColor);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }




    /**
     * ---------------------------------------------------------------------------------------------
     *
     * ---------------------------------------------------------------------------------------------
     */
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
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
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
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
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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
                    // TODOo : SHOW DIALOG FRAGMENT WITH ISTRUCTION
                } else {
                    item.setChecked(true);
                    Log.d(TAG, "onOptionsItemSelected: was unchecked !!");
                    // TODOo : GO TO CURRENT POSITION

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
