package com.indiewalk.watchdog.earthquake.UI;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
// import android.support.v7.preference.PreferenceFragmentCompat;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.util.MyUtil;

public class SettingSimpleActivity extends AppCompatActivity {

    public static final String TAG = SettingSimpleActivity.class.getName();

    // admob banner ref
    private AdView mAdView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_simple);

        // -----------------------------------------------------------------------------------------
        // Init admob
        // Sample AdMob banner ID:         ca-app-pub-3940256099942544~3347511713
        // THIS APP REAL AdMob banner ID:  ca-app-pub-8846176967909254~9979565057
        // -----------------------------------------------------------------------------------------
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        // load ads banner
        mAdView = findViewById(R.id.adView);

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("7DC1A1E8AEAD7908E42271D4B68FB270")
                .build();
        mAdView.loadAd(adRequest);
    }

    public static class EarthquakePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // show and keep update the preferences
            addPreferencesFromResource(R.xml.settings_simple_main);

            // bind prefs on changes
            Preference orderBy = findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderBy);

            Preference distanceUnit = findPreference(getString(R.string.settings_distance_unit_by_key));
            bindPreferenceSummaryToValue(distanceUnit);

            Preference minMagnitude = findPreference(getString(R.string.settings_min_magnitude_key));
            bindPreferenceSummaryToValue(minMagnitude);

            Preference maxEquakesNum = findPreference(getString(R.string.settings_max_equakes_key));
            bindPreferenceSummaryToValue(maxEquakesNum);
/*
            Preference manualLoc = findPreference(getString(R.string.manual_Localization_key));
            bindPreferenceSummaryToValue(manualLoc);
*/

        }


        /**
         * Bind prefs text shown below label on prefs changes
         * @param preference
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);  // bind

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());

            // get new value to use for replacing old
            String sPreference = sharedPreferences.getString(preference.getKey(),"");

            // callback invocation on preference param
            onPreferenceChange(preference,sPreference);


        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String sValue = newValue.toString();

            if (preference instanceof ListPreference){
                ListPreference listPreference = (ListPreference) preference;
                int prefindex = ((ListPreference) preference).findIndexOfValue(sValue);
                if(prefindex >= 0){
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefindex]);
                }

            }else{

                preference.setSummary(sValue);

            }
            return true;
        }
    }
}
