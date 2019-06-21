package com.indiewalk.watchdog.earthquake.UI;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.indiewalk.watchdog.earthquake.SingletonProvider;
import com.indiewalk.watchdog.earthquake.data.Earthquake;
import com.indiewalk.watchdog.earthquake.data.EarthquakeRepository;

import java.util.List;

public class MainViewModel extends ViewModel {

    // tag for logging
    private static final String TAG = MainViewModel.class.getSimpleName();


    // Livedata var on Earthquake List to populate through ViewModel
    private LiveData<List<Earthquake>> earthquakesEntries;

    // Livedata var on Earthquake obj to populate through ViewModel
    // ** not used for the moment
    private LiveData<Earthquake> earthquakeSingleEntry;

    // repository ref
    private EarthquakeRepository eqRepository;


    /**
     * Standard MainViewModel constructor
     */
    public MainViewModel() {
        // init repository
        eqRepository       = ((SingletonProvider) SingletonProvider.getsContext()).getRepository();
        earthquakesEntries = eqRepository.getEarthquakesList();
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

        // get repository instance
        eqRepository = ((SingletonProvider) SingletonProvider.getsContext()).getRepository();



        // choose the type of food list to load from db
        if (listType.equals(MainActivity.ORDER_BY_MAGNITUDE)) {
            Log.d(TAG, "setupAdapter: ORDER_BY_MAGNITUDE : " + listType);
            earthquakesEntries = eqRepository.loadAll_orderby_mag();

        }else if (listType.equals(MainActivity.ORDER_BY_MOST_RECENT)){
            Log.d(TAG, "setupAdapter: ORDER_BY_MOST_RECENT : " + listType);
            earthquakesEntries = eqRepository.loadAll_orderby_most_recent();

        }else if (listType.equals(MainActivity.ORDER_BY_NEAREST)){
            Log.d(TAG, "setupAdapter: ORDER_BY_NEAREST : " + listType);
            earthquakesEntries = eqRepository.loadAll_orderby_nearest();

        }else if (listType.equals(MainActivity.ORDER_BY_FARTHEST)){
            Log.d(TAG, "setupAdapter: ORDER_BY_FARTHEST : " + listType);
            earthquakesEntries = eqRepository.loadAll_orderby_farthest();

        }else if (listType.equals(MainActivity.LOAD_ALL_NO_ORDER)){
            Log.d(TAG, "setupAdapter: LOAD_ALL_NO_ORDER : " + listType);
            earthquakesEntries = eqRepository.loadAll();
        }

    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Getter for LiveData<List<FoodEntry>> list
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public LiveData<List<Earthquake>> getEqList() {
        return earthquakesEntries;
    }



}
