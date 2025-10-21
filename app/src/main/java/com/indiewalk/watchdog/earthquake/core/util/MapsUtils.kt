package com.indiewalk.watchdog.earthquake.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.provider.Settings
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.model.MappingSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQGeometryDTO
import it.abenergie.customerarea.core.utility.extensions.TAG
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MapsUtils {

    // Convert degree angle in radiant
    private fun fromDegreeToRadiant(deg_angle: Double): Double {
        return deg_angle * Math.PI / 180
    }

    // Returning the distance in Km between 2 points on a sphere throught the Haversine formula
    private fun haversineDistanceKm(lat1: Double, lat2: Double, lng1: Double, lng2: Double): Double {
        val R = 6371.0 // km
        val dLat = fromDegreeToRadiant(lat2 - lat1)
        val dLng = fromDegreeToRadiant(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(fromDegreeToRadiant(lat1)) *
                cos(fromDegreeToRadiant(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }


    /*// Convert km to miles
    fun fromKmToMiles(km: Double): Double {
        return km * KM_TO_MILES
    }

    // Convert from miles to km
    fun fromMilesToKm(miles: Double): Double {
        return miles * MILES_TO_KM
    }*/


    // Update each equakes info with custom distance from user if any, with distance unit preferred.
    fun getEQDistanceFromUser(
        eqCoords: EQGeometryDTO,
        settings: MappingSettings
    ): Double? {
        Log.d(TAG, "getEQDistanceFromUser: eqCoords: $eqCoords")
        val eqLat = eqCoords.latitude ?: return null
        val eqLng = eqCoords.longitude ?: return null

        var dist =  haversineDistanceKm(
            lat1 = settings.userLat,
            lat2 = eqLat,
            lng1 = settings.userLng,
            lng2 = eqLng
        )
        Log.i(TAG, "getEQDistanceFromUser: eq distance from user : $dist in km")

        return dist
    }

    /*fun getEQDistanceFromUser(
        eqCoords: EQGeometryDTO,
        ): Int? {
        Log.d(TAG, "getEQDistanceFromUser: eqCoords: $eqCoords")
        if (eqCoords == null || eqCoords.latitude == null || eqCoords.longitude == null) {
            return null
        } else {
            var userLat = device_lat.toDouble()
            var userLng = device_lng.toDouble()

            var dist = haversineDistanceCalc(
                userLat, eqCoords.latitude as Double,
                userLng, eqCoords.longitude as Double
            )

            *//*if (unitSystem == UnitSystems.IMPERIAL.value) {
                dist = fromKmToMiles(dist.toDouble())
            }

            Log.i(TAG, "getEQDistanceFromUser: eq distance from user : $dist in $unitSystem")*//*
            Log.i(TAG, "getEQDistanceFromUser: eq distance from user : $dist in km")

            return dist.toInt()
        }
    }*/

    fun openAppSettings(context: Context) {
        val uri = Uri.fromParts("package", context.packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLatLng(context: Context): LatLng? {
        return try {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            val loc = fused.lastLocation.await() ?: return null
            LatLng(loc.latitude, loc.longitude)
        } catch (_: Exception) {
            null
        }
    }

    // Performs reverse geocoding to get a human-readable address from LatLng coordinates.

    fun getAddressFromLatLng(context: Context, latLng: LatLng): Address? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            addresses?.firstOrNull() // Return 1st address found, or null if empty
        } catch (e: Exception) {
            Log.e("MapsUtils", "Failed to get address from LatLng", e)
            null
        }
    }

}

