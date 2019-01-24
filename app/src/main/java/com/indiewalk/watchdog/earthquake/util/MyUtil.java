package com.indiewalk.watchdog.earthquake.util;

public class MyUtil {


    /**
     * Convert degree angle in radiant
     * @param deg_angle
     * @return
     */
    public static double fromDegreeToRadiant(double deg_angle){
        return deg_angle*Math.PI/180;
    }


    /**
     * Returning the distance between 2 points on a sphere throught the Haversine formula
     * @return
     */
    public static int haversineDistanceCalc(double p1Lat, double p2Lat, double p1Lng, double p2Lng ){
      int    R    = 6378137; // Earth’s mean radius in meter
      double dLat = fromDegreeToRadiant(p2Lat - p1Lat);
      double dLng = fromDegreeToRadiant(p2Lng - p1Lng);
      double a    = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(fromDegreeToRadiant(p1Lat)) *
                    Math.cos(fromDegreeToRadiant(p2Lat)) *
                    Math.sin(dLng / 2) * Math.sin(dLng / 2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      double d = R * c;
      return (int) d/1000; // returns the distance in Km
    }

}
