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
    private Double magnitude;

    // eq location
    private String location;

    // eq date of occurence
    private Long date;


    /**
     * Constructor
     *
     * @param magnitude
     * @param location
     * @param date
     */
    public Earthquake(Double magnitude, String location, Long date) {
        this.magnitude = magnitude;
        this.location = location;
        this.date = date;

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
    public Long getDate() {
        return date;
    }


}
