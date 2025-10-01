package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity

@Dao
interface EarthquakeDao {

    //------------------------------------------- QUERY --------------------------------------------

    // retrieve all the eqs
    @Query("SELECT * FROM earthquakes ")
    suspend  fun loadAllEQs(): MutableList<EQEntity>

    // retrieve all the eqs order by desc magnitude
    @Query("SELECT * FROM earthquakes WHERE mag >=:min_mag ORDER BY mag desc")
    suspend  fun loadAllEQs_orderby_desc_mag(min_mag: Double): MutableList<EQEntity>

    // retrieve all the eqs order by asc magnitude
    @Query("SELECT * FROM Earthquakes WHERE mag >=:min_mag ORDER BY mag asc")
    suspend  fun loadAllEQs_orderby_asc_mag(min_mag: Double): MutableList<EQEntity>

    // retrieve all the eqs order by min magnitude
    @Query("SELECT * FROM Earthquakes WHERE mag >=:min_mag")
    suspend  fun loadAll_orderby_min_mag(min_mag: Double): MutableList<EQEntity>

    // retrieve all the eqs order by most recent (time desc)
    @Query("SELECT * FROM Earthquakes WHERE mag >=:min_mag ORDER BY time desc")
    suspend  fun loadAll_orderby_most_recent(min_mag: Double): MutableList<EQEntity>

    // retrieve all the eqs order by oldest (time asc)
    @Query("SELECT * FROM Earthquakes WHERE mag >=:min_mag ORDER BY time asc")
    suspend  fun loadAll_orderby_oldest(min_mag: Double): MutableList<EQEntity>

    // retrieve all the eqs order by nearest to user
    @Query("SELECT * FROM Earthquakes WHERE mag >=:min_mag ORDER BY distanceFromUser asc")
    suspend  fun loadAll_orderby_nearest(min_mag: Double): MutableList<EQEntity>

    // retrieve all the eqs order by furthest to user
    @Query("SELECT * FROM Earthquakes WHERE mag >=:min_mag ORDER BY distanceFromUser desc")
    suspend  fun loadAll_orderby_furthest(min_mag: Double): MutableList<EQEntity>

    // retrieve all the eqs in a box
    @Query("""
        SELECT * FROM earthquakes
        WHERE :west <= longitude AND longitude <= :east
          AND :south <= latitude AND latitude <= :north
        ORDER BY time DESC
        LIMIT :limit
    """)
    suspend  fun loadAllInBbox(
        west: Double, south: Double, east: Double, north: Double, limit: Int = 200
    ): MutableList<EQEntity>

    //----------------------------------- INSERT/UPDATE --------------------------------------------

    // insert/update the eq
    @Upsert
    suspend  fun upsertEarthquake(eqEntity: EQEntity)

    @Query("UPDATE earthquakes SET distanceFromUser =:new_distance WHERE id =:id")
    suspend  fun updatedEqDistanceFromUser(new_distance: Int, id: Int)

    //----------------------------------------- DROP TABLE -----------------------------------------
    // drop table : delete all table content each loading
    @Query("DELETE FROM earthquakes")
    suspend  fun dropEarthquakeListTable()

    @Delete
    suspend fun delete(eqEntity: EQEntity)

    // delete all the eqs older than cutoff date/time
    @Query("DELETE FROM earthquakes WHERE time IS NOT NULL AND time < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}