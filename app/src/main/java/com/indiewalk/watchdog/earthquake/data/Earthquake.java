package com.indiewalk.watchdog.earthquake.data;

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

    // eq url page for details
    private String url;




    /**
     * Constructor
     *
     * @param magnitude
     * @param location
     * @param timeInMillisec
     */
    public Earthquake(double magnitude, String location, long timeInMillisec, String url) {
        this.magnitude = magnitude;
        this.location = location;
        this.timeInMillisec = timeInMillisec;
        this.url = url;
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

    /*
    Return date
     */
    public String getUrl() { return url; }

}
