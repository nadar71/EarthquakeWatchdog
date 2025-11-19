package com.indiewalk.watchdog.earthquake.feat_eqsmap.domain.use_cases

import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.repository.EQRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEarthquakesUseCase @Inject constructor(
    private val repository: EQRepository
) {
    suspend operator fun invoke(): Flow<List<EQEntity>> = repository.observeAll()
}