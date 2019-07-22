package com.indiewalk.watchdog.earthquake.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Query;
import android.util.Log;

import com.indiewalk.watchdog.earthquake.SingletonProvider;
import com.indiewalk.watchdog.earthquake.net.EarthquakeNetworkDataSource;
import com.indiewalk.watchdog.earthquake.util.AppExecutors;
import com.indiewalk.watchdog.earthquake.util.MyUtil;

import java.util.List;

public class EarthquakeRepository {

    private static final String TAG = EarthquakeRepository.class.getSimpleName();

    private static EarthquakeRepository  sInstance;
    private EarthquakeNetworkDataSource  networkDataSource;
    private AppExecutors                 executors;
    private boolean                      mInitialized = false;

    private  EarthquakeDatabase eqDb;

    // standard constructor
    private EarthquakeRepository(EarthquakeDatabase earthquakeDatabase){
        this.eqDb = earthquakeDatabase;
    }

    // constructor with data source
    private EarthquakeRepository(EarthquakeDatabase earthquakeDatabase,
                                 EarthquakeNetworkDataSource networkDataSource,
                                 AppExecutors executors) {
        this.eqDb                = earthquakeDatabase;
        this.networkDataSource   = networkDataSource;
        this.executors           = executors;

        // get a copy of data from data source, to keep them updated through observer
        LiveData<Earthquake[]> networkData = networkDataSource.getEarthquakesData();

        // observe data from data source; in case of change, update all the db
        networkData.observeForever(newEqFromNetwork ->{
            executors.diskIO().execute( () ->{
                        Log.d(TAG, "EarthquakeRepository observer : " +
                                "New values found, deletes previous and insert new ones. ");
                        // Delete old and insert new data
                        dropEarthquakeListTable();

                        // update with distance from user, distance unit each earthquake
                        MyUtil.setEqDistanceFromCurrentCoords(newEqFromNetwork,
                                (SingletonProvider)SingletonProvider.getsContext());

                        eqDb.earthquakeDbDao().renewDataInsert(newEqFromNetwork);
                        Log.d(TAG, "WeatherAppRepository observer : New values inserted. ");
                    }

            );
        });


    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Get repo singleton instance for standard constructor
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


    /**
     * ---------------------------------------------------------------------------------------------
     * Get repo singleton instance for constructor with data source support
     * @param earthquakeDatabase
     * @param networkDataSource
     * @param executors
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public static EarthquakeRepository getInstanceWithDataSource(EarthquakeDatabase earthquakeDatabase,
                                                                 EarthquakeNetworkDataSource networkDataSource,
                                                                 AppExecutors executors) {
        if (sInstance == null) {
            synchronized (EarthquakeRepository.class) {
                if (sInstance == null) {
                    sInstance = new EarthquakeRepository(earthquakeDatabase,networkDataSource,executors);
                }
            }
        }
        return sInstance;
    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     * ---------------------------------------------------------------------------------------------
     */
    private synchronized void initializeData() {

        // Db data initialized check : at every app start
        if (mInitialized) {
            Log.d(TAG, "initializeData: data already initialized, return");
            return;
        }

        // if not initialized :
        mInitialized = true;

        // Set Synchronizing data every SYNC_INTERVAL_HOURS; if already present replace
        networkDataSource.scheduleRecurringFetchEarthquakeSync();


        // try to fetch earthquakes remote data if needed
        executors.diskIO().execute(()->{
            if (MyUtil.isConnectionOk()) {
                if(isRequestDataNeeded()){
                    Log.d(TAG, "initializeData: isFetchNeeded == true, run the intent from fetching data from remote");
                    startFetchEarthquakeService();
                }
            } else {
                return;
            }

        });
    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Checks if there are eqs data in db, otherwise return true to request remote data
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    private boolean isRequestDataNeeded() {
        List<Earthquake> eqs = eqDb.earthquakeDbDao().loadAllNoLiveData();
        if (eqs.size() <= 0) return true; else return false;
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Start IntentService of EarthquakeNetworkDataSource
     * ---------------------------------------------------------------------------------------------
     */
    private void startFetchEarthquakeService() {
        // call the intent service for retrieving network data daemon
        networkDataSource.startFetchEarthquakeService();
    }


    //----------------------------------------------------------------------------------------------
    //  QUERY
    //----------------------------------------------------------------------------------------------
    // retrieve all the eqs
    public LiveData<List<Earthquake>> loadAll(){
        initializeData();
        return eqDb.earthquakeDbDao().loadAll();
    }

    // retrieve all the eqs order by desc magnitude
    public LiveData<List<Earthquake>> loadAll_orderby_desc_mag(double min_mag){
        initializeData();
        return eqDb.earthquakeDbDao().loadAll_orderby_desc_mag(min_mag);
    }


    // retrieve all the eqs order by asc magnitude
    public LiveData<List<Earthquake>> loadAll_orderby_asc_mag(double min_mag){
        initializeData();
        return eqDb.earthquakeDbDao().loadAll_orderby_asc_mag(min_mag);
    }


    // retrieve all the eqs order by most recent (time desc)
    public LiveData<List<Earthquake>> loadAll_orderby_most_recent(double min_mag){
        initializeData();
        return eqDb.earthquakeDbDao().loadAll_orderby_most_recent(min_mag);
    }


    // retrieve all the eqs order by oldest (time asc)
    public LiveData<List<Earthquake>> loadAll_orderby_oldest(double min_mag){
        initializeData();
        return eqDb.earthquakeDbDao().loadAll_orderby_oldest(min_mag);
    }


    // retrieve all the eqs order by nearest to user
    public LiveData<List<Earthquake>> loadAll_orderby_nearest(double min_mag){
        initializeData();
        return eqDb.earthquakeDbDao().loadAll_orderby_nearest(min_mag);
    }

    // retrieve all the eqs order by furthest to user
    public LiveData<List<Earthquake>> loadAll_orderby_furthest(double min_mag){
        initializeData();
        return eqDb.earthquakeDbDao().loadAll_orderby_furthest(min_mag);
    }





    //----------------------------------------------------------------------------------------------
    //  INSERT
    //----------------------------------------------------------------------------------------------
    public void insertEarthquake(Earthquake earthquake){
        eqDb.earthquakeDbDao().insertEarthquake(earthquake);
    }


    //----------------------------------------------------------------------------------------------
    //  UPDATE
    //----------------------------------------------------------------------------------------------
    public void updatedAllEqsDistFromUser(List<Earthquake> equakes){
        executors.diskIO().execute( () ->{
                    Log.d(TAG, "Updating eqs distances from current user location. ");
                    for(Earthquake eq:equakes) {
                        eqDb.earthquakeDbDao().updatedEqDistanceFromUser(eq.getUserDistance(),
                                eq.getId());
                    }
                    Log.d(TAG, "Updating eqs distances from current user location. : New values inserted. ");
                }

        );

    }


    //----------------------------------------------------------------------------------------------
    //  DELETE
    //----------------------------------------------------------------------------------------------
    // drop table : delete all table content
    public void dropEarthquakeListTable(){
        eqDb.earthquakeDbDao().dropEarthquakeListTable();
    }


    /**
     * ----------------------------------------------------------------------------------------------
     * Return  the non-empty list of earthquake if any
     * @return
     * ----------------------------------------------------------------------------------------------
     */
    public LiveData<List<Earthquake>> getEarthquakesList(){
        return loadAll();
    }



}
