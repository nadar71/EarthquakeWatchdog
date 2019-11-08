package com.indiewalk.watchdog.earthquake.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.indiewalk.watchdog.earthquake.util.MyUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/*
EarthquakeDbDao_test #123
Running the test, it ends with :
java.lang.NullPointerException: Attempt to invoke interface method 'java.lang.Object
java.util.List.get(int)' on a null object reference

Maybe depending of not using androidx, it's about the Obvserver class for sure.
 */


@RunWith(AndroidJUnit4.class)
public class EarthquakeDbDao_test {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private EarthquakeDatabase earthquakeDatabase;
    private EarthquakeDbDao earthquakeDbDao;


    @Before
    public void createDb() {
        earthquakeDatabase = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().getContext(),
                EarthquakeDatabase.class).build();
        earthquakeDbDao = earthquakeDatabase.earthquakeDbDao();

    }

    @After
    public void closeDb() throws IOException {
        earthquakeDatabase.close();
    }


    @Test
    public void loadAll_test() throws Exception {
        earthquakeDbDao.insertEarthquake(createEarthquake());
        earthquakeDbDao.insertEarthquake(createEarthquake());

        LiveData<List<Earthquake>> eartquakes = earthquakeDbDao.loadAll();

        Observer<List<Earthquake>> observer = eqs -> assertEquals(2, eqs.size());
        eartquakes.observeForever(observer);
        List<Earthquake> currentEqs = eartquakes.getValue();

        assertEquals(currentEqs.size(),2);
    }



    @Test
    public void loadAllNoLiveData_test() throws Exception {
        earthquakeDbDao.insertEarthquake(createEarthquake());
        earthquakeDbDao.insertEarthquake(createEarthquake());
        List<Earthquake> eartquakes = earthquakeDbDao.loadAllNoLiveData();
        assertEquals(eartquakes.size(),2);
    }



    @Test
    public void loadAll_orderby_desc_mag_test() throws Exception {
        Date today = new Date();
        Long today_long = DateConverter.INSTANCE.fromDate(today);

        earthquakeDbDao.insertEarthquake(new Earthquake(6.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));
        earthquakeDbDao.insertEarthquake(new Earthquake(4.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));
        earthquakeDbDao.insertEarthquake(new Earthquake(2.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));

        LiveData<List<Earthquake>> eartquakes = earthquakeDbDao.loadAll_orderby_desc_mag(6.0);

        Observer<List<Earthquake>> observer = eqs -> assertEquals(3, eqs.size());
        eartquakes.observeForever(observer);
        List<Earthquake> currentEqs = eartquakes.getValue();

        assertEquals(currentEqs.size(),3);
        assertTrue(currentEqs.get(0).getMagnitude() > currentEqs.get(1).getMagnitude());
        assertTrue(currentEqs.get(1).getMagnitude() > currentEqs.get(2).getMagnitude());

    }

    @Test
    public void loadAll_orderby_asc_mag_test() throws Exception {
        Date today = new Date();
        Long today_long = DateConverter.INSTANCE.fromDate(today);

        earthquakeDbDao.insertEarthquake(new Earthquake(6.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));
        earthquakeDbDao.insertEarthquake(new Earthquake(4.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));
        earthquakeDbDao.insertEarthquake(new Earthquake(2.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));

        LiveData<List<Earthquake>> eartquakes = earthquakeDbDao.loadAll_orderby_asc_mag(1.0);

        Observer<List<Earthquake>> observer = eqs -> assertEquals(3, eqs.size());
        eartquakes.observeForever(observer);
        List<Earthquake> currentEqs = eartquakes.getValue();

        assertEquals(currentEqs.size(),3);
        assertTrue(currentEqs.get(0).getMagnitude() < currentEqs.get(1).getMagnitude());
        assertTrue(currentEqs.get(1).getMagnitude() < currentEqs.get(2).getMagnitude());
    }


    @Test
    public void loadAll_orderby_min_mag_test() throws Exception {
        Date today = new Date();
        Long today_long = DateConverter.INSTANCE.fromDate(today);

        earthquakeDbDao.insertEarthquake(new Earthquake(6.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));
        earthquakeDbDao.insertEarthquake(new Earthquake(4.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));
        earthquakeDbDao.insertEarthquake(new Earthquake(2.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));

        LiveData<List<Earthquake>> eartquakes = earthquakeDbDao.loadAll_orderby_min_mag(4.0);

        Observer<List<Earthquake>> observer = eqs -> assertEquals(2, eqs.size());
        eartquakes.observeForever(observer);
        List<Earthquake> currentEqs = eartquakes.getValue();

        assertEquals(currentEqs.size(),2);
        assertEquals(java.util.Optional.ofNullable(currentEqs.get(0).getMagnitude()), 6.0);
        assertEquals(java.util.Optional.ofNullable(currentEqs.get(1).getMagnitude()), 4.0);

    }


    @Test
    public void loadAll_orderby_most_recent_test() throws Exception {
        Date today = new Date();
        Date yesterday      = MyUtil.INSTANCE.addDays(today,-1);
        Date daysAgo_2       = MyUtil.INSTANCE.addDays(today,-2);
        Date daysAgo_3       = MyUtil.INSTANCE.addDays(today,-3);

        Long today_long = DateConverter.INSTANCE.fromDate(today);
        Long daysAgo_2_long = DateConverter.INSTANCE.fromDate(daysAgo_2);
        Long daysAgo_3_long  = DateConverter.INSTANCE.fromDate(daysAgo_3);

        earthquakeDbDao.insertEarthquake(new Earthquake(6.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));
        earthquakeDbDao.insertEarthquake(new Earthquake(4.0,"Nowhere",daysAgo_2_long,
                "www.google.it",1.0,1.0,10.0, 100));
        earthquakeDbDao.insertEarthquake(new Earthquake(2.0,"Nowhere",daysAgo_3_long,
                "www.google.it",1.0,1.0,10.0, 100));

        LiveData<List<Earthquake>> eartquakes = earthquakeDbDao.loadAll_orderby_most_recent(4.0);

        Observer<List<Earthquake>> observer = eqs -> assertEquals(2, eqs.size());
        eartquakes.observeForever(observer);
        List<Earthquake> currentEqs = eartquakes.getValue();

        assertEquals(currentEqs.size(),2);
        assertTrue(currentEqs.get(0).getTimeInMillisec() > currentEqs.get(1).getTimeInMillisec());
    }


    @Test
    public void loadAll_orderby_oldest_test() throws Exception {
        Date today = new Date();
        Date yesterday      = MyUtil.INSTANCE.addDays(today,-1);
        Date daysAgo_2       = MyUtil.INSTANCE.addDays(today,-2);
        Date daysAgo_3       = MyUtil.INSTANCE.addDays(today,-3);

        Long today_long = DateConverter.INSTANCE.fromDate(today);
        Long daysAgo_2_long = DateConverter.INSTANCE.fromDate(daysAgo_2);
        Long daysAgo_3_long  = DateConverter.INSTANCE.fromDate(daysAgo_3);

        earthquakeDbDao.insertEarthquake(new Earthquake(6.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));
        earthquakeDbDao.insertEarthquake(new Earthquake(4.0,"Nowhere",daysAgo_2_long,
                "www.google.it",1.0,1.0,10.0, 100));
        earthquakeDbDao.insertEarthquake(new Earthquake(2.0,"Nowhere",daysAgo_3_long,
                "www.google.it",1.0,1.0,10.0, 100));

        LiveData<List<Earthquake>> eartquakes = earthquakeDbDao.loadAll_orderby_oldest(4.0);

        Observer<List<Earthquake>> observer = eqs -> assertEquals(2, eqs.size());
        eartquakes.observeForever(observer);
        List<Earthquake> currentEqs = eartquakes.getValue();

        assertEquals(currentEqs.size(),2);
        assertTrue(currentEqs.get(0).getTimeInMillisec() < currentEqs.get(1).getTimeInMillisec());

    }


    @Test
    public void loadAll_orderby_nearest_test() throws Exception {
        Date today = new Date();
        Long today_long = DateConverter.INSTANCE.fromDate(today);

        earthquakeDbDao.insertEarthquake(new Earthquake(6.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));
        earthquakeDbDao.insertEarthquake(new Earthquake(4.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 1000));
        earthquakeDbDao.insertEarthquake(new Earthquake(2.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 10000));

        LiveData<List<Earthquake>> eartquakes = earthquakeDbDao.loadAll_orderby_nearest(1.0);

        Observer<List<Earthquake>> observer = eqs -> assertEquals(3, eqs.size());
        eartquakes.observeForever(observer);
        List<Earthquake> currentEqs = eartquakes.getValue();

        assertEquals(currentEqs.size(),3);
        assertTrue(currentEqs.get(0).getUserDistance() < currentEqs.get(1).getUserDistance());
        assertTrue(currentEqs.get(1).getUserDistance() < currentEqs.get(2).getUserDistance());
    }


    @Test
    public void loadAll_orderby_furthest_test() throws Exception {
        Date today = new Date();
        Long today_long = DateConverter.INSTANCE.fromDate(today);

        earthquakeDbDao.insertEarthquake(new Earthquake(6.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));
        earthquakeDbDao.insertEarthquake(new Earthquake(4.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 1000));
        earthquakeDbDao.insertEarthquake(new Earthquake(2.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 10000));

        LiveData<List<Earthquake>> eartquakes = earthquakeDbDao.loadAll_orderby_furthest(1.0);

        Observer<List<Earthquake>> observer = eqs -> assertEquals(3, eqs.size());
        eartquakes.observeForever(observer);
        List<Earthquake> currentEqs = eartquakes.getValue();

        assertEquals(currentEqs.size(),3);
        assertTrue(currentEqs.get(0).getUserDistance() > currentEqs.get(1).getUserDistance());
        assertTrue(currentEqs.get(1).getUserDistance() > currentEqs.get(2).getUserDistance());
    }



    @Test
    public void insertEarthquake_test() throws Exception {
        Date today = new Date();
        Long today_long = DateConverter.INSTANCE.fromDate(today);

        earthquakeDbDao.insertEarthquake(new Earthquake(6.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100));

        LiveData<List<Earthquake>> eartquakes = earthquakeDbDao.loadAll_orderby_furthest(1.0);

        Observer<List<Earthquake>> observer = eqs -> assertEquals(1, eqs.size());
    }


    @Test
    public void renewDataInsert_test() throws Exception {
        Date today = new Date();
        Long today_long = DateConverter.INSTANCE.fromDate(today);
        Earthquake eq_01 = new Earthquake(6.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100);
        Earthquake eq_02 = new Earthquake(4.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 1000);
        Earthquake eq_03 = new Earthquake(2.0,"Nowhere",today_long,
                        "www.google.it",1.0,1.0,10.0, 10000);

        List<Earthquake> eqs = new ArrayList<>();
        eqs.add(eq_01);
        eqs.add(eq_02);
        eqs.add(eq_03);

        earthquakeDbDao.insertEarthquake(eq_01);
        earthquakeDbDao.insertEarthquake(eq_02);
        earthquakeDbDao.insertEarthquake(eq_03);

        LiveData<List<Earthquake>> eartquakes = earthquakeDbDao.loadAll_orderby_furthest(1.0);
        Observer<List<Earthquake>> observer_01 = eaqs -> assertEquals(3, eaqs.size());

        Earthquake[] eqs_array = new Earthquake[eqs.size()];
        eqs_array = eqs.toArray(eqs_array);

        earthquakeDbDao.renewDataInsert(eqs_array);
        eartquakes = earthquakeDbDao.loadAll_orderby_furthest(1.0);
        Observer<List<Earthquake>> observer_02 = eaqs -> assertEquals(3, eaqs.size());

    }



    @Test
    public void updatedEqDistanceFromUser_test() throws Exception {
        Date today = new Date();
        Long today_long = DateConverter.INSTANCE.fromDate(today);
        Earthquake eq_01 = new Earthquake(6.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 100);
        Earthquake eq_02 = new Earthquake(4.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 1000);
        Earthquake eq_03 = new Earthquake(2.0,"Nowhere",today_long,
                "www.google.it",1.0,1.0,10.0, 10000);


        earthquakeDbDao.insertEarthquake(eq_01);
        earthquakeDbDao.insertEarthquake(eq_02);
        earthquakeDbDao.insertEarthquake(eq_03);

        LiveData<List<Earthquake>> eartquakes = earthquakeDbDao.loadAll_orderby_furthest(1.0);
        Observer<List<Earthquake>> observer_01 = eaqs -> assertEquals(3, eaqs.size());
        eartquakes.observeForever(observer_01);
        List<Earthquake> currentEqs = eartquakes.getValue();

        assertEquals(currentEqs.size(),3);
        assertEquals(currentEqs.get(0).getUserDistance(),100);
        assertEquals(currentEqs.get(1).getUserDistance(),1000);
        assertEquals(currentEqs.get(2).getUserDistance(),10000);

        int user_id_01  = currentEqs.get(0).getId();
        int user_id_02  = currentEqs.get(1).getId();
        int user_id_03  = currentEqs.get(2).getId();

        earthquakeDbDao.updatedEqDistanceFromUser(user_id_01,200);
        earthquakeDbDao.updatedEqDistanceFromUser(user_id_02,2000);
        earthquakeDbDao.updatedEqDistanceFromUser(user_id_03,20000);

        assertEquals(currentEqs.get(0).getUserDistance(),200);
        assertEquals(currentEqs.get(1).getUserDistance(),2000);
        assertEquals(currentEqs.get(2).getUserDistance(),20000);

    }


    // Generate a dummy eq
    private Earthquake createEarthquake(){
        Date today = new Date();
        Long today_long = DateConverter.INSTANCE.fromDate(today);
        return new Earthquake(2.0,"Nowhere",today_long,
                    "www.google.it",1.0,1.0,10.0, 100);

    }

}
