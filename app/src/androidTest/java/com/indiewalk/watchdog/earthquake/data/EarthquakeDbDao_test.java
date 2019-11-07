package com.indiewalk.watchdog.earthquake.data;

import android.arch.persistence.room.Room;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;


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

    }

    @Test
    public void loadAllNoLiveData_test() throws Exception {

    }

    @Test
    public void loadAll_orderby_desc_mag_test() throws Exception {

    }

    @Test
    public void loadAll_orderby_asc_mag_test() throws Exception {

    }


    @Test
    public void loadAll_orderby_min_mag_test() throws Exception {

    }


    @Test
    public void loadAll_orderby_most_recent_test() throws Exception {

    }


    @Test
    public void loadAll_orderby_oldest_test() throws Exception {

    }


    @Test
    public void loadAll_orderby_nearest_test() throws Exception {

    }


    @Test
    public void loadAll_orderby_furthest_test() throws Exception {

    }



    @Test
    public void insertEarthquake_test() throws Exception {

    }

    @Test
    public void renewDataInsert_test() throws Exception {

    }



    @Test
    public void updatedEqDistanceFromUser_test() throws Exception {

    }

}
