package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.use_cases

import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.haversineDistanceKm
import javax.inject.Inject
import kotlin.math.*

class ApplySortUseCase @Inject constructor() {
    fun invoke(
        items: List<EQEntity>,
        sort: EqsSortOption,
        userLatLng: LatLng? = null // needed for distance sorts if distanceFromUser is null
    ): List<EQEntity> {

        return when (sort) {
            EqsSortOption.MAG_DESC -> items.sortedByDescending { it.mag ?: Double.NEGATIVE_INFINITY }
            EqsSortOption.MAG_ASC  -> items.sortedBy { it.mag ?: Double.POSITIVE_INFINITY }
            EqsSortOption.DATE_ASC -> items.sortedBy { it.time ?: Long.MIN_VALUE }
            EqsSortOption.DATE_DESC-> items.sortedByDescending { it.time ?: Long.MIN_VALUE }
            EqsSortOption.DIST_ASC -> {
                val origin = userLatLng
                items.sortedBy { e ->
                    e.distanceFromUser?.toDouble()
                        ?: if (origin != null && e.latitude != null && e.longitude != null)
                            haversineDistanceKm(
                                origin.latitude, origin.longitude,
                                e.latitude, e.longitude
                            )
                        else Double.POSITIVE_INFINITY
                }
            }
            EqsSortOption.DIST_DESC -> {
                val origin = userLatLng
                items.sortedByDescending { e ->
                    e.distanceFromUser?.toDouble()
                        ?: if (origin != null && e.latitude != null && e.longitude != null)
                            haversineDistanceKm(
                                origin.latitude, origin.longitude,
                                e.latitude, e.longitude
                            )
                        else Double.NEGATIVE_INFINITY
                }
            }
        }
    }
}
