package com.indiewalk.watchdog.earthquake.data;

import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

public class EarthquakeRepository {

    private static final String TAG = EarthquakeRepository.class.getSimpleName();

    private static EarthquakeRepository sInstance;

    private final EarthquakeDatabase eqDb;

    private EarthquakeRepository(EarthquakeDatabase earthquakeDatabase){
        this.eqDb = earthquakeDatabase;
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Get singleton instance
     * @param database
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public static EarthquakeRepository getInstance(final EarthquakeDatabase database) {
        if (sInstance == null) {
            synchronized (EarthquakeRepository.class) {
                if (sInstance == null) {
                    sInstance = new EarthquakeRepository(database);
                }
            }
        }
        return sInstance;
    }



    //----------------------------------------------------------------------------------------------
    //  QUERY
    //----------------------------------------------------------------------------------------------
    // retrieve all the eqs
    public List<Earthquake> loadAll(){
        return eqDb.earthquakeDbDao().loadAll();
    }

    // retrieve all the eqs order by magnitude
    public List<Earthquake> loadAll_orderby_mag(){
        return eqDb.earthquakeDbDao().loadAll_orderby_mag();
    }

    // retrieve all the eqs order by most recent (time desc)
    public List<Earthquake> loadAll_orderby_most_recent(){
        return eqDb.earthquakeDbDao().loadAll_orderby_most_recent();
    }

    // retrieve all the eqs order by nearest to user
    public List<Earthquake> loadAll_orderby_nearest(){
        return eqDb.earthquakeDbDao().loadAll_orderby_nearest();
    }

    // retrieve all the eqs order by farthest to user
    public List<Earthquake> loadAll_orderby_farthest(){
        return eqDb.earthquakeDbDao().loadAll_orderby_farthest();
    }





    //----------------------------------------------------------------------------------------------
    //  INSERT
    //----------------------------------------------------------------------------------------------
    @Insert
    public void insertEarthquake(Earthquake earthquake){
        eqDb.earthquakeDbDao().insertEarthquake(earthquake);
    }









}
