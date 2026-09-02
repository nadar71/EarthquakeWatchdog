package com.indiewalk.watchdog.earthquake.feat_ads.di

import com.indiewalk.watchdog.earthquake.feat_ads.data.ConsentClientFactory
import com.indiewalk.watchdog.earthquake.feat_ads.data.MobileAdsSdkInitializer
import com.indiewalk.watchdog.earthquake.feat_ads.data.UmpConsentClientFactory
import com.indiewalk.watchdog.earthquake.feat_ads.domain.AdsSdkInitializer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AdsModule {
    @Binds
    abstract fun bindAdsSdkInitializer(implementation: MobileAdsSdkInitializer): AdsSdkInitializer

    @Binds
    abstract fun bindConsentClientFactory(implementation: UmpConsentClientFactory): ConsentClientFactory
}
