package com.indiewalk.watchdog.earthquake.core.domain.repository

import android.location.Address
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo

interface LocationRepository {
    suspend fun getLastKnownLatLng(): LatLng?
    fun getAddress(latLng: LatLng): Address?
    fun getLocationInfo(latLng: LatLng): LocationInfo
    fun getLocationInfo(address: Address?): LocationInfo
}

