package com.indiewalk.watchdog.earthquake.UI;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.data.Earthquake;
import com.indiewalk.watchdog.earthquake.util.MyUtil;

import java.text.DecimalFormat;
//import java.time.format.DateTimeFormatter; only for api 26, using Date class before java 8 for retrocompat
import java.util.ArrayList;

/**
 * ---------------------------------------------------------------------------------------------
 * {@link EarthquakeAdapter} It's an {@link ArrayAdapter} which can provide a list of
 * object {@link Earthquake} based from a data source
 * ---------------------------------------------------------------------------------------------
 */

public class EarthquakeAdapter extends ArrayAdapter<Earthquake> {

    private final  static String TAG = EarthquakeAdapter.class.getSimpleName();

    private Context context;

    private String primaryLocation;

    private String locationOffset ;

    private double magnitude;
    private int    magnitudeColor;

    private String dist_unit;


    /**
     * ---------------------------------------------------------------------------------------------
     * Main constructor
     * @param context
     * @param earthquakes
     * ---------------------------------------------------------------------------------------------
     */
    public EarthquakeAdapter(Activity context, ArrayList<Earthquake> earthquakes){
        super(context,0,earthquakes);  // * 2nd param to 0 because NOT populating simple TextView
        this.context = context;

        locationOffset = context.getResources().getString(R.string.locationOffset_label);

        // set preferred distance unit
        checkPreferences();
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Provides a view for an AdapterView
     * @param position    : position in the list of the item
     * @param convertView : the recycle view item to be populated
     * @param parent      : parent ViewGroup used for inflation of this view
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View itemView = convertView;

        // inflate the item layout if not null
        if (itemView == null){
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_ii,parent,false);
        }

        // get the item in the current position
        Earthquake currentEartquakeItem = getItem(position);

        // set the data in the different view
        TextView magnitudeView = (TextView)itemView.findViewById(R.id.magnitudeText);
        magnitude = currentEartquakeItem.getMagnitude();
        magnitudeView.setText(formatMagnitude(magnitude));


        // set proper color for magnitude
        /*
        GradientDrawable magnitudeCircle = (GradientDrawable) magnitudeView.getBackground();
        magnitudeColor = MyUtil.getMagnitudeColor(magnitude,context);
        magnitudeCircle.setColor(magnitudeColor);
        */

        // set proper image color for magnitude
        magnitudeView.setBackground(MyUtil.getMagnitudeImg(magnitude, context));

        // display locations formatted using {@link extractLocations}
        extractLocations(currentEartquakeItem.getLocation());

        TextView locationOffsetView = (TextView)itemView.findViewById(R.id.locationOffsetText);
        locationOffsetView.setText(locationOffset);

        TextView primaryLocationView = (TextView)itemView.findViewById(R.id.primaryLocationText);
        primaryLocationView.setText(primaryLocation );

        // display date formatted using {@link formatDateFromMsec}
        TextView dateView = (TextView)itemView.findViewById(R.id.dateText);

        dateView.setText(MyUtil.formatDateFromMsec(currentEartquakeItem.getTimeInMillisec()));

        // display time formatted using {@link formatTimeFromMsec}
        TextView timeView = (TextView)itemView.findViewById(R.id.timeText);
        timeView.setText(MyUtil.formatTimeFromMsec(currentEartquakeItem.getTimeInMillisec()));

        // display eq distance from user location or custom location
        TextView distanceFromUser = (TextView)itemView.findViewById(R.id.distanceFromMe_tv);


        // set distance label based on user location type
        boolean check = checkPreferences();
        TextView distanceFromUser_label = (TextView)itemView.findViewById(R.id.distanceFromMeLabel_tv);
        if (check == true) { // custom location
            distanceFromUser.setText("            "+currentEartquakeItem.getUserDistance()+ " " + dist_unit);
            distanceFromUser_label.setText(context.getString(R.string.distance_from_user_location));
        } else if (check == false){
            distanceFromUser.setText(currentEartquakeItem.getUserDistance()+ " " + dist_unit);
            distanceFromUser_label.setText(context.getString(R.string.distance_from_default_location));
        }


        return itemView;

    }



    /**
     * ---------------------------------------------------------------------------------------------
     * Check for distance unit preference, and if a user location is different from the default one
     * ---------------------------------------------------------------------------------------------
     */
    private boolean checkPreferences() {
        // init shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        // set distance unit choosen
        dist_unit = sharedPreferences.getString(context.getString(R.string.settings_distance_unit_by_key),
                context.getString(R.string.settings_distance_unit_by_default));

        Log.i(TAG, "EarthquakeAdapter : dist unit : "+ dist_unit);

        String lat_s = sharedPreferences.getString(context.getString(R.string.device_lat),
                Double.toString(MainActivity.DEFAULT_LAT));
        String lng_s = sharedPreferences.getString(context.getString(R.string.device_lng),
                Double.toString(MainActivity.DEFAULT_LNG));

        // if there is user location different from default location
        if ( (!lat_s.equals(Double.toString(MainActivity.DEFAULT_LAT))) &&
                (!lng_s.equals(Double.toString(MainActivity.DEFAULT_LNG))) ) {
            return true; // custom location
        } else {
            return false; // default location, Google inc. Mountain view
        }

    }





    /**
     * ---------------------------------------------------------------------------------------------
     * Split string  location into  offsetLocation and primaryLocation
     * @param location
     * ---------------------------------------------------------------------------------------------
     */
    public void extractLocations(String location){
        // Check if location contains string "of".
        // In case yes, store the substring before "of" in offsetLocation, and the part after in primaryLocation
        // On the contrary, put location in primaryLocation
        if (location.contains("of")) {  // case of e.g. "85km SSW of xxxx"
            String[] splitResult = location.split("of");
            locationOffset  = splitResult[0]+ "of";
            // convert place distance to desidered distance unit
            locationOffset = convertPlaceDist(locationOffset);

            primaryLocation = splitResult[1];
        } else {
            locationOffset = context.getResources().getString(R.string.locationOffset_label);
            primaryLocation = location;
        }
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Convert place distance to desidered distance unit
     * @param loc
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public String convertPlaceDist(String loc) {
        // convert distance in distance unit as preference
        String distance = MyUtil.returnDigit(loc);
        int   dist_i = -1;
        float dist_f = -1;

        // check the case the distance is as int or as float
        try {
            dist_i = Integer.parseInt(distance);
        }catch (NumberFormatException e) {
            try {
                dist_f = Float.parseFloat(distance);
            } catch (NumberFormatException f) {
                dist_i = 0;
            }
        }

        if (dist_i > 0 ){
            dist_i = (int) MyUtil.fromKmToMiles((double) dist_i);
        }

        if (dist_f > 0 ){
            dist_i = (int) MyUtil.fromKmToMiles((double) dist_f);
        }

        // get rid of the original distance unit
        loc  = MyUtil.returnChar(loc)
                .replaceAll("Km","")
                .replaceAll("km","")
                .replaceAll("KM","")
                .replaceAll("Mi","")
                .replaceAll("mi","")
                .replaceAll("MI","");

        // add the preferred distance unit
        //distance = distWithUnit(dist_i);
        String finalString = distance + " " + dist_unit + loc;
        return finalString;
    }




    /**
     * ---------------------------------------------------------------------------------------------
     * Convert magnitude to format 0.0 and in string type
     * @param mag
     * @return
     * ---------------------------------------------------------------------------------------------
     */
    public String formatMagnitude(double mag){
        DecimalFormat formatter = new DecimalFormat("0.0");
        return formatter.format(mag);
    }








}
