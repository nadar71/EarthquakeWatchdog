package com.indiewalk.watchdog.earthquake.core.data.repository

import android.content.Context
import android.location.Address
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.domain.repository.LocationRepository
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo
import com.indiewalk.watchdog.earthquake.core.util.extensions.toLocationInfo
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {

    override suspend fun getLastKnownLatLng(): LatLng? = MapsUtils.getLastKnownLatLng(context)

    override fun getAddress(latLng: LatLng): Address? = MapsUtils.getAddress(context, latLng)

    override fun getLocationInfo(latLng: LatLng): LocationInfo =
        getLocationInfo(getAddress(latLng))

    override fun getLocationInfo(address: Address?): LocationInfo = address.toLocationInfo(context)
}
