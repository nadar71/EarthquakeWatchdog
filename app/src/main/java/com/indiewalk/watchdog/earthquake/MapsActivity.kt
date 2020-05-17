package com.indiewalk.watchdog.earthquake

import android.Manifest
import android.app.ProgressDialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.ads.AdView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.indiewalk.watchdog.earthquake.UI.MainActivityEarthquakesList
import com.indiewalk.watchdog.earthquake.UI.MainViewModel
import com.indiewalk.watchdog.earthquake.UI.MainViewModelFactory
import com.indiewalk.watchdog.earthquake.data.Earthquake
import com.indiewalk.watchdog.earthquake.data.EarthquakeRepository
import com.indiewalk.watchdog.earthquake.util.ConsentSDK
import com.indiewalk.watchdog.earthquake.util.MyUtil
import kotlinx.android.synthetic.main.main_activity_earthquakes_list.*



import android.app.AlertDialog

import java.io.IOException
import java.util.ArrayList
import java.util.Locale


/**
 * -------------------------------------------------------------------------------------------------
 * Show earthquakes positions, as well user's one.
 * -------------------------------------------------------------------------------------------------
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    internal var context: Context = this@MapsActivity

    // Map references
    internal lateinit var mGoogleMap: GoogleMap

    // Map frag
    internal lateinit var mapFrag: SupportMapFragment

    // Device location req references
    internal lateinit var mLocationRequest: LocationRequest
    internal lateinit var mLastLocation: Location
    internal var mFusedLocationClient: FusedLocationProviderClient? = null

    // Marker for my current position by gps
    internal var myCurrentPositionMarker: Marker? = null

    // Markers associated with earthquake on map
    internal lateinit var earthquakesMarkersList: MutableList<Marker>

    // eqs list with livedata
    internal lateinit var equakes: LiveData<List<Earthquake>>

    // eqs list WITHOUT livedata
    internal var equakes_no_live: List<Earthquake>? = null

    internal var eqRepository: EarthquakeRepository? = null

    // searching location progress dialog
    internal var dialog: ProgressDialog? = null

    // SharedPrefences ref actvity global
    internal lateinit var sharedPreferences: SharedPreferences

    // Manual Localization flag
    internal var manualLocIsOn = false

    // Manual localization menu item ref
    internal lateinit var locCheckbox: MenuItem

    // User location coords
    internal var lat_s: String? = null
    internal var lng_s: String? = null


    /**
     * ---------------------------------------------------------------------------------------------
     * Set user locations coordinates in case of MANUAL LOCALIZATION OFF
     * ---------------------------------------------------------------------------------------------
     */
    internal var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            val locationList = locationResult.locations
            if (locationList.size > 0) {
                //The last location in the list is the newest
                val location = locationList[locationList.size - 1]
                Log.i("MapsActivity", "Location: " + location.latitude + " " + location.longitude)
                mLastLocation = location

                //Place device current location marker
                val userLat = location.latitude
                val userLng = location.longitude


                // position the user location's marker
                userLocationMarker(userLat, userLng)


                // save user location's coordinates in shared preferences
                val locationPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val editor = locationPreferences.edit()
                editor.putString(getString(R.string.device_lat), java.lang.Double.toString(userLat))
                editor.putString(getString(R.string.device_lng), java.lang.Double.toString(userLng))
                editor.apply()

                // set address location in pref
                setLocationAddress(userLat, userLng)

                // update equakes list with new distance from user
                // MyUtil.setEqDistanceFromCurrentCoords(equakes_no_live, context);

                // update eqs in db
                eqRepository!!.updatedAllEqsDistFromUser(equakes_no_live, context)

                // stop progress bar
                dialog?.let{it.dismiss()}

                // zoom on a particular equake if request came from main activity
                zoomOnEquake()

            }
        }


    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Load equakes list, set up ma and position
     * @param savedInstanceState
     * ---------------------------------------------------------------------------------------------
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)



        // init shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // back btn
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Show on map
        mapFrag = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFrag.getMapAsync(this)

        // check if there was a previous user location manual setted
        checkManualLocation()

        if (manualLocIsOn == false) {
            // standard title
            supportActionBar!!.title = getString(R.string.title_activity_maps_standard)

            // localize user
            localizeUser()

        } else {
            // manual loc on title
            supportActionBar!!.title = Html.fromHtml("<font color='#66ff66'>" +
                    getString(R.string.title_activity_maps_manual_localization_on) + "</font>")
        }

        // get repo. For issue #96,97 do not use getRepository()
        eqRepository = (SingletonProvider.getsContext() as SingletonProvider)
                .repositoryWithDataSource

    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Stop locating device when activity is on pause for battery saving
     * ---------------------------------------------------------------------------------------------
     */
    public override fun onPause() {
        super.onPause()

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }

        // avoid leaked window problem
        dialog?.let{it.dismiss()}
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Launch User localization
     * ---------------------------------------------------------------------------------------------
     */
    private fun localizeUser() {
        // retrieve position and show it on map
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Start your GPS Reading progress bar
        dialog = ProgressDialog(this)
        dialog?.let{it.setMessage("Please wait!"); it.show()}
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Callback when map is available.
     * Show map, equakes markers, device marker.
     * When {@localizeUser()} ended
     * @param googleMap
     * ---------------------------------------------------------------------------------------------
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        // set equakes markers on map
        earthquakesMarkersList = ArrayList()

        // Get eq list through LiveData
        val factory = MainViewModelFactory(MainActivityEarthquakesList.LOAD_ALL_NO_ORDER)
        val viewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)

        equakes = viewModel.eqList!!
        equakes.observe(this, Observer { earthquakeList ->
            equakes_no_live = earthquakeList // update for use outside
            setMarkerForEachEq(earthquakeList)
        })


        // set device location request params
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 120000 // two minute interval
        mLocationRequest.fastestInterval = 120000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        // -1- localize user only if MANUAL LOCALIZATION OFF
        if (manualLocIsOn == false) {
            // support for os version newer and older
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Location Permission already granted
                    mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                    mGoogleMap.isMyLocationEnabled = true

                    // set user marker in case of previous coordinates not default
                    checkPrevious()

                    // ask for gps if not enabled
                    showGpsRequestAlert()
                } else {
                    //Request Location Permission
                    checkLocationPermission()

                    // ask for gps if not enabled
                    showGpsRequestAlert()
                }
            } else {
                mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                mGoogleMap.isMyLocationEnabled = true


                // set user marker in case of previous coordinates not default
                checkPrevious()

                // ask for gps if not enabled
                showGpsRequestAlert()
            }

            // -2- in case on MANUAL LOCALIZATION ON
        } else {
            // get previous set position
            lat_s = sharedPreferences.getString(getString(R.string.device_lat), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LAT))
            lng_s = sharedPreferences.getString(getString(R.string.device_lng), java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LNG))

            val latLng = LatLng(java.lang.Double.parseDouble(lat_s), java.lang.Double.parseDouble(lng_s))

            // set marker
            myCurrentPositionMarker = mGoogleMap.addMarker(MarkerOptions()
                    .position(latLng)
                    .title("Your Manual position")
                    .snippet("Latitude : $lat_s\nLongitude : $lng_s"))
            myCurrentPositionMarker!!.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

            // allow to change location
            manualLocalizationAlert()

            // fly to the new location
            animateCameraTo(java.lang.Double.parseDouble(lat_s), java.lang.Double.parseDouble(lng_s), 3f)
        }

        // zoom on a particular equake if request came from main activity
        zoomOnEquake()

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Set marker and details on click for each eq on map
     * @param earthquakeList
     * ---------------------------------------------------------------------------------------------
     */
    private fun setMarkerForEachEq(earthquakeList: List<Earthquake>?) {
        val minMagnitude = java.lang.Double.parseDouble(sharedPreferences.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default)))

        for (earthquake in earthquakeList!!) {

            if (earthquake.getMagnitude()!! < minMagnitude) continue

            // convert eq position icon to bitmap
            val eqMarkerIcon = MyUtil.getBitmapFromVector(context,
                    R.drawable.ic_earthquake_pointer,
                    MyUtil.getMagnitudeColor(earthquake.getMagnitude()!!, context))

            earthquakesMarkersList.add(mGoogleMap.addMarker(MarkerOptions()
                    .position(LatLng(earthquake.latitude, earthquake.longitude))
                    .title("Location : " + earthquake.location!!)
                    .snippet("Magnitude : " + earthquake.getMagnitude()!!)
                    .icon(eqMarkerIcon)
            ))

            mGoogleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

                override fun getInfoWindow(arg0: Marker): View? {
                    return null
                }

                override fun getInfoContents(marker: Marker): View {

                    val info = LinearLayout(context)
                    info.orientation = LinearLayout.VERTICAL

                    val title = TextView(context)
                    title.setTextColor(Color.BLACK)
                    title.gravity = Gravity.CENTER
                    title.setTypeface(null, Typeface.BOLD)
                    title.text = marker.title

                    val snippet = TextView(context)
                    snippet.setTextColor(Color.GRAY)
                    snippet.text = marker.snippet

                    info.addView(title)
                    info.addView(snippet)

                    return info
                }
            })


            Log.d(TAG, "onMapReady: latitude : " + earthquake.latitude + " longitude : " + earthquake.longitude)
        }
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Ask for activating gps if not active.
     * NB : use only after localization permissions are granted
     * ---------------------------------------------------------------------------------------------
     */
    fun showGpsRequestAlert() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return

        val locationManager: LocationManager
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("GPS setting!")
                    .setCancelable(false)
                    .setMessage("GPS is not enabled, Do you want to go to settings menu? ")
                    .setPositiveButton("Setting") { dialog, which ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    }
            alertDialog.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
            alertDialog.show()
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
    private fun suggestManualLocalization() {
        // check if I can show the dialog
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val allowShow = sharedPreferences.getString(getString(R.string.settings_dontshow_me_again), "false")

        if (allowShow == "true") {
            return
        }

        // show dialog
        val builder = android.support.v7.app.AlertDialog.Builder(this@MapsActivity)
        val view = layoutInflater.inflate(R.layout.suggest_manual_loc_dialog, null)
        val dontaskagainFlag = view.findViewById<View>(R.id.dont_ask) as CheckBox

        builder.setTitle(getString(R.string.sugges_manual_loc_title))
                .setCancelable(false)
                .setPositiveButton("Cancel") { dialog, which ->
                    if (dontaskagainFlag.isChecked) {
                        val editor = sharedPreferences.edit()
                        editor.putString(getString(R.string.settings_dontshow_me_again),
                                "true")
                        editor.apply()
                    }
                    dialog.cancel()
                }

        builder.setView(view)
        val dialog = builder.create()
        dialog.show()

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Check if there is already a user location set from previous access to avoid delay while
     * the gps is connecting
     * ---------------------------------------------------------------------------------------------
     */
    private fun checkPrevious() {
        // init shared preferences
        // SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        lat_s = sharedPreferences.getString(getString(R.string.device_lat),
                java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LAT))
        lng_s = sharedPreferences.getString(getString(R.string.device_lng),
                java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LNG))

        // if there is already user location different from default location
        if (lat_s != java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LAT) && lng_s != java.lang.Double.toString(MainActivityEarthquakesList.DEFAULT_LNG)) {
            // position the user location's marker
            userLocationMarker(java.lang.Double.parseDouble(lat_s), java.lang.Double.parseDouble(lng_s))
        }

        // save in preferences
        setLocationAddress(java.lang.Double.parseDouble(lat_s), java.lang.Double.parseDouble(lng_s))

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Check if there is a location manually set
     * ---------------------------------------------------------------------------------------------
     */
    private fun checkManualLocation() {

        val manualLocFlag = sharedPreferences.getString(getString(R.string.manual_Localization_On),
                "false")

        // if there is already user location different from default location
        if (manualLocFlag == "true") {
            manualLocIsOn = true
        } else {
            manualLocIsOn = false
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Zoom on an equake
     * ---------------------------------------------------------------------------------------------
     */
    private fun zoomOnEquake() {

        val mainIntent = intent
        val flag = mainIntent.getStringExtra("ShowEquake")
        if (flag != null) {
            val equake_lat_d = java.lang.Double.parseDouble(mainIntent.getStringExtra("equake_lat"))
            val equake_lng_d = java.lang.Double.parseDouble(mainIntent.getStringExtra("equake_lng"))
            // LatLng latLng = new LatLng(equake_lat_d, equake_lng_d);
            // mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
            // animateTo(equake_lat_d, equake_lng_d, 5, 5, 30, 300);
            animateCameraTo(equake_lat_d, equake_lng_d, 8f)

        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Recover and save location address in preferences
     * @param userLat
     * @param userLng
     * ---------------------------------------------------------------------------------------------
     */
    private fun setLocationAddress(userLat: Double, userLng: Double) {
        //Set Address
        val address: String
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(userLat, userLng, 1)
            if (addresses != null && addresses.size > 0) {

                // String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                val city = addresses[0].locality
                // String state = addresses.get(0).getAdminArea();
                // String country = addresses.get(0).getCountryName();
                val postalCode = addresses[0].postalCode
                val knownName = addresses[0].featureName // Only if available else return NULL

                address = "$city $postalCode $knownName"

                // Log.d(TAG, "getAddress:  address" + address);
                Log.d(TAG, "getAddress:  city : $city")
                // Log.d(TAG, "getAddress:  state" + state);
                Log.d(TAG, "getAddress:  postalCode : $postalCode")
                Log.d(TAG, "getAddress:  knownName : $knownName")

                Log.d(TAG, "Address : $address")

                val locationPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val editor = locationPreferences.edit()
                editor.putString(getString(R.string.location_address), address)
                editor.apply()

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }


    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Position User location marker
     * @param userLat
     * @param userLng
     * ---------------------------------------------------------------------------------------------
     */
    private fun userLocationMarker(userLat: Double, userLng: Double) {

        val latLng = LatLng(userLat, userLng)

        // reset previous
        if (myCurrentPositionMarker != null) {
            myCurrentPositionMarker!!.remove()
        }

        // convert user position icon to bitmap
        val locationMarkerIcon = MyUtil.getBitmapFromVector(context, R.drawable.ic_home_white_24dp,
                ContextCompat.getColor(context, R.color.marker_color))

        myCurrentPositionMarker = mGoogleMap.addMarker(MarkerOptions()
                .position(latLng)
                .title("Your Current Position : latitude : $userLat longitude : $userLng")
                .icon(locationMarkerIcon))

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 1f))
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Animating camera to a target point
     * @param lat
     * @param lon
     * ---------------------------------------------------------------------------------------------
     */
    private fun animateCameraTo(lat: Double, lon: Double, z: Float) {
        val target = CameraPosition.Builder().target(LatLng(lat, lon))
                .zoom(z)
                .bearing(0f)
                .tilt(25f)
                .build()

        /*
        m_handler = new Handler();
        m_handler.postDelayed(new Runnable(){ // delay to allow load tiles
            @Override
            public void run() {
        */

        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(target),
                object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        // Toast.makeText(getBaseContext(), "Animation to target complete",
                        // Toast.LENGTH_SHORT).show();
                    }

                    override fun onCancel() {
                        // Toast.makeText(getBaseContext(), "Animation to target canceled",
                        // Toast.LENGTH_SHORT).show();
                    }
                })

        // }},500);

    }

    private fun checkLocationPermission() {

        if (manualLocIsOn == false) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    AlertDialog.Builder(this)
                            .setTitle("Location Permission Needed")
                            .setMessage("This app needs the Location permission, please accept to use location functionality")
                            .setPositiveButton("OK") { dialogInterface, i ->
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(this@MapsActivity,
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                        MY_PERMISSIONS_REQUEST_LOCATION)
                            }
                            .create()
                            .show()


                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION)
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
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                        mGoogleMap.isMyLocationEnabled = true

                        // request gps if is off
                        showGpsRequestAlert()
                    }

                } else {

                    // stop progress bar
                    dialog?.let{it.dismiss()}

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Localization permission denied", Toast.LENGTH_LONG).show()

                    // Suggest manual localization
                    suggestManualLocalization()
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Choose the map type
     * ---------------------------------------------------------------------------------------------
     */
    private fun showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        val fDialogTitle = "Select Map Type"
        val builder = AlertDialog.Builder(this)
        builder.setTitle(fDialogTitle)

        // Find the current map type to pre-check the item representing the current state.
        val checkItem = mGoogleMap.mapType - 1

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem
        ) { dialog, item ->
            // Locally create a finalised object.

            // Perform an action depending on which item was selected.
            when (item) {
                1 -> mGoogleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                2 -> mGoogleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                3 -> mGoogleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                else -> mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            dialog?.let{it.dismiss()}
        }

        // Build the dialog and show it.
        val fMapTypeDialog = builder.create()
        fMapTypeDialog.setCanceledOnTouchOutside(true)
        fMapTypeDialog.show()
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Set user costum location manually by long pressing
     * ---------------------------------------------------------------------------------------------
     */
    private fun setManualLocalization() {

        //stop location updates, doing manually
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }

        // set position by long press
        mGoogleMap.setOnMapLongClickListener { latLng ->
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
                myCurrentPositionMarker!!.remove()
            }

            myCurrentPositionMarker = mGoogleMap.addMarker(MarkerOptions()
                    .position(latLng)
                    .title("Your Manual position")
                    .snippet("Latitude : " + latLng.latitude + "\n" + "Longitude : " + latLng.longitude))



            myCurrentPositionMarker!!.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

            // set flag for manual_Localization_On
            val editor = sharedPreferences.edit()
            editor.putString(getString(R.string.manual_Localization_On), "true")
            editor.apply()


            editor.putString(getString(R.string.device_lat), java.lang.Double.toString(latLng.latitude))
            editor.apply()

            editor.putString(getString(R.string.device_lng), java.lang.Double.toString(latLng.longitude))
            editor.apply()


            // save in preferences
            setLocationAddress(latLng.latitude, latLng.longitude)

            // update equakes list with new distance from user
            // MyUtil.setEqDistanceFromCurrentCoords(equakes_no_live, context);

            // update eqs in db
            eqRepository!!.updatedAllEqsDistFromUser(equakes_no_live, context)

            manualLocIsOn = true

            // change title
            supportActionBar!!.title = Html.fromHtml("<font color='#66ff66'>" +
                    getString(R.string.title_activity_maps_manual_localization_on) + "</font>")
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Unset user manual location
     * ---------------------------------------------------------------------------------------------
     */
    private fun unSetManualLocalization() {

        // reset manual position marker
        if (myCurrentPositionMarker != null) {
            myCurrentPositionMarker!!.remove()
        }

        // set flag for manual_Localization_On off
        val editor = sharedPreferences.edit()
        editor.putString(getString(R.string.manual_Localization_On), "false")
        editor.apply()

        manualLocIsOn = false

        /*
        // try to set user localization by gps
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Start your GPS Reading progress bar
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait!");
        dialog.show();
        */

        // change title
        supportActionBar!!.title = ""

        // restart activity
        MyUtil.restartActivity(this@MapsActivity)

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Show alert with instruction for manual localization
     * ---------------------------------------------------------------------------------------------
     */
    private fun manualLocalizationAlert() {
        AlertDialog.Builder(this)
                .setTitle(this.resources.getString(R.string.dialog_manual_loc_title))
                .setMessage(this.resources.getString(R.string.dialog_manual_loc_msg))
                .setPositiveButton("OK") { dialogInterface, i -> setManualLocalization() }
                .create()
                .show()

    }


    // ---------------------------------------------------------------------------------------------
    //                                          MENU STUFF
    // ---------------------------------------------------------------------------------------------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.map_action, menu)
        locCheckbox = menu.findItem(R.id.overridePosition_cb)

        // in case of previous manual loc setting, check and open menu
        if (manualLocIsOn == true) {
            // set checkbox manual loc set checked
            locCheckbox.isChecked = true

        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.overridePosition_cb -> {
                if (item.isChecked) {
                    item.isChecked = false
                    unSetManualLocalization()
                    Log.d(TAG, "onOptionsItemSelected: was unChecked !!")

                } else {
                    item.isChecked = true
                    manualLocalizationAlert()
                }
                return true
            }

            R.id.showMarkers_cb -> {
                if (item.isChecked) {
                    item.isChecked = false
                    for (earthquakesMarker in earthquakesMarkersList) {
                        earthquakesMarker.isVisible = true
                    }
                } else {
                    item.isChecked = true
                    for (earthquakesMarker in earthquakesMarkersList) {
                        earthquakesMarker.isVisible = false
                    }
                }
                return true
            }

            R.id.chooseMapType_d -> {
                showMapTypeSelectorDialog()
                return true
            }


            android.R.id.home -> {
                onBackPressed()
                return true
            }

            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        private val TAG = MapsActivity::class.java.simpleName

        // Maps type list
        private val MAP_TYPE_ITEMS = arrayOf<CharSequence>("Road Map", "Hybrid", "Satellite", "Terrain")


        /**
         * ---------------------------------------------------------------------------------------------
         * Ask for user location permissions
         * ---------------------------------------------------------------------------------------------
         */
        val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }


}
