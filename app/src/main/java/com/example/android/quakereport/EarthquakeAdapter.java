package com.example.android.quakereport;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.time.format.DateTimeFormatter; only for api 26, using Date class before java 8 for retrocompat
import java.util.ArrayList;

/**
 * {@link EarthquakeAdapter} It's an {@link ArrayAdapter} which can provide a list of object {@link Earthquake}
 * based from a data source
 */

public class EarthquakeAdapter extends ArrayAdapter<Earthquake> {

    private String primaryLocation;
    private String locationOffset = "Near the";
    private double magnitude;
    private int magnitudeColor;


    /**
     * Main constructor
     * @param context
     * @param earthquakes
     */
    public EarthquakeAdapter(Activity context, ArrayList<Earthquake> earthquakes){
        super(context,0,earthquakes);  // * 2nd param to 0 because NOT populating simple TextView

    }


    /**
     * Provides a view for an AdapterView
     * @param position    : position in the list of the item
     * @param convertView : the recycle view item to be populated
     * @param parent      : parent ViewGroup used for inflation of this view
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View itemView = convertView;

        // inflate the item layout if not null
        if (itemView == null){
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item,parent,false);
        }

        // get the item in the current position
        Earthquake currentEartquakeItem = getItem(position);

        // set the data in the different view
        TextView magnitudeView = (TextView)itemView.findViewById(R.id.magnitudeText);
        magnitude = currentEartquakeItem.getMagnitude();
        magnitudeView.setText(formatMagnitude(magnitude));

        // set proper color for magnitude
        GradientDrawable magnitudeCircle = (GradientDrawable) magnitudeView.getBackground();
        // get and set the proper color for the proper magnitude value
        magnitudeColor = getMagnitudeColor(magnitude);
        magnitudeCircle.setColor(magnitudeColor);


        /** display locations formatted using {@link extractLocations} **/
        extractLocations(currentEartquakeItem.getLocation());

        TextView locationOffsetView = (TextView)itemView.findViewById(R.id.locationOffsetText);
        locationOffsetView.setText(locationOffset);

        TextView primaryLocationView = (TextView)itemView.findViewById(R.id.primaryLocationText);
        primaryLocationView.setText(primaryLocation );



        /** display date formatted using {@link formatDateFromMsec} **/
        TextView dateView = (TextView)itemView.findViewById(R.id.dateText);
        dateView.setText(formatDateFromMsec(currentEartquakeItem.getTime()));

        /** display time formatted using {@link formatTimeFromMsec} **/
        TextView timeView = (TextView)itemView.findViewById(R.id.timeText);
        timeView.setText(formatTimeFromMsec(currentEartquakeItem.getTime()));

        return itemView;

    }


    /**
     * Format date in a specific way and millisec  format
     * @param dateMillisec
     * @return
     */
    public String formatDateFromMsec(long dateMillisec){
        // Date
        Date date = new Date(dateMillisec);
        System.out.println("date : "+date.toString());

        // Format Date
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM DD, yyyy");
        return dateFormatter.format(date);
    }


    /**
     * Format time in a specific way and millisec  format
     * @param dateMillisec
     * @return
     */
    public String formatTimeFromMsec(long dateMillisec){
        // Time
        Date time = new Date(dateMillisec);
        System.out.println("time : "+time.toString());

        // Format Time
        SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");
        return timeFormatter.format(time);
    }

    /**
     * Split string  location into  offsetLocation and primaryLocation
     * @param location
     */
    public void extractLocations(String location){
        // Check if location contains string "of".
        // In case yes, store the substring before "of" in offsetLocation, and the part after in primaryLocation
        // On the contrary, put location in primaryLocation
        if (location.contains("of")) {
            String[] splitResult = location.split("of");
            locationOffset  = splitResult[0] + "of";
            primaryLocation = splitResult[1];
        } else {
            locationOffset = "Near the";
            primaryLocation = location;
        }
    }

    /**
     * Convert magnitude to format 0.0 and in string type
     * @param mag
     * @return
     */
    public String formatMagnitude(double mag){
        DecimalFormat formatter = new DecimalFormat("0.0");
        return formatter.format(mag);
    }


    /**
     * Return specific color value for specific magnitude values
     * @param magnitude
     * @return
     */
    public int getMagnitudeColor(double magnitude){
        int mag = (int) magnitude;
        Log.i("getMagnitudeColor", "Color: "+mag);
        switch(mag){
            case 1 :
            case 0 :
                return ContextCompat.getColor(getContext(),R.color.magnitude1);
            case 2 : return ContextCompat.getColor(getContext(),R.color.magnitude2);
            case 3 : return ContextCompat.getColor(getContext(),R.color.magnitude3);
            case 4 : return ContextCompat.getColor(getContext(),R.color.magnitude4);
            case 5 : return ContextCompat.getColor(getContext(),R.color.magnitude5);
            case 6 : return ContextCompat.getColor(getContext(),R.color.magnitude6);
            case 7 : return ContextCompat.getColor(getContext(),R.color.magnitude7);
            case 8 : return ContextCompat.getColor(getContext(),R.color.magnitude8);
            case 9 : return ContextCompat.getColor(getContext(),R.color.magnitude9);
            case 10 : return ContextCompat.getColor(getContext(),R.color.magnitude10plus);
            default : break;
        }
        return -1;
    }





}
