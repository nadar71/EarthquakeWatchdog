package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.use_cases

import android.content.Context
import android.util.Log
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.repository.EQRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.indiewalkabout.fridgemanager.core.domain.model.DbResponse
import eu.indiewalkabout.fridgemanager.core.domain.model.ErrorResponse
import javax.inject.Inject

class UpdateEqDistanceUseCase @Inject constructor(
    private val repository: EQRepository,
    @ApplicationContext private val context: Context
) {
    private val TAG = "UpdateEqDistanceUseCase"

    suspend operator fun invoke(newDistance: Int, id: Int): DbResponse<Unit> {
        return try {
            repository.updatedEqDistanceFromUser(newDistance, id)
            DbResponse.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage ?: context.getString(R.string.db_generic_error))
            DbResponse.Error(
                ErrorResponse(
                    0,
                    listOf(),
                    e.localizedMessage ?: context.getString(R.string.db_generic_error)
                )
            )
        }
    }
}