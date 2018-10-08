
package com.example.android.quakereport;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
// Loader lib stuff
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EarthquakeActivity extends AppCompatActivity  implements LoaderCallbacks<List<Earthquake>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    private List<Earthquake> earthquakes;

    //Progress bar
    private ProgressBar loadingInProgress;

    // Empty view
    private TextView emptyListText;

    // ListView Adapter
    private EarthquakeAdapter adapter;

    /** URL to query the USGS dataset for earthquake information */
    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=6&limit=10";

    // Constant  id for loader which retrieve data from remote source (not necessary there is only it!)
    private static final int EARTHQUAKE_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // Find a reference to the {@link ListView} in the layout : using listView because it has only tens of
        // entry, otherwise RecycleView would be better
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new {@link EarthquakeAdapter} of {@link Earthquakes} objects
        adapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        // Set the adapter on the {@link ListView} so the list can be populated in the user interface
        earthquakeListView.setAdapter(adapter);


        // set progress bar
        loadingInProgress = findViewById(R.id.loading_spinner);

        // set Empty View in case of List empty
        emptyListText = findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(emptyListText);
        emptyListText.setText(R.string.searching);



        // Click on item
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // get url data
                Earthquake earthquake = earthquakes.get(position);
                String url = earthquake.getUrl();
                Log.i("setOnItemClickListener", "onItemClick: "+url);

                // Open the related url page of the eq clicked
                Uri webpage = Uri.parse(url);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(Intent.createChooser(webIntent, "Open details"));
            }
        });




        // check connection
        // reference to connection manager
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // network status retrieving
        NetworkInfo netinfo = connManager.getActiveNetworkInfo();

        if(netinfo != null && netinfo.isConnected()){
            // LoaderManager reference
            LoaderManager loaderManager = getLoaderManager();
            // Init loader : id above, bundle = null , this= current activity for LoaderCallbacks
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        }else{

            // hide progress bar
            loadingInProgress.setVisibility(View.GONE);
            emptyListText.setText(R.string.no_internet_connection);

        }

    }



    @Override
    public Loader<List<Earthquake>> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG, "onCreateLoader: Create a new Loader");
        // create a new loader for the url
        return new EarthquakeAsyncLoader(this, USGS_REQUEST_URL );
    }


    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakesReturnedByLoader) {
        Log.i(LOG_TAG, "onLoadFinished: Loader return back with data");

        // hide progress bar
        loadingInProgress.setVisibility(View.GONE);

        // Set empty state text to display "No earthquakes found."
        emptyListText.setText(R.string.no_earthquakes);

        // clear the adapater of previous data
        adapter.clear();

        // update UI when loader finishes its job
        if (setEartquakesList(earthquakesReturnedByLoader)==true) {
            adapter.addAll(earthquakes);
        } else {
            Log.i(LOG_TAG, "Problem with earthquake list, is empty. Check the request. ");
        }


    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        Log.i(LOG_TAG, "onLoaderReset: Reset Loader previous data");
        // reset loader to clean up previous data
        adapter.clear();
    }



    /**
     * Used by onLoadFinished to populate the ArrayList fetched
     * @param earthquakes
     * @return true/false
     */
    protected boolean setEartquakesList(List<Earthquake> earthquakes){
        if (  (earthquakes != null) && (earthquakes.isEmpty() == false)  ){
            this.earthquakes = earthquakes;
            return true;
        }else{
            Log.i(LOG_TAG, "The earthquake list is empty. Check the request. ");
            Toast.makeText(this, "The earthquake list is empty. Check the request. ", Toast.LENGTH_LONG).show();
            return false;
        }
    }



}