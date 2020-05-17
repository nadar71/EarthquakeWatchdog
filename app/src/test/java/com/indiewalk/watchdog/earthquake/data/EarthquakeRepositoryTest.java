package com.indiewalk.watchdog.earthquake.data;

import android.content.Context;
import android.util.Log;


import com.indiewalk.watchdog.earthquake.util.AppExecutors;
import com.indiewalk.watchdog.earthquake.util.MyUtil;

import org.apache.tools.ant.taskdefs.Ear;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
EarthquakeRepositoryTest Error #121
Launching every single tests by itself they are passed, but running altogether only the first
it's ok, the other no, independently by which tests are active in the class.

Tried with reset mocks in teardown function and remock explicitly inside @before method,
as well as doing it inside each test (both reset and remock), check the commented code.
*/

@RunWith(MockitoJUnitRunner.class)
public class EarthquakeRepositoryTest {

    @Mock
    EarthquakeDatabase mockDb;

    @Mock
    EarthquakeDbDao mockDao;

    @Mock
    Earthquake earthquake;

    @Mock
    Context mockApplicationContext;


    EarthquakeRepository repository;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        repository = EarthquakeRepository.getInstance(mockDb);
        when(mockDb.earthquakeDbDao()).thenReturn(mockDao);
    }


    @Test // OK ran single
    public void loadAll_test() {
        EarthquakeRepository spy_repository = spy(repository);
        doNothing().when(spy_repository).initializeData();  //ignore this method
        spy_repository.loadAll();
        verify(mockDao, times(1)).loadAll();
    }


    @Test // OK ran single
    public void loadAll_orderby_desc_mag_test() {
        EarthquakeRepository spy_repository = spy(repository);
        doNothing().when(spy_repository).initializeData();
        spy_repository.loadAll_orderby_desc_mag(1.0);
        verify(mockDao, times(1)).loadAll_orderby_desc_mag(1.0);
    }


    @Test // OK ran single
    public void loadAll_orderby_asc_mag_test(){
        EarthquakeRepository spy_repository = spy(repository);
        doNothing().when(spy_repository).initializeData();
        spy_repository.loadAll_orderby_asc_mag(6.0);
        verify(mockDao, times(1)).loadAll_orderby_asc_mag(6.0);
    }


    @Test // OK ran single
    public void loadAll_orderby_most_recent_test() {
        EarthquakeRepository spy_repository = spy(repository);
        doNothing().when(spy_repository).initializeData();
        spy_repository.loadAll_orderby_most_recent(2.0);
        verify(mockDao, times(1)).loadAll_orderby_most_recent(2.0);
    }


    @Test // OK ran single
    public void loadAll_orderby_oldest_test() {
        EarthquakeRepository spy_repository = spy(repository);
        doNothing().when(spy_repository).initializeData();
        spy_repository.loadAll_orderby_oldest(2.0);
        verify(mockDao, times(1)).loadAll_orderby_oldest(2.0);
    }


    @Test // OK ran single
    public void loadAll_orderby_nearest_test() {
        EarthquakeRepository spy_repository = spy(repository);
        doNothing().when(spy_repository).initializeData();
        spy_repository.loadAll_orderby_nearest(2.0);
        verify(mockDao, times(1)).loadAll_orderby_nearest(2.0);
    }

    @Test // OK ran single
    public void loadAll_orderby_furthest_test() {
        EarthquakeRepository spy_repository = spy(repository);
        doNothing().when(spy_repository).initializeData();
        spy_repository.loadAll_orderby_furthest(2.0);
        verify(mockDao, times(1)).loadAll_orderby_furthest(2.0);
    }



    @Test // OK ran single
    public void insertEarthquake_test() {
        Date today = new Date();
        Long today_long = DateConverter.fromDate(today);
        Earthquake earthquake = new Earthquake(2.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100);

        repository.insertEarthquake(earthquake);
        verify(mockDao, times(1)).insertEarthquake(earthquake);
    }


    @Test // ERROR : renewDataInsert is not detected, something wrong with mocking
    public void updatedAllEqsDistFromUser_test() {
        List<Earthquake> earthquakes= generateEqList(10);
        Earthquake[] eq_array = new Earthquake[earthquakes.size()];
        eq_array = earthquakes.toArray(eq_array);
        AppExecutors executors = AppExecutors.getInstance();

        EarthquakeRepository spy_repository = spy(repository);
        doNothing().when(spy_repository).dropEarthquakeListTable();

        spy_repository.setExecutors(executors);

        MyUtil spy_myutil = spy(MyUtil.class);
        doNothing().when(spy_myutil).setEqDistanceFromCurrentCoords(earthquakes, mockApplicationContext);

        spy_repository.updatedAllEqsDistFromUser(earthquakes, mockApplicationContext);
        // verify(mockDao, times(1)).dropEarthquakeListTable();
        // verify(spy_myutil, times(1)).setEqDistanceFromCurrentCoords(earthquakes, mockApplicationContext);
        verify(mockDao, times(1)).renewDataInsert(eq_array);

    }


    @Test // OK ran single
    public void dropEarthquakeListTable_test() {
        repository.dropEarthquakeListTable();
        verify(mockDao, times(1)).dropEarthquakeListTable();
    }



    // Generate a dummy eq list
    private List<Earthquake> generateEqList(int quantity){
        List<Earthquake> earthquakes= new ArrayList<>();
        Date today = new Date();
        Long today_long = DateConverter.fromDate(today);

        for(int i=0; i<quantity; i++){
            earthquakes.add(new Earthquake(2.0,"Nowhere",today_long,
                    "www.google.it",1.0,1.0,10.0, 100));
        }
        return earthquakes;
    }



}
