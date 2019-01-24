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

    // longitude
    private double longitude;

    // latitude
    private double latitude;

    // depth
    private double depth;

    //equake distance from user
    private int userDistance;


    /**
     * ---------------------------------------------------------------------------------------------
     * Create a new plain Earthquake
     * ---------------------------------------------------------------------------------------------
     * @param magnitude
     * @param location
     * @param timeInMillisec
     * @param url
     * @param longitude
     * @param latitude
     * @param depth
     * @param userDistance
     */
    @Ignore
    public Earthquake(double magnitude, String location, long timeInMillisec, String url,
                      double longitude, double latitude, double depth, int userDistance) {
        this.magnitude      = magnitude;
        this.location       = location;
        this.timeInMillisec = timeInMillisec;
        this.url            = url;
        this.longitude      = longitude;
        this.latitude       = latitude;
        this.depth          = depth;
        this.userDistance   = userDistance;
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
     * @param longitude
     * @param latitude
     * @param depth
     * @param userDistance
     */
    public Earthquake(int id, double magnitude, String location, long timeInMillisec, String url,
                      double longitude, double latitude, double depth, int userDistance) {
        this.id             = id;
        this.magnitude      = magnitude;
        this.location       = location;
        this.timeInMillisec = timeInMillisec;
        this.url            = url;
        this.longitude      = longitude;
        this.latitude       = latitude;
        this.depth          = depth;
        this.userDistance   = userDistance;
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


    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }


    public int getUserDistance() {
        return userDistance;
    }

    public void setUserDistance(int userDistance) {
        this.userDistance = userDistance;
    }

}
