package com.indiewalk.watchdog.earthquake

import android.app.Application
import android.content.Context

import com.indiewalk.watchdog.earthquake.data.local.EarthquakeDatabase
// import com.indiewalk.watchdog.earthquake.data.EarthquakeDatabase_Impl;
import com.indiewalk.watchdog.earthquake.data.EarthquakeRepository
import com.indiewalk.watchdog.earthquake.data.remote.EarthquakeNetworkDataSource
import com.indiewalk.watchdog.earthquake.util.AppExecutors


/**
 * -------------------------------------------------------------------------------------------------
 * Class used for access classes singletons and application context wherever in the app.
 * Just like repository is an interface for all data operations.
 * Can be used dependency injection as well.
 * NB : registered in manifest in <Application android:name=".AppEarthquake">... </Application>
 * -------------------------------------------------------------------------------------------------
 */
class AppEarthquake : Application() {

    /**
     * ---------------------------------------------------------------------------------------------
     * Return AppExecutors singleton instance
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    var appExecutorsInstance: AppExecutors? = null
        private set

    /**
     * ---------------------------------------------------------------------------------------------
     * Return singleton db instance
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    val database: EarthquakeDatabase?
        get() = EarthquakeDatabase.getDbInstance(this)


    /**
     * ---------------------------------------------------------------------------------------------
     * Return depository singleton instance
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    // repo standard constructor
    val repository: EarthquakeRepository?
        get() = database?.let { EarthquakeRepository.getInstance(it) }


    // repo constructor with data source support
    val repositoryWithDataSource: EarthquakeRepository?
        get() {
            val db = database
            val executors = AppExecutors.instance
            val networkDataSource = executors?.let { EarthquakeNetworkDataSource.getInstance(this.applicationContext, it) }

            return networkDataSource?.let { EarthquakeRepository.getInstanceWithDataSource(db!!, it, executors!!) }
        }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return EarthquakeDatasource singleton instance
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    // getRepository(); // the repository is not created if called from a intent service
    // nedeed otherwise the repository is not created if called from a intent service
    val networkDatasource: EarthquakeNetworkDataSource?
        get() {
            repositoryWithDataSource
            return sContext?.let { appExecutorsInstance?.let { it1 -> EarthquakeNetworkDataSource.getInstance(it, it1) } }
        }

    override fun onCreate() {
        super.onCreate()
        appExecutorsInstance = AppExecutors.instance
        sContext = applicationContext
    }

    companion object {

        private var sContext: Context? = null


        /**
         * ---------------------------------------------------------------------------------------------
         * Return application context wherever we are in the app
         * @return
         * ---------------------------------------------------------------------------------------------
         */
        fun getsContext(): Context? {
            return sContext
        }
    }
}
