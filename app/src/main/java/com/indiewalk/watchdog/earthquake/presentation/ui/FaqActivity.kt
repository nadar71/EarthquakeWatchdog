package com.indiewalk.watchdog.earthquake.presentation.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem

import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.util.ConsentSDK
import kotlinx.android.synthetic.main.activity_faq.*

class FaqActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        // load ads banner
        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        mAdView!!.loadAd(ConsentSDK.getAdRequest(this@FaqActivity))

        // back btn
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        supportActionBar!!.title = getString(R.string.faq_btn_title)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
