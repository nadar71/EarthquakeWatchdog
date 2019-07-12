package com.indiewalk.watchdog.earthquake.net;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.indiewalk.watchdog.earthquake.SingletonProvider;

public class EarthquakeFirebaseJobService extends JobService {
    private static final String LOG_TAG = EarthquakeFirebaseJobService.class.getSimpleName();

    /**
     * ---------------------------------------------------------------------------------------------
     * Job entry point, called by the Job Dispatcher.
     * @return  whether there is more work remaining.
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.d(LOG_TAG, "Earthquake remote fetching Job service started");

        EarthquakeNetworkDataSource networkDataSource =
                ((SingletonProvider)SingletonProvider.getsContext()).getNetworkDatasource();
        networkDataSource.fetchEarthquakeWrapper();

        jobFinished(jobParameters, false);
        return true;
    }


    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
