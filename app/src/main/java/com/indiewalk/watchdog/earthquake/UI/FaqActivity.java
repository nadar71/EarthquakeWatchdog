package com.indiewalk.watchdog.earthquake.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;

import com.ayoubfletcher.consentsdk.ConsentSDK;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.model.Marker;
import com.indiewalk.watchdog.earthquake.R;

public class FaqActivity extends AppCompatActivity {

    // admob banner ref
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);


        // -----------------------------------------------------------------------------------------
        // Init admob
        // Sample AdMob banner ID:         ca-app-pub-3940256099942544~3347511713
        // THIS APP REAL AdMob banner ID:  ca-app-pub-8846176967909254~9979565057
        // -----------------------------------------------------------------------------------------
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        // load ads banner
        mAdView = findViewById(R.id.adView);

        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        mAdView.loadAd(ConsentSDK.getAdRequest(FaqActivity.this));

        // back btn
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(getString(R.string.title_activity_faq));

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
