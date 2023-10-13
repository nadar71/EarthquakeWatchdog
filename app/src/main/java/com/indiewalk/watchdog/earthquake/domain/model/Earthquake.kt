package com.indiewalk.watchdog.earthquake.domain.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


// Represent a single eq event.
@Entity(tableName = "EARTHQUAKE_LIST")
class Earthquake {


    // Getter and setter
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    // eq's magnitude
    private var magnitude: Double = 0.toDouble()

    // eq location
    var location: String? = null

    // eq date and time of occurence
    // @ColumnInfo(name = "time_ms")
    var timeInMillisec: Long = 0

    // eq url page for details
    var url: String? = null

    // longitude
    var longitude: Double = 0.toDouble()

    // latitude
    var latitude: Double = 0.toDouble()

    // depth
    var depth: Double = 0.toDouble()

    //equake distance from user
    var userDistance: Int = 0


    // Create a new plain Earthquake
    @Ignore
    constructor(
        magnitude: Double, location: String, timeInMillisec: Long, url: String,
        longitude: Double, latitude: Double, depth: Double, userDistance: Int
    ) {
        this.magnitude = magnitude
        this.location = location
        this.timeInMillisec = timeInMillisec
        this.url = url
        this.longitude = longitude
        this.latitude = latitude
        this.depth = depth
        this.userDistance = userDistance
    }


    // Create a new plain Earthquake for db insert
    constructor(
        id: Int, magnitude: Double, location: String, timeInMillisec: Long, url: String,
        longitude: Double, latitude: Double, depth: Double, userDistance: Int
    ) {
        this.id = id
        this.magnitude = magnitude
        this.location = location
        this.timeInMillisec = timeInMillisec
        this.url = url
        this.longitude = longitude
        this.latitude = latitude
        this.depth = depth
        this.userDistance = userDistance
    }


    fun getMagnitude(): Double {
        return magnitude
    }

    fun setMagnitude(magnitude: Double) {
        this.magnitude = magnitude
    }

}
