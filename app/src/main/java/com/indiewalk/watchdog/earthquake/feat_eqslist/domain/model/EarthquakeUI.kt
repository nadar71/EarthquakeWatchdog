package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model

// Represent a single eq event for UI presentation.
data class EarthquakeUI(
    var magnitude: Double? = 0.0,
    var location: String? = null,
    var occurenceDateTime: Long? = 0,  // date and time of occurrence in millisec
    var urlDetails: String? = null,   // url to details
    var longitude: Double? = 0.0,
    var latitude: Double? = 0.0,
    var depth: Double? = 0.0,
    var distanceFromUser: Int? = null    // distance from user location



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
