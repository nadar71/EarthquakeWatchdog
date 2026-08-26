package com.indiewalk.watchdog.earthquake.feat_eqsmap.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.provider.Settings
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.diagnostics.AppDiagnostics
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticCategory
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQGeometryDTO
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MapsUtils {


    // Returning the distance in Km between 2 points on a sphere throught the Haversine formula
    fun haversineDistanceKm(lat1: Double, lat2: Double, lng1: Double, lng2: Double): Double {
        val R = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }


    // Update each equakes info with custom distance from user if any, with distance unit preferred.
    fun getEQDistanceFromUser(
        eqCoords: EQGeometryDTO,
        settings: AppSettings
    ): Double? {
        val eqLat = eqCoords.latitude ?: return null
        val eqLng = eqCoords.longitude ?: return null

        val dist = haversineDistanceKm(
            lat1 = if (settings.manualLocOn) settings.manualPosition.latitude
                   else settings.userPosition.latitude,
            lat2 = eqLat,
            lng1 = if (settings.manualLocOn) settings.manualPosition.longitude
                   else settings.userPosition.longitude,
            lng2 = eqLng
        )
        return dist
    }


    fun openAppSettings(context: Context) {
        val uri = Uri.fromParts("package", context.packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // Get user's last location and updates creating a fused Location client provider
    // with suspend function
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLatLng(context: Context): LatLng? {
        return try {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            val loc = fused.lastLocation.await() ?: return null
            LatLng(loc.latitude, loc.longitude)
        } catch (e: Exception) {
            AppDiagnostics.recordNonFatal(DiagnosticCategory.LOCATION, e)
            null
        }
    }
    // Get user's last location and updates creating a fused Location client provider
    // with callback, no suspend function
    @SuppressLint("MissingPermission")
    fun getLastKnownLatLngNotSusp(context: Context, onResult: (LatLng?) -> Unit) {
        val fused = LocationServices.getFusedLocationProviderClient(context)
        fused.lastLocation
            .addOnSuccessListener { location ->
                onResult(location?.let { LatLng(it.latitude, it.longitude) })
            }
            .addOnFailureListener { e ->
                AppDiagnostics.recordNonFatal(DiagnosticCategory.LOCATION, e)
                onResult(null)
            }
    }

    // --- Variuos Reverse geocoding to get address from LatLng ---

    // get Address obj from LatLng
    fun getAddress(context: Context, latLng: LatLng): Address? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            addresses?.firstOrNull() // Return 1st address found, or null if empty
        } catch (e: Exception) {
            AppDiagnostics.recordNonFatal(DiagnosticCategory.LOCATION, e)
            null
        }
    }

    // Reverse geocoding to get address as String from LatLng
    fun getPlaceNameOrNull(context: Context, latLng: LatLng): String? = try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        list?.firstOrNull()?.getAddressLine(0)
    } catch (_: Exception) {
        null
    }

}
