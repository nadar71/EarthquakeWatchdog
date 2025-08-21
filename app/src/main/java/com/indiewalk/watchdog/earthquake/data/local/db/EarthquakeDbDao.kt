package com.indiewalk.watchdog.earthquake.data.local.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indiewalk.watchdog.earthquake.domain.model.EarthquakeUI
import com.indiewalk.watchdog.earthquake.domain.model.remote.EQFeaturesCollection

@Dao
interface EarthquakeDbDao {

    //----------------------------------------------------------------------------------------------
    //  QUERY
    //----------------------------------------------------------------------------------------------

    // retrieve all the eqs
    @Query("SELECT * FROM Earthquakes ")
    fun loadAll(): MutableList<EQFeaturesCollection>

    @Query("SELECT * FROM Earthquakes ")
    fun loadAllNoLiveData(): List<EarthquakeUI>

    // retrieve all the eqs order by desc magnitude
    @Query("SELECT * FROM Earthquakes WHERE magnitude >=:min_mag ORDER BY magnitude desc")
    fun loadAll_orderby_desc_mag(min_mag: Double): LiveData<List<EQFeaturesCollection>>

    // retrieve all the eqs order by asc magnitude
    @Query("SELECT * FROM Earthquakes WHERE magnitude >=:min_mag ORDER BY magnitude asc")
    fun loadAll_orderby_asc_mag(min_mag: Double): LiveData<List<EQFeaturesCollection>>

    // retrieve all the eqs order by min magnitude
    @Query("SELECT * FROM Earthquakes WHERE magnitude >=:min_mag")
    fun loadAll_orderby_min_mag(min_mag: Double): LiveData<List<EQFeaturesCollection>>

    // retrieve all the eqs order by most recent (time desc)
    @Query("SELECT * FROM Earthquakes WHERE magnitude >=:min_mag ORDER BY timeInMillisec desc")
    fun loadAll_orderby_most_recent(min_mag: Double): LiveData<List<EQFeaturesCollection>>

    // retrieve all the eqs order by oldest (time asc)
    @Query("SELECT * FROM Earthquakes WHERE magnitude >=:min_mag ORDER BY timeInMillisec asc")
    fun loadAll_orderby_oldest(min_mag: Double): LiveData<List<EQFeaturesCollection>>

    // retrieve all the eqs order by nearest to user
    @Query("SELECT * FROM Earthquakes WHERE magnitude >=:min_mag ORDER BY userDistance asc")
    fun loadAll_orderby_nearest(min_mag: Double): LiveData<List<EQFeaturesCollection>>

    // retrieve all the eqs order by furthest to user
    @Query("SELECT * FROM Earthquakes WHERE magnitude >=:min_mag ORDER BY userDistance desc")
    fun loadAll_orderby_furthest(min_mag: Double): LiveData<List<EQFeaturesCollection>>


    //----------------------------------------------------------------------------------------------
    //  INSERT
    //----------------------------------------------------------------------------------------------
    @Insert
    fun insertEarthquake(earthquakeUI: EQFeaturesCollection)

    // Insert all the earthquakes info get from restful at a new update
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun renewDataInsert(vararg earthquakeUI: EQFeaturesCollection)


    //----------------------------------------------------------------------------------------------
    //  UPDATE
    //----------------------------------------------------------------------------------------------
    @Query("UPDATE Earthquakes SET userDistance =:new_distance WHERE id =:tid")
    fun updatedEqDistanceFromUser(new_distance: Int, tid: Int)

    //----------------------------------------------------------------------------------------------
    //  DROP TABLE
    //----------------------------------------------------------------------------------------------
    // drop table : delete all table content each loading
    @Query("DELETE FROM Earthquakes")
    fun dropEarthquakeListTable()
}
