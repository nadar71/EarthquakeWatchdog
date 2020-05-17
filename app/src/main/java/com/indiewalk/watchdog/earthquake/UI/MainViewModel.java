package com.indiewalk.watchdog.earthquake.UI;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.SingletonProvider;
import com.indiewalk.watchdog.earthquake.data.Earthquake;
import com.indiewalk.watchdog.earthquake.data.EarthquakeRepository;
import com.indiewalk.watchdog.earthquake.UI.MainActivityEarthquakesList;

import java.util.List;

public class MainViewModel extends ViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();


    // Livedata on Earthquakes List to be populated by ViewModel
    private LiveData<List<Earthquake>> earthquakesEntries;

    // Livedata var on Earthquake obj to populate through ViewModel
    // ** not used for the moment
    private LiveData<Earthquake> earthquakeSingleEntry;

    private EarthquakeRepository eqRepository;




    private static Context context;

    private SharedPreferences sharedPreferences;
    private String minMagnitude;
    private double dMinMagnitude;


    /**
     * ---------------------------------------------------------------------------------------------
     * Standard MainViewModel constructor
     * ---------------------------------------------------------------------------------------------
     */
    public MainViewModel() {
        context            = (SingletonProvider) SingletonProvider.getsContext();

        // init repository
        eqRepository       = ((SingletonProvider) SingletonProvider.getsContext()).getRepository();
        earthquakesEntries = eqRepository.getEarthquakesList();

        // init shared preferences
        sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(context);
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Constructor with parameter used by {@link MainViewModelFactory}
     * : init the attributes with LiveData<List<Earthquake>>
     * @param listType
     * ---------------------------------------------------------------------------------------------
     */
    public MainViewModel(String listType) {
        Log.d(TAG, "Actively retrieving the collections from repository");

        context = (SingletonProvider) SingletonProvider.getsContext();

        // get repository instance
        // eqRepository = ((SingletonProvider) SingletonProvider.getsContext()).getRepository();
        eqRepository = ((SingletonProvider) SingletonProvider.getsContext()).getRepositoryWithDataSource();

        // init shared preferences and get value
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        checkPreferences();
        dMinMagnitude = Double.parseDouble(minMagnitude);


        // choose the type of Earthquakes list to load from db
        if (listType.equals(MainActivityEarthquakesList.ORDER_BY_DESC_MAGNITUDE)) {
            Log.d(TAG, "setupAdapter: ORDER_BY_DESC_MAGNITUDE : " + listType);
            earthquakesEntries = eqRepository.loadAll_orderby_desc_mag(dMinMagnitude);

        }else if (listType.equals(MainActivityEarthquakesList.ORDER_BY_ASC_MAGNITUDE)) {
            Log.d(TAG, "setupAdapter: ORDER_BY_ASC_MAGNITUDE : " + listType);
            earthquakesEntries = eqRepository.loadAll_orderby_asc_mag(dMinMagnitude);

        }else if (listType.equals(MainActivityEarthquakesList.ORDER_BY_MOST_RECENT)){
            Log.d(TAG, "setupAdapter: ORDER_BY_MOST_RECENT : " + listType);
            earthquakesEntries = eqRepository.loadAll_orderby_most_recent(dMinMagnitude);

        }else if (listType.equals(MainActivityEarthquakesList.ORDER_BY_OLDEST)){
            Log.d(TAG, "setupAdapter: ORDER_BY_OLDEST : " + listType);
            earthquakesEntries = eqRepository.loadAll_orderby_oldest(dMinMagnitude);

        }else if (listType.equals(MainActivityEarthquakesList.ORDER_BY_NEAREST)){
            Log.d(TAG, "setupAdapter: ORDER_BY_NEAREST : " + listType);
            earthquakesEntries = eqRepository.loadAll_orderby_nearest(dMinMagnitude);

        }else if (listType.equals(MainActivityEarthquakesList.ORDER_BY_FURTHEST)){
            Log.d(TAG, "setupAdapter: ORDER_BY_FURTHEST : " + listType);
            earthquakesEntries = eqRepository.loadAll_orderby_furthest(dMinMagnitude);

        }else if (listType.equals(MainActivityEarthquakesList.LOAD_ALL_NO_ORDER)){
            Log.d(TAG, "setupAdapter: LOAD_ALL_NO_ORDER : " + listType);
            earthquakesEntries = eqRepository.loadAll();
        }

    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Getter for LiveData<List<Earthquake>> list
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public LiveData<List<Earthquake>> getEqList() {
        return earthquakesEntries;
    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Set and check location coordinates from shared preferences.
     * If not set, put default value
     * ---------------------------------------------------------------------------------------------
     */
    private void checkPreferences() {

        // recover min magnitude value from prefs or set a default from string value
        minMagnitude = sharedPreferences.getString(
                context.getString(R.string.settings_min_magnitude_key),
                context.getString(R.string.settings_min_magnitude_default));


        // check preferences safety
        safePreferencesValue();

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * making code more robust checking if for same reasons the defaut value stored are null or
     * not equals to none of the preferences stored values (e.g.  in case of key value change on code
     * but user saved with the previous one with previous app version )
     * ---------------------------------------------------------------------------------------------
     */
    private void safePreferencesValue() {

        SharedPreferences.Editor editor = sharedPreferences.edit();

        // minMagnitude for safe
        if (minMagnitude.isEmpty() || minMagnitude == null) {
            setMinMagDefault(editor);
        }

        if ((!minMagnitude.equals(context.getString(R.string.settings_1_0_min_magnitude_value))) &&
            (!minMagnitude.equals(context.getString(R.string.settings_2_0_min_magnitude_value))) &&
            (!minMagnitude.equals(context.getString(R.string.settings_3_0_min_magnitude_value))) &&
            (!minMagnitude.equals(context.getString(R.string.settings_4_0_min_magnitude_value))) &&
            (!minMagnitude.equals(context.getString(R.string.settings_4_5_min_magnitude_value))) &&
            (!minMagnitude.equals(context.getString(R.string.settings_5_0_min_magnitude_value))) &&
            (!minMagnitude.equals(context.getString(R.string.settings_5_5_min_magnitude_value))) &&
            (!minMagnitude.equals(context.getString(R.string.settings_6_0_min_magnitude_value))) &&
            (!minMagnitude.equals(context.getString(R.string.settings_6_5_min_magnitude_value)))
        ){
            setMinMagDefault(editor);
        }

    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Set min_magnitude to default
     * @param editor
     * ---------------------------------------------------------------------------------------------
     */
    private void setMinMagDefault(SharedPreferences.Editor editor){
        minMagnitude = context.getString(R.string.settings_min_magnitude_default);
        editor.putString(context.getString(R.string.settings_min_magnitude_key),
                context.getString(R.string.settings_min_magnitude_default));
    }



}
