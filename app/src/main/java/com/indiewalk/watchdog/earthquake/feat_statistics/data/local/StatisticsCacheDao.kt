package com.indiewalk.watchdog.earthquake.feat_statistics.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface StatisticsCacheDao {
    @Query("SELECT * FROM statistics_cache WHERE cacheId = 1")
    suspend fun get(): StatisticsCacheEntity?

    @Upsert
    suspend fun replace(entity: StatisticsCacheEntity)
}
