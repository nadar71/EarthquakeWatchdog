/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class EarthquakeActivity extends AppCompatActivity {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);


        // Get the data from a static json string in {@link QueryUtils}
        final ArrayList<Earthquake> earthquakes = QueryUtils.extractEarthquakes();


        // Find a reference to the {@link ListView} in the layout : using listView because it has only tens of
        // entry, otherwise RecycleView would be better
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new {@link EarthquakeAdapter} of {@link Earthquakes} objects
        EarthquakeAdapter adapter = new EarthquakeAdapter(this, earthquakes);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(adapter);

        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get url data
                // 1st method : don't like it
                /*
                Earthquake earthquake = earthquakes.get(position);
                String url = earthquake.getUrl();
                */
                // 2nd method : better
                Earthquake earthquake = earthquakes.get(position);

                String url = earthquake.getUrl();
                Log.i("setOnItemClickListener", "onItemClick: "+url);
                // Open the related url page of the eq clicked
                Uri webpage = Uri.parse(url);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(Intent.createChooser(webIntent, "Open details"));
            }
        });
    }
}
