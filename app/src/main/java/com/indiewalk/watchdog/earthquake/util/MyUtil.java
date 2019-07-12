package com.indiewalk.watchdog.earthquake.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.indiewalk.watchdog.earthquake.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class MyUtil {


    // URL to query the USGS dataset for earthquake information
    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query";
    // "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=6&limit=10"; // debug


    /**
     * ---------------------------------------------------------------------------------------------
     * Compose a query url starting from preferences parameters
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public static String composeQueryUrl(String dateFilter){
        Uri rootUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder builder = rootUri.buildUpon();

        builder.appendQueryParameter("format","geojson");

        // commented, it creates only problem, will be substituted with user preferred time range
        // builder.appendQueryParameter("limit",numEquakes);

        // calculate 30-days ago date and set as start date
        // String aMonthAgo = MyUtil.oldDate(30).toString();
        // builder.appendQueryParameter("starttime",aMonthAgo);

        int offset = Integer.parseInt(dateFilter);
        String rangeAgo = MyUtil.oldDate(offset).toString();
        builder.appendQueryParameter("starttime",rangeAgo);


        /*
        builder.appendQueryParameter("minmag",minMagnitude); // TODO : delete

        if (!orderBy.equals(getString(R.string.settings_order_by_nearest_value))
                && !orderBy.equals(getString(R.string.settings_order_by_farthest_value)) ){
            orderBy = getString(R.string.settings_order_by_default);
            builder.appendQueryParameter("orderby", orderBy);     // TODO : delete
        }
        */


        return  builder.toString();
    }

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
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy");
        String dateFormatted = dateFormatter.format(date);
        return dateFormatted;
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




    /**
     * ---------------------------------------------------------------------------------------------
     * Hide nav bar total
     * @param activity
     * ---------------------------------------------------------------------------------------------
     */
    public static void hideNavBar(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return Past date by daysOffset
     * ---------------------------------------------------------------------------------------------
     */
    public static String oldDate(int daysOffset){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd ");
        Calendar calReturn = Calendar.getInstance();
        calReturn.add(Calendar.DATE, -daysOffset);
        String oldDate = dateFormat.format(calReturn.getTime());
        return oldDate;
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Restart current activity
     * ---------------------------------------------------------------------------------------------
     */
    public static void restartActivity(Activity activity){
        Intent mIntent = activity.getIntent();
        activity.finish();
        activity.startActivity(mIntent);
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Return specific color value for specific magnitude values
     * @param magnitude
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public static int getMagnitudeColor(double magnitude, Context context){
        int mag = (int) magnitude;
        Log.i("getMagnitudeColor", "Color: "+mag);
        switch(mag){
            case 1 :
            case 0 :
                return ContextCompat.getColor(context,R.color.magnitude1);
            case 2 : return ContextCompat.getColor(context,R.color.magnitude2);
            case 3 : return ContextCompat.getColor(context,R.color.magnitude3);
            case 4 : return ContextCompat.getColor(context,R.color.magnitude4);
            case 5 : return ContextCompat.getColor(context,R.color.magnitude5);
            case 6 : return ContextCompat.getColor(context,R.color.magnitude6);
            case 7 : return ContextCompat.getColor(context,R.color.magnitude7);
            case 8 : return ContextCompat.getColor(context,R.color.magnitude8);
            case 9 : return ContextCompat.getColor(context,R.color.magnitude9);
            case 10 : return ContextCompat.getColor(context,R.color.magnitude10plus);
            default : break;
        }
        return -1;
    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Return specific vector image for specific magnitude values
     * @param magnitude
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public static Drawable getMagnitudeImg(double magnitude, Context context){
        int mag = (int) magnitude;
        Log.i("getMagnitudeColor", "Color: "+mag);
        switch(mag){
            case 1 :
            case 0 :
                return ContextCompat.getDrawable(context,R.drawable.ic_earthquake_pointer_1);
            case 2 :  return ContextCompat.getDrawable(context,R.drawable.ic_earthquake_pointer_2);
            case 3 :  return ContextCompat.getDrawable(context,R.drawable.ic_earthquake_pointer_3);
            case 4 :  return ContextCompat.getDrawable(context,R.drawable.ic_earthquake_pointer_4);
            case 5 :  return ContextCompat.getDrawable(context,R.drawable.ic_earthquake_pointer_5);
            case 6 :  return ContextCompat.getDrawable(context,R.drawable.ic_earthquake_pointer_6);
            case 7 :  return ContextCompat.getDrawable(context,R.drawable.ic_earthquake_pointer_7);
            case 8 :  return ContextCompat.getDrawable(context,R.drawable.ic_earthquake_pointer_8);
            case 9 :  return ContextCompat.getDrawable(context,R.drawable.ic_earthquake_pointer_9);
            case 10 : return ContextCompat.getDrawable(context,R.drawable.ic_earthquake_pointer_10);
            default : break;
        }
        return ContextCompat.getDrawable(context,R.drawable.ic_earthquake_pointer);
    }



}
