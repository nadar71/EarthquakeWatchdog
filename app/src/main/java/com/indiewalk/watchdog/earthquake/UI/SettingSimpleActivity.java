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

import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.util.MyUtil;

public class SettingSimpleActivity extends AppCompatActivity {

    public static final String LOG_TAG = SettingSimpleActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_simple);
    }

    public static class EarthquakePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // show and keep update the preferences
            addPreferencesFromResource(R.xml.settings_simple_main);

            Preference orderBy = findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderBy);

            Preference distanceUnit = findPreference(getString(R.string.settings_distance_unit_by_key));
            bindPreferenceSummaryToValue(distanceUnit);

            // bind prefs on changes
            Preference minMagnitude = findPreference(getString(R.string.settings_min_magnitude_key));
            bindPreferenceSummaryToValue(minMagnitude);

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

            // callback invokation on preference param
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
