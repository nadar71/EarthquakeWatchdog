package com.indiewalk.watchdog.earthquake.core.data.local

import com.google.android.gms.maps.model.LatLng

object Constants {
    // API
    // URL to query the USGS dataset for earthquake information
    const val USGS_REQUEST_URL    = "https://earthquake.usgs.gov/fdsnws/event/1/query"

    // MAPS
    // day limit for accessing map
    const val DAYS_LIMIT = 1
    // this is the default position: google at mountain view
    const val DEFAULT_LAT           = 37.4219999
    const val DEFAULT_LNG           = -122.0862515
    const val DEFAULT_CITY          = "Mountain View"
    const val DEFAULT_COUNTRY_CODE  = "US"
    const val DEFAULT_ADDRESS       = "1600 Amphitheatre Parkway, Mountain View, CA 94043"
    const val DEFAULT_LAST_UPDATE   = ""
    val DEFAULT_POSITION = LatLng(DEFAULT_LAT, DEFAULT_LNG)

    // UNIT MEASURE
    const val KM_TO_MILES  = 0.621371         // miles = km * 0.621371
    const val MILES_TO_KM  = 1.60934          // km = miles * 1.60934
}