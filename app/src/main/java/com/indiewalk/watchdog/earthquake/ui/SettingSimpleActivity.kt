package com.indiewalk.watchdog.earthquake.ui

import android.content.Context
import android.content.Intent
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
// import android.support.v7.preference.PreferenceFragmentCompat;
import android.os.Bundle
import android.util.Log

import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.util.ConsentSDK
import kotlinx.android.synthetic.main.activity_setting_simple.*

class SettingSimpleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_simple)

        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        mAdView!!.loadAd(ConsentSDK.getAdRequest(this@SettingSimpleActivity))

    }

    class EarthquakePreferenceFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {

        private var consentSDK: ConsentSDK? = null


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // show and keep update the preferences
            addPreferencesFromResource(R.xml.settings_simple_main)

            // get preference Screen reference
            val preferenceScreen = preferenceManager
                    .createPreferenceScreen(activity)

            // bind prefs on changes
            val orderBy = findPreference(getString(R.string.settings_order_by_key))
            bindPreferenceSummaryToValue(orderBy)

            val distanceUnit = findPreference(getString(R.string.settings_distance_unit_by_key))
            bindPreferenceSummaryToValue(distanceUnit)

            val minMagnitude = findPreference(getString(R.string.settings_min_magnitude_key))
            bindPreferenceSummaryToValue(minMagnitude)

            // TODO : must delete this part
            /*
            Preference maxEquakesNum = findPreference(getString(R.string.settings_max_equakes_key));
            bindPreferenceSummaryToValue(maxEquakesNum);
            */

            val dateFilter = findPreference(getString(R.string.settings_date_filter_key))
            bindPreferenceSummaryToValue(dateFilter)

            /*
            Preference manualLoc = findPreference(getString(R.string.manual_Localization_key));
            bindPreferenceSummaryToValue(manualLoc);
            */

            // gdprConsentBtn = findViewById(R.id.gdpr_withdraw_btn);
            val gdprConsentBtn = findPreference(getString(R.string.gdpr_btn_key))
            val faqBtn = findPreference(getString(R.string.faq_btn_key))

            // Initialize ConsentSDK
            initConsentSDK(activity)

            // Checking the status of the user
            if (ConsentSDK.isUserLocationWithinEea(activity)) {
                val choice = if (ConsentSDK.isConsentPersonalized(activity)) "Personalize" else "Non-Personalize"
                Log.i(TAG, "onCreate: consent choice : $choice")

                gdprConsentBtn.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    // Check Consent SDK
                    // Request the consent without callback
                    // consentSDK.requestConsent(null);
                    //To get the result of the consent
                    consentSDK!!.requestConsent(object : ConsentSDK.ConsentStatusCallback() {
                        override fun onResult(isRequestLocationInEeaOrUnknown: Boolean, isConsentPersonalized: Int) {
                            var choice = ""
                            when (isConsentPersonalized) {
                                0 -> choice = "Non-Personalize"
                                1 -> choice = "Personalized"
                                -1 -> choice = "Error occurred"
                            }
                            Log.i(TAG, "onCreate: consent choice : $choice")
                        }

                    })

                    true
                }

            } else {
                preferenceScreen.removePreference(gdprConsentBtn)
            }


            // Faq button
            faqBtn.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val showEqOnMap = Intent(activity, FaqActivity::class.java)
                startActivity(showEqOnMap)
                true
            }

        }


        /**
         * -----------------------------------------------------------------------------------------
         * Bind prefs text shown below label on prefs changes
         * @param preference
         * -----------------------------------------------------------------------------------------
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = this  // bind

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.context)

            // get new value to use for replacing old
            val sPreference = sharedPreferences.getString(preference.key, "")

            // callback invocation on preference param
            onPreferenceChange(preference, sPreference!!)


        }

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            val sValue = newValue.toString()

            if (preference is ListPreference) {
                val prefindex = preference.findIndexOfValue(sValue)
                if (prefindex >= 0) {
                    val labels = preference.entries
                    preference.setSummary(labels[prefindex])
                }

            } else {

                preference.summary = sValue

            }
            return true
        }


        /**
         * -----------------------------------------------------------------------------------------
         * Initialize consent
         * @param context
         * -----------------------------------------------------------------------------------------
         */
        private fun initConsentSDK(context: Context) {
            // Initialize ConsentSDK
            consentSDK = ConsentSDK.Builder(context)
                    .addTestDeviceId("7DC1A1E8AEAD7908E42271D4B68FB270") // Add your test device id "Remove addTestDeviceId on production!"
                    .addCustomLogTag("gdpr_TAG") // Add custom tag default: ID_LOG
                    .addPrivacyPolicy("http://www.indie-walkabout.eu/privacy-policy-app") // Add your privacy policy url
                    .addPublisherId("pub-8846176967909254") // Add your admob publisher id
                    .build()
        }


    } // Fragment

    companion object {

        val TAG = SettingSimpleActivity::class.java.name
    }


}
