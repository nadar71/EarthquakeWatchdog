package com.indiewalk.watchdog.earthquake.UI;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.SingletonProvider;
import com.indiewalk.watchdog.earthquake.data.Earthquake;
import com.indiewalk.watchdog.earthquake.data.EarthquakeRepository;
import com.indiewalk.watchdog.earthquake.UI.MainActivityEarthquakesList;
import com.indiewalk.watchdog.earthquake.util.MyUtil;

import java.text.DecimalFormat;
import java.util.List;

public class EarthquakeListAdapter extends RecyclerView.Adapter<EarthquakeListAdapter.EarthquakeViewRowHolder> {

    private final String TAG = EarthquakeListAdapter.class.getName();

    private EarthquakeRepository repository;

    // Handle item clicks
    final private ItemClickListener eqItemClickListener;

    private List<Earthquake> earthquakesEntries;
    private Context          context;

    private String primaryLocation;

    private String locationOffset ;

    private double magnitude;
    private int    magnitudeColor;

    private String dist_unit;


    /**
     * ----------------------------------------------------------------------------------
     * Constructor :
     * @param context  the current Context
     * @param listener the ItemClickListener
     * ----------------------------------------------------------------------------------
     */
    public EarthquakeListAdapter(Context context, ItemClickListener listener) {
        this.context             = context;
        eqItemClickListener      = listener;

        locationOffset = context.getResources().getString(R.string.locationOffset_label);

        // set preferred distance unit
        checkPreferences();

        repository = ((SingletonProvider) SingletonProvider.getsContext()).getRepositoryWithDataSource();
    }


    /**
     * ----------------------------------------------------------------------------------
     * Inflate list's each view/row layout.
     * @return new EarthquakeViewRowHolder
     * ----------------------------------------------------------------------------------
     */
    @NonNull
    @Override
    public EarthquakeViewRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_ii, parent, false);

        return new EarthquakeViewRowHolder(view);
    }


    /**
     * ----------------------------------------------------------------------------------
     * Connect each earthquake ViewHolder obj with their data
     * @param holder   ViewHolder to bind Cursor data to
     * @param position data position in Cursor
     * ----------------------------------------------------------------------------------
     */
    @Override
    public void onBindViewHolder(@NonNull EarthquakeViewRowHolder holder, int position) {

        // get the item in the current position
        Earthquake currentEartquakeItem = earthquakesEntries.get(position);

        magnitude = currentEartquakeItem.getMagnitude();
        holder.magnitudeView.setText(formatMagnitude(magnitude));

        // set proper image color for magnitude
        holder.magnitudeView.setBackground(MyUtil.getMagnitudeImg(magnitude, context));

        // display locations formatted using {@link extractLocations}
        extractLocations(currentEartquakeItem.getLocation());
        holder.locationOffsetView.setText(locationOffset);
        holder.primaryLocationView.setText(primaryLocation );

        // display date formatted using {@link formatDateFromMsec}
        holder.dateView.setText(MyUtil.formatDateFromMsec(currentEartquakeItem.getTimeInMillisec()));

        // display time formatted using {@link formatTimeFromMsec}
        holder.timeView.setText(MyUtil.formatTimeFromMsec(currentEartquakeItem.getTimeInMillisec()));

        // set distance label based on user location type
        boolean check = checkPreferences();
        if (check == true) { // custom location
            holder.distanceFromUser.setText("            "+currentEartquakeItem.getUserDistance()+ " " + dist_unit);
            holder.distanceFromUser_label.setText(context.getString(R.string.distance_from_user_location));
        } else if (check == false){
            holder.distanceFromUser.setText(currentEartquakeItem.getUserDistance()+ " " + dist_unit);
            holder.distanceFromUser_label.setText(context.getString(R.string.distance_from_default_location));
        }

    }


    @Override
    public int getItemCount() {
        if (earthquakesEntries == null) {
            return 0;
        }
        return earthquakesEntries.size();

    }



    /**
     * ----------------------------------------------------------------------------------
     * Set data for RecycleView as earthquakesEntries list.
     * Used by calling activity to init/update the adapter
     * @param earthquakesEntries
     * ----------------------------------------------------------------------------------
     */
    public void setEarthquakesEntries(List<Earthquake> earthquakesEntries) {
        // if  (this.earthquakesEntries != null) this.earthquakesEntries.clear();
        this.earthquakesEntries = earthquakesEntries;

        //data changed, refresh the view : notify the related observers
        notifyDataSetChanged();
    }



    /**
     * ----------------------------------------------------------------------------------
     * Reset adapter earthquakesEntries list.
     * ----------------------------------------------------------------------------------
     */
    public void resetEarthquakesEntries() {
        if  (earthquakesEntries != null) earthquakesEntries.clear();
    }

    // ----------------------------------------------------------------------------------
    // Implemented in calling class if needed
    // ----------------------------------------------------------------------------------
    public interface ItemClickListener {
        void onItemClickListener(View v, int position);
    }


    /**
     * ----------------------------------------------------------------------------------
     * Return a eq item in list at defined position
     * ----------------------------------------------------------------------------------
     */
    public Earthquake getEqItemAtPosition(int position){
        return earthquakesEntries.get(position);
    }


    /**
     * ----------------------------------------------------------------------------------
     * Get all the eq items list
     * ----------------------------------------------------------------------------------
     */
    public List<Earthquake> getEarthquakesEntries(){
        return earthquakesEntries;
    }



    class EarthquakeViewRowHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView magnitudeView;
        TextView locationOffsetView;
        TextView primaryLocationView;
        TextView dateView;
        TextView timeView;
        TextView distanceFromUser;
        TextView distanceFromUser_label;


        // EarthquakeViewRowHolder Constructor
        // @param itemView view inflated in onCreateViewHolder
        public EarthquakeViewRowHolder(View itemView) {
            super(itemView);

            magnitudeView          = itemView.findViewById(R.id.magnitudeText);
            locationOffsetView     = itemView.findViewById(R.id.locationOffsetText);
            primaryLocationView    = itemView.findViewById(R.id.primaryLocationText);
            dateView               = itemView.findViewById(R.id.dateText);
            timeView               = itemView.findViewById(R.id.timeText);
            distanceFromUser       = itemView.findViewById(R.id.distanceFromMe_tv);
            distanceFromUser_label = itemView.findViewById(R.id.distanceFromMeLabel_tv);

            // row click listener
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            // int elementId = earthquakesEntries.get(getAdapterPosition()).getId();
            eqItemClickListener.onItemClickListener(view, this.getLayoutPosition());
        }

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
                Double.toString(MainActivityEarthquakesList.DEFAULT_LAT));
        String lng_s = sharedPreferences.getString(context.getString(R.string.device_lng),
                Double.toString(MainActivityEarthquakesList.DEFAULT_LNG));

        // if there is user location different from default location
        if ( (!lat_s.equals(Double.toString(MainActivityEarthquakesList.DEFAULT_LAT))) &&
                (!lng_s.equals(Double.toString(MainActivityEarthquakesList.DEFAULT_LNG))) ) {
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
