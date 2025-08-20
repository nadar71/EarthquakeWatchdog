package com.indiewalk.watchdog.earthquake.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Represent a single eq event.
@Entity(tableName = "EARTHQUAKES")
data class EarthquakeUI(


    // Getter and setter
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var magnitude: Double = 0.toDouble(),
    var location: String? = null,
    var occurenceDateTime: Long = 0,  // date and time of occurrence in millisec
    var urlDetails: String? = null,   // url to details
    var longitude: Double = 0.toDouble(),
    var latitude: Double = 0.toDouble(),
    var depth: Double = 0.toDouble(),
    var distanceFromUser: Int = 0    // distance from user location



    // Create a new plain Earthquake
    /*@Ignore
    constructor(
        magnitude: Double, location: String, timeInMillisec: Long, url: String,
        longitude: Double, latitude: Double, depth: Double, userDistance: Int
    ) {
        this.magnitude = magnitude
        this.location = location
        this.occurenceDateTime = timeInMillisec
        this.urlDetails = url
        this.longitude = longitude
        this.latitude = latitude
        this.depth = depth
        this.distanceFromUser = userDistance
    }


    // Create a new plain Earthquake for db insert
    constructor(
        id: Int, magnitude: Double, location: String, timeInMillisec: Long, url: String,
        longitude: Double, latitude: Double, depth: Double, userDistance: Int
    ) {
        this.id = id
        this.magnitude = magnitude
        this.location = location
        this.occurenceDateTime = timeInMillisec
        this.urlDetails = url
        this.longitude = longitude
        this.latitude = latitude
        this.depth = depth
        this.distanceFromUser = userDistance
    }*/




)
