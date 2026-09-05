package com.indiewalk.watchdog.earthquake.feat_ads.data

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.indiewalk.watchdog.earthquake.feat_ads.domain.AdsSdkInitializer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileAdsSdkInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) : AdsSdkInitializer {
    override fun initialize() {
        MobileAds.initialize(context)
    }
}
