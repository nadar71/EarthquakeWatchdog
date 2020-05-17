package com.indiewalk.watchdog.earthquake.UI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
// import android.support.v7.preference.PreferenceFragmentCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.util.ConsentSDK;

public class SettingSimpleActivity extends AppCompatActivity {

    public static final String TAG = SettingSimpleActivity.class.getName();

    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_simple);

        /* ADMOB DELETED
        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(ConsentSDK.getAdRequest(SettingSimpleActivity.this));
         */


    }

    public static class EarthquakePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

        private ConsentSDK consentSDK = null;


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // show and keep updated the preferences
            addPreferencesFromResource(R.xml.settings_simple_main);

            // get preference Screen reference
            PreferenceScreen preferenceScreen = getPreferenceManager()
                    .createPreferenceScreen(getActivity());

            // bind prefs on changes
            Preference orderBy = findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderBy);

            Preference distanceUnit = findPreference(getString(R.string.settings_distance_unit_by_key));
            bindPreferenceSummaryToValue(distanceUnit);

            Preference minMagnitude = findPreference(getString(R.string.settings_min_magnitude_key));
            bindPreferenceSummaryToValue(minMagnitude);


            Preference dateFilter = findPreference(getString(R.string.settings_date_filter_key));
            bindPreferenceSummaryToValue(dateFilter);

            /*
            Preference manualLoc = findPreference(getString(R.string.manual_Localization_key));
            bindPreferenceSummaryToValue(manualLoc);
            */

            Preference gdprConsentBtn = findPreference(getString(R.string.gdpr_btn_key));
            Preference faqBtn         = findPreference(getString(R.string.faq_btn_key));

            /* ADMOB DELETED
            // Initialize ConsentSDK
            initConsentSDK(getActivity());

            // Checking the status of the user
            if(ConsentSDK.isUserLocationWithinEea(getActivity())) {
                String choice = ConsentSDK.isConsentPersonalized(getActivity())? "Personalize": "Non-Personalize";
                Log.i(TAG, "onCreate: consent choice : "+choice);

                gdprConsentBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // Check Consent SDK:Request the consent without callback
                        // consentSDK.requestConsent(null); to get the result of the consent
                        consentSDK.requestConsent(new ConsentSDK.ConsentStatusCallback() {
                            @Override
                            public void onResult(boolean isRequestLocationInEeaOrUnknown, int isConsentPersonalized) {
                                String choice = "";
                                switch (isConsentPersonalized) {
                                    case 0:
                                        choice = "Non-Personalize";
                                        break;
                                    case 1:
                                        choice = "Personalized";
                                        break;
                                    case -1:
                                        choice = "Error occurred";
                                }
                                Log.i(TAG, "onCreate: consent choice : "+choice);
                            }

                        });

                        return true;
                    }
                });

            } else {
                preferenceScreen.removePreference(gdprConsentBtn);
            }
             */
            preferenceScreen.removePreference(gdprConsentBtn);



            faqBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent showEqOnMap = new Intent(getActivity(), FaqActivity.class);
                    startActivity(showEqOnMap);
                    return true;
                }
            });

        }


        /**
         * -----------------------------------------------------------------------------------------
         * Bind prefs text shown below label on prefs changes
         * @param preference
         * -----------------------------------------------------------------------------------------
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



        /**
         * -----------------------------------------------------------------------------------------
         * Init consent
         * @param context
         * -----------------------------------------------------------------------------------------
         */
        private void initConsentSDK(Context context) {
            // Initialize ConsentSDK
            consentSDK = new ConsentSDK.Builder(context)
                    .addTestDeviceId("7DC1A1E8AEAD7908E42271D4B68FB270") // Add your test device id "Remove addTestDeviceId on production!"
                    .addCustomLogTag("gdpr_TAG") // Add custom tag default: ID_LOG
                    .addPrivacyPolicy("http://www.indie-walkabout.eu/privacy-policy-app") // Add your privacy policy url
                    .addPublisherId("pub-8846176967909254") // Add your admob publisher id
                    .build();
        }


    } // Fragment









}
