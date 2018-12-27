package com.indiewalk.watchdog.earthquake.data;

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
    // retrieve all the
    @Query("SELECT * FROM EARTHQUAKE_LIST ORDER BY magnitude")
    List<Earthquake> loadAllEarthquakeRetrieved();


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
