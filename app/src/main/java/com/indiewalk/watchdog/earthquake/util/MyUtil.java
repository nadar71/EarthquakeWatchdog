package com.indiewalk.watchdog.earthquake.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyUtil {


    /**
     * ---------------------------------------------------------------------------------------------
     * Convert degree angle in radiant
     * @param deg_angle
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public static double fromDegreeToRadiant(double deg_angle){
        return deg_angle*Math.PI/180;
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Returning the distance between 2 points on a sphere throught the Haversine formula
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public static double haversineDistanceCalc(double p1Lat, double p2Lat, double p1Lng, double p2Lng ){
      int    R    = 6378137; // Earth’s mean radius in meter
      double dLat = fromDegreeToRadiant(p2Lat - p1Lat);
      double dLng = fromDegreeToRadiant(p2Lng - p1Lng);
      double a    = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(fromDegreeToRadiant(p1Lat)) *
                    Math.cos(fromDegreeToRadiant(p2Lat)) *
                    Math.sin(dLng / 2) * Math.sin(dLng / 2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      double d = R * c;
      return  d/1000; // returns the distance in Km
    }


    /**
     * Convert km to miles
     * @param km
     * @return
     */
    public static double fromKmToMiles(double km){
        double MileInkm = 0.621371192;
        return km * MileInkm;
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Format date in a specific way and millisec  format
     * @param dateMillisec
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public static String formatDateFromMsec(long dateMillisec){
        // Date
        Date date = new Date(dateMillisec);
        System.out.println("date : "+date.toString());

        // Format Date
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM DD, yyyy");
        return dateFormatter.format(date);
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Format time in a specific way and millisec  format
     * @param dateMillisec
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public static String formatTimeFromMsec(long dateMillisec){
        // Time
        Date time = new Date(dateMillisec);
        System.out.println("time : "+time.toString());

        // Format Time
        SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");
        return timeFormatter.format(time);
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Extract only the digit with "." from a String
     * ---------------------------------------------------------------------------------------------
     */
    public static String returnDigit(String s){
        return s.replaceAll("[^0-9?!\\.]+", "");
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Extract only the char from a String
     * ---------------------------------------------------------------------------------------------
     */
    public static String returnChar(String s){
        return s.replaceAll("[0-9]+", "");
    }




    /**
     * ---------------------------------------------------------------------------------------------
     * Converting vector icon to bitmap one, to get used as marker icon (allow bitmap only)
     * @param context
     * @param vectorResourceId
     * @param tintColor
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public static BitmapDescriptor getBitmapFromVector(@NonNull Context context,
                                                       @DrawableRes int vectorResourceId,
                                                       @ColorInt int tintColor) {

        Drawable vectorDrawable = ResourcesCompat.getDrawable(
                context.getResources(), vectorResourceId, null);

        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, tintColor);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}
