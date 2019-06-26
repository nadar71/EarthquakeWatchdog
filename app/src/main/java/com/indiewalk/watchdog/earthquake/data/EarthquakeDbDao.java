package com.indiewalk.watchdog.earthquake.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface EarthquakeDbDao {

    //----------------------------------------------------------------------------------------------
    //  QUERY
    //----------------------------------------------------------------------------------------------
    // retrieve all the eqs
    @Query("SELECT * FROM EARTHQUAKE_LIST ")
    LiveData<List<Earthquake>> loadAll();

    // retrieve all the eqs order by desc magnitude
    @Query("SELECT * FROM EARTHQUAKE_LIST ORDER BY magnitude desc")
    LiveData<List<Earthquake>> loadAll_orderby_desc_mag();

    // retrieve all the eqs order by asc magnitude
    @Query("SELECT * FROM EARTHQUAKE_LIST ORDER BY magnitude asc")
    LiveData<List<Earthquake>> loadAll_orderby_asc_mag();

    // retrieve all the eqs order by min magnitude
    // TODO: SET IN REPO
    @Query("SELECT * FROM EARTHQUAKE_LIST WHERE magnitude >:min_mag")
    LiveData<List<Earthquake>> loadAll_orderby_min_mag(double min_mag);


    // retrieve all the eqs order by most recent (time desc)
    @Query("SELECT * FROM EARTHQUAKE_LIST ORDER BY timeInMillisec desc")
    LiveData<List<Earthquake>> loadAll_orderby_most_recent();

    // retrieve all the eqs order by nearest to user
    @Query("SELECT * FROM EARTHQUAKE_LIST ORDER BY userDistance asc")
    LiveData<List<Earthquake>> loadAll_orderby_nearest();

    // retrieve all the eqs order by farthest to user
    @Query("SELECT * FROM EARTHQUAKE_LIST ORDER BY userDistance desc")
    LiveData<List<Earthquake>> loadAll_orderby_farthest();





    //----------------------------------------------------------------------------------------------
    //  INSERT
    //----------------------------------------------------------------------------------------------
    @Insert
    void insertEarthquake(Earthquake earthquake);


    //----------------------------------------------------------------------------------------------
    //  DROP TABLE
    //----------------------------------------------------------------------------------------------
    // drop table : delete all table content each loading
    @Query("DELETE FROM EARTHQUAKE_LIST")
    public void dropEarthquakeListTable();
}
