package com.indiewalk.watchdog.earthquake.unused;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.indiewalk.watchdog.earthquake.R;
import com.indiewalk.watchdog.earthquake.UI.MainActivity;
import com.indiewalk.watchdog.earthquake.util.ConsentSDK;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        // Initialize a dummy banner using the default test banner id provided by google
        // to get the device id from logcat using 'Ads' tag
        // ConsentSDK.initDummyBanner(this);

        // Initialize ConsentSDK
        ConsentSDK consentSDK = new ConsentSDK.Builder(this)
                .addTestDeviceId("7DC1A1E8AEAD7908E42271D4B68FB270") // redminote 5 // Add your test device id "Remove addTestDeviceId on production!"
                // .addTestDeviceId("9978A5F791A259430A0156313ED9C6A2")
                .addCustomLogTag("gdpr_TAG") // Add custom tag default: ID_LOG
                .addPrivacyPolicy("http://www.indie-walkabout.eu/privacy-policy-app") // Add your privacy policy url
                .addPublisherId("pub-8846176967909254") // Add your admob publisher id
                .build();

        // To check the consent and to move to MainActivity after everything is fine :).
        consentSDK.checkConsent(new ConsentSDK.ConsentCallback() {
            @Override
            public void onResult(boolean isRequestLocationInEeaOrUnknown) {
                Log.i("gdpr_TAG", "onResult: isRequestLocationInEeaOrUnknown : "+isRequestLocationInEeaOrUnknown);
                goToMain();
            }
        });

        // Loading indicator
        loadingHandler();
    }

    // Go to MainActivity
    private void goToMain() {
        // Wait few seconds just to show my stunning loading indication, you like it right :P.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Go to main after the consent is done.
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    /**
     * Some stuff to tell that your app is loading and it's not lagging.
     */
    // Loading indicator handler
    private void loadingHandler() {
        final TextView loadingTxt = findViewById(R.id.loadingTxt);
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Loading Txt
                if(loadingTxt.getText().length() > 10) {
                    loadingTxt.setText("Loading ");
                } else {
                    loadingTxt.setText(loadingTxt.getText()+".");
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 500);
    }
}
