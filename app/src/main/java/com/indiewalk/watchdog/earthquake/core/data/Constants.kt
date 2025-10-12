package com.indiewalk.watchdog.earthquake.core.data

object Constants {
    // API
    // URL to query the USGS dataset for earthquake information
    const val USGS_REQUEST_URL    = "https://earthquake.usgs.gov/fdsnws/event/1/query"

    // MAPS
    // day limit for accessing map
    const val DAYS_LIMIT = 1
    // this is the default position: google at mountain view
    const val DEFAULT_LAT         = 37.4219999
    const val DEFAULT_LNG         = -122.0862515
    const val DEFAULT_ADDRESS     = "Mountain View,CA"
    const val DEFAULT_LAST_UPDATE = ""

    // UNIT MEASURE
    const val KM_TO_MILES  = 0.621371         // miles = km * 0.621371
    const val MILES_TO_KM  = 1.60934          // km = miles * 1.60934
}