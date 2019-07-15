package com.indiewalk.watchdog.earthquake;

import android.app.Application;
import android.content.Context;

import com.indiewalk.watchdog.earthquake.data.EarthquakeDatabase;
import com.indiewalk.watchdog.earthquake.data.EarthquakeDatabase_Impl;
import com.indiewalk.watchdog.earthquake.data.EarthquakeRepository;
import com.indiewalk.watchdog.earthquake.net.EarthquakeNetworkDataSource;
import com.indiewalk.watchdog.earthquake.util.AppExecutors;


/**
 * -------------------------------------------------------------------------------------------------
 * Class used for access classes singletons and application context wherever in the app.
 * Just like repository is an interface for all data operations.
 * Can be used dependency injection as well.
 * NB : registered in manifest in <Application  android:name=".SingletonProvider" >... </Application>
 * -------------------------------------------------------------------------------------------------
 */
public class SingletonProvider extends Application {

    private AppExecutors mAppExecutors;

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppExecutors = AppExecutors.getInstance();
        sContext = getApplicationContext();
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Return singleton db instance
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public EarthquakeDatabase getDatabase() {
        return EarthquakeDatabase.getDbInstance(this);
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return depository singleton instance
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    // repo standard constructor
    public EarthquakeRepository getRepository() {
        return EarthquakeRepository.getInstance(getDatabase());
    }



    // repo constructor with data source support
    public EarthquakeRepository getRepositoryWithDataSource() {
        EarthquakeDatabase db = getDatabase();
        AppExecutors executors = AppExecutors.getInstance();
        EarthquakeNetworkDataSource networkDataSource =
                EarthquakeNetworkDataSource.getInstance(this.getApplicationContext(), executors);

        return EarthquakeRepository.getInstanceWithDataSource(db,networkDataSource, executors);
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return AppExecutors singleton instance
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public AppExecutors getAppExecutorsInstance() {
        return mAppExecutors;
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return application context wherever we are in the app
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public static Context getsContext(){
        return sContext;
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return EarthquakeDatasource singleton instance
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public EarthquakeNetworkDataSource getNetworkDatasource() {
        // getRepository(); // the repository is not created if called from a intent service
        // nedeed otherwise the repository is not created if called from a intent service
        getRepositoryWithDataSource();
        return EarthquakeNetworkDataSource.getInstance(sContext,mAppExecutors);
    }
}
