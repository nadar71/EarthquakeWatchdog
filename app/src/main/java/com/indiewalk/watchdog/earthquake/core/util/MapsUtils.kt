package com.indiewalk.watchdog.earthquake.core.util

import android.util.Log
import com.indiewalk.watchdog.earthquake.core.data.AppPreferences.device_lat
import com.indiewalk.watchdog.earthquake.core.data.AppPreferences.device_lng
import com.indiewalk.watchdog.earthquake.core.data.AppPreferences.unitSystem
import com.indiewalk.watchdog.earthquake.core.data.Constants.KM_TO_MILES
import com.indiewalk.watchdog.earthquake.core.data.Constants.MILES_TO_KM
import com.indiewalk.watchdog.earthquake.core.data.enums.UnitSystems
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQGeometryDTO
import it.abenergie.customerarea.core.utility.extensions.TAG
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
    private fun haversineDistanceCalc(p1Lat: Double, p2Lat: Double, p1Lng: Double, p2Lng: Double): Double {
        val R = 6378137 // Earth’s mean radius in meter
        val dLat = fromDegreeToRadiant(p2Lat - p1Lat)
        val dLng = fromDegreeToRadiant(p2Lng - p1Lng)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(fromDegreeToRadiant(p1Lat)) *
                cos(fromDegreeToRadiant(p2Lat)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val d = R * c
        return d / 1000 // returns the distance in Km
    }


    // Convert km to miles
    fun fromKmToMiles(km: Double): Double {
        return km * KM_TO_MILES
    }

    // Convert from miles to km
    fun fromMilesToKm(miles: Double): Double {
        return miles * MILES_TO_KM
    }


    // Update each equakes info with custom distance from user if any,with distance unit preferred.
    fun getEQDistanceFromUser(eqCoords: EQGeometryDTO): Int? {
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

            if (unitSystem == UnitSystems.IMPERIAL.value) {
                dist = fromKmToMiles(dist.toDouble())
            }

            Log.i(TAG, "getEQDistanceFromUser: eq distance from user : $dist in $unitSystem")

            return dist.toInt()
        }
    }
}

