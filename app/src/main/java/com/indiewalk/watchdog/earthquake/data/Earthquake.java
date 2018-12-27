package com.indiewalk.watchdog.earthquake.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;


import java.util.Date;

/**
 * {@link  public class Earthquake } Represent a single eq event.
 * Properties :
 * - magnitude
 * - location
 * - date
 */
@Entity(tableName = "EARTHQUAKE_LIST")
public class Earthquake {



    @PrimaryKey(autoGenerate = true)
    private int id;

    // eq's magnitude
    private double magnitude;

    // eq location
    private String location;

    // eq date and time of occurence
    private long timeInMillisec;

    // eq url page for details
    private String url;




    /**
     * ---------------------------------------------------------------------------------------------
     * Create a new plain Earthquake
     * ---------------------------------------------------------------------------------------------
     * @param magnitude
     * @param location
     * @param timeInMillisec
     */
    @Ignore
    public Earthquake(double magnitude, String location, long timeInMillisec, String url) {
        this.magnitude = magnitude;
        this.location = location;
        this.timeInMillisec = timeInMillisec;
        this.url = url;
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Create a new plain Earthquake for db insert
     * ---------------------------------------------------------------------------------------------
     * @param id
     * @param magnitude
     * @param location
     * @param timeInMillisec
     * @param url
     */
    public Earthquake(int id, double magnitude, String location, long timeInMillisec, String url) {
        this.id = id;
        this.magnitude = magnitude;
        this.location = location;
        this.timeInMillisec = timeInMillisec;
        this.url = url;
    }


    // Getter and setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getTimeInMillisec() {
        return timeInMillisec;
    }

    public void setTimeInMillisec(long timeInMillisec) {
        this.timeInMillisec = timeInMillisec;
    }

    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }
}
