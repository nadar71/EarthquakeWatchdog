package com.indiewalk.watchdog.earthquake;

import android.app.Application;
import android.content.Context;

import com.indiewalk.watchdog.earthquake.data.EarthquakeDatabase;
import com.indiewalk.watchdog.earthquake.data.EarthquakeRepository;
import com.indiewalk.watchdog.earthquake.util.AppExecutors;


/**
 * -------------------------------------------------------------------------------------------------
 * Class used for access singletons and application context wherever in the app.
 * Just like repository is an interface for all data operations.
 * Can be used dependency injection as well.
 * NB : register in manifest in <Application  android:name=".SingletonProvider" >... </Application>
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
    public EarthquakeRepository getRepository() {
        return EarthquakeRepository.getInstance(getDatabase());
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
}