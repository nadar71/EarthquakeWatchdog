package com.indiewalk.watchdog.earthquake.data;

import android.arch.lifecycle.LiveData;

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
    public LiveData<List<Earthquake>> loadAll(){
        return eqDb.earthquakeDbDao().loadAll();
    }

    // retrieve all the eqs order by desc magnitude
    public LiveData<List<Earthquake>> loadAll_orderby_desc_mag(){
        return eqDb.earthquakeDbDao().loadAll_orderby_desc_mag();
    }


    // retrieve all the eqs order by asc magnitude
    public LiveData<List<Earthquake>> loadAll_orderby_asc_mag(){
        return eqDb.earthquakeDbDao().loadAll_orderby_asc_mag();
    }


    // retrieve all the eqs order by most recent (time desc)
    public LiveData<List<Earthquake>> loadAll_orderby_most_recent(){
        return eqDb.earthquakeDbDao().loadAll_orderby_most_recent();
    }

    // retrieve all the eqs order by nearest to user
    public LiveData<List<Earthquake>> loadAll_orderby_nearest(){
        return eqDb.earthquakeDbDao().loadAll_orderby_nearest();
    }

    // retrieve all the eqs order by farthest to user
    public LiveData<List<Earthquake>> loadAll_orderby_farthest(){
        return eqDb.earthquakeDbDao().loadAll_orderby_farthest();
    }





    //----------------------------------------------------------------------------------------------
    //  INSERT
    //----------------------------------------------------------------------------------------------
    public void insertEarthquake(Earthquake earthquake){
        eqDb.earthquakeDbDao().insertEarthquake(earthquake);
    }



    //----------------------------------------------------------------------------------------------
    //  DROP TABLE
    //----------------------------------------------------------------------------------------------
    // drop table : delete all table content each loading
    public void dropEarthquakeListTable(){
        dropEarthquakeListTable();
    }


    //----------------------------------------------------------------------------------------------
    //  OTHER METHODS
    //----------------------------------------------------------------------------------------------

    /**
     * Return  the non-empty list of earthquake if any
     * @return
     */
    public LiveData<List<Earthquake>> getEarthquakesList(){
        return loadAll();

    }








}
