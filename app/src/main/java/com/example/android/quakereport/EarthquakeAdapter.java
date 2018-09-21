package com.example.android.quakereport;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * {@link EarthquakeAdapter} It's an {@link ArrayAdapter} which can provide a list of object {@link Earthquake}
 * based from a data source
 */

public class EarthquakeAdapter extends ArrayAdapter<Earthquake> {

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
        magnitudeView.setText(Double.toString(currentEartquakeItem.getMagnitude()));

        TextView locationView = (TextView)itemView.findViewById(R.id.locationText);
        locationView.setText(currentEartquakeItem.getLocation());

        /** display date formatted using {@link formatDateFromMsec} **/
        TextView dateView = (TextView)itemView.findViewById(R.id.dateText);
        dateView.setText(formatDateFromMsec(currentEartquakeItem.getTime()));

        /** display time formatted using {@link formatTimeFromMsec} **/
        TextView timeView = (TextView)itemView.findViewById(R.id.timeText);
        timeView.setText(formatTimeFromMsec(currentEartquakeItem.getTime()));

        return itemView;

    }


    public String formatDateFromMsec(long dateMillisec){
        // Date
        Date date = new Date(dateMillisec);
        System.out.println("date : "+date.toString());

        // Format Date
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM DD, yyyy");
        return dateFormatter.format(date);
    }


    public String formatTimeFromMsec(long dateMillisec){
        // Time
        Date time = new Date(dateMillisec);
        System.out.println("time : "+time.toString());

        // Format Time
        SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");
        return timeFormatter.format(time);
    }

}
