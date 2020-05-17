package com.indiewalk.watchdog.earthquake.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

@Dao
interface EarthquakeDbDao {

    //----------------------------------------------------------------------------------------------
    //  QUERY
    //----------------------------------------------------------------------------------------------
    // retrieve all the eqs
    @Query("SELECT * FROM EARTHQUAKE_LIST ")
    fun loadAll(): LiveData<List<Earthquake>>

    @Query("SELECT * FROM EARTHQUAKE_LIST ")
    fun loadAllNoLiveData(): List<Earthquake>

    // retrieve all the eqs order by desc magnitude
    @Query("SELECT * FROM EARTHQUAKE_LIST WHERE magnitude >=:min_mag ORDER BY magnitude desc")
    fun loadAll_orderby_desc_mag(min_mag: Double): LiveData<List<Earthquake>>

    // retrieve all the eqs order by asc magnitude
    @Query("SELECT * FROM EARTHQUAKE_LIST WHERE magnitude >=:min_mag ORDER BY magnitude asc")
    fun loadAll_orderby_asc_mag(min_mag: Double): LiveData<List<Earthquake>>

    // retrieve all the eqs order by min magnitude
    @Query("SELECT * FROM EARTHQUAKE_LIST WHERE magnitude >=:min_mag")
    fun loadAll_orderby_min_mag(min_mag: Double): LiveData<List<Earthquake>>

    // retrieve all the eqs order by most recent (time desc)
    @Query("SELECT * FROM EARTHQUAKE_LIST WHERE magnitude >=:min_mag ORDER BY timeInMillisec desc")
    fun loadAll_orderby_most_recent(min_mag: Double): LiveData<List<Earthquake>>

    // retrieve all the eqs order by oldest (time asc)
    @Query("SELECT * FROM EARTHQUAKE_LIST WHERE magnitude >=:min_mag ORDER BY timeInMillisec asc")
    fun loadAll_orderby_oldest(min_mag: Double): LiveData<List<Earthquake>>

    // retrieve all the eqs order by nearest to user
    @Query("SELECT * FROM EARTHQUAKE_LIST WHERE magnitude >=:min_mag ORDER BY userDistance asc")
    fun loadAll_orderby_nearest(min_mag: Double): LiveData<List<Earthquake>>

    // retrieve all the eqs order by furthest to user
    @Query("SELECT * FROM EARTHQUAKE_LIST WHERE magnitude >=:min_mag ORDER BY userDistance desc")
    fun loadAll_orderby_furthest(min_mag: Double): LiveData<List<Earthquake>>


    //----------------------------------------------------------------------------------------------
    //  INSERT
    //----------------------------------------------------------------------------------------------
    @Insert
    fun insertEarthquake(earthquake: Earthquake)

    // Insert all the earthquakes info get from restful at a new update
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun renewDataInsert(vararg earthquake: Earthquake)


    //----------------------------------------------------------------------------------------------
    //  UPDATE
    //----------------------------------------------------------------------------------------------
    @Query("UPDATE EARTHQUAKE_LIST SET userDistance =:new_distance WHERE id =:tid")
    fun updatedEqDistanceFromUser(new_distance: Int, tid: Int)

    //----------------------------------------------------------------------------------------------
    //  DROP TABLE
    //----------------------------------------------------------------------------------------------
    // drop table : delete all table content each loading
    @Query("DELETE FROM EARTHQUAKE_LIST")
    fun dropEarthquakeListTable()
}
