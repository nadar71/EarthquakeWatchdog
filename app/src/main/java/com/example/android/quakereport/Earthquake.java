package com.example.android.quakereport;

import java.util.Date;

/**
 * {@link  public class Earthquake } Represent a single eq event.
 * Properties :
 * - magnitude
 * - location
 * - date
 */
public class Earthquake {

    // eq's magnitude
    private double magnitude;

    // eq location
    private String location;

    // eq date and time of occurence
    private long timeInMillisec;




    /**
     * Constructor
     *
     * @param magnitude
     * @param location
     * @param timeInMillisec
     */
    public Earthquake(double magnitude, String location, long timeInMillisec) {
        this.magnitude = magnitude;
        this.location = location;
        this.timeInMillisec = timeInMillisec;


    }


    /*
    Return magnitude
     */
    public Double getMagnitude() {
        return magnitude;
    }

    /*
    Return location
     */
    public String getLocation() {
        return location;
    }

    /*
    Return date
     */
    public long getTime() { return timeInMillisec; }


}
