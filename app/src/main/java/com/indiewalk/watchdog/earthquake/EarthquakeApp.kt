package com.indiewalk.watchdog.earthquake

import android.app.Application

import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.EarthquakeDatabase
// import com.indiewalk.watchdog.earthquake.data.EarthquakeDatabase_Impl;
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.repository.EarthquakeRepository
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.remote.OLD.EarthquakeNetworkDataSource
import com.indiewalk.watchdog.earthquake.core.util.AppExecutors


// Class used for access classes singletons and application context wherever in the app.
class EarthquakeApp : Application() {

    companion object {
        lateinit var appContext: Application
    }

    // Return AppExecutors singleton instance
    private var appExecutorsInstance: AppExecutors? = null

    // Return singleton db instance
    private val database: EarthquakeDatabase?
        get() = EarthquakeDatabase.getDbInstance(this)


    // Return depository singleton instance
    val repository: EarthquakeRepository?
        get() = database?.let { EarthquakeRepository.getInstance(it) }


    // repo constructor with data source support
    val repositoryWithDataSource: EarthquakeRepository?
        get() {
            val db = database
            val executors = AppExecutors.instance
            val networkDataSource = executors?.let {
                EarthquakeNetworkDataSource.getInstance(
                    this.applicationContext,
                    it
                )
            }

            return networkDataSource?.let {
                EarthquakeRepository.getInstanceWithDataSource(
                    db!!,
                    it,
                    executors!!
                )
            }
        }


    // Return EarthquakeDatasource singleton instance
    // getRepository(); // the repository is not created if called from a intent service
    // needed otherwise the repository is not created if called from a intent service
    val networkDatasource: EarthquakeNetworkDataSource?
        get() {
            repositoryWithDataSource
            return appContext?.let {
                appExecutorsInstance?.let { it1 ->
                    EarthquakeNetworkDataSource.getInstance(
                        it,
                        it1
                    )
                }
            }
        }


    override fun onCreate() {
        super.onCreate()
        appExecutorsInstance = AppExecutors.instance
        appContext = this
    }



}
