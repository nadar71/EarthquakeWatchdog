import org.gradle.kotlin.dsl.implementation
import java.util.Properties
import java.io.FileInputStream

plugins { // plugin application
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.indiewalk.watchdog.earthquake"
    compileSdk = 36


    // Load keystore properties
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        // This is the existing release config - it's correct.
        create("release") {
            keyAlias = keystoreProperties.getProperty("release_keyAlias")
            keyPassword = keystoreProperties.getProperty("release_keyPassword")
            storeFile = if (keystoreProperties.getProperty("release_storeFile") != null) {
                rootProject.file(keystoreProperties.getProperty("release_storeFile"))
            } else {
                null
            }
            storePassword = keystoreProperties.getProperty("release_storePassword")
        }

    }


    defaultConfig {
        applicationId = "com.indiewalk.watchdog.earthquake"
        minSdk = 26
        targetSdk = 35
        versionCode = 11
        versionName = "3.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = ""
    }

    buildTypes {
        release{
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["MAPS_API_KEY"] = keystoreProperties.getProperty("MAPS_API_KEY", "")
        }

        debug{
            signingConfig = signingConfigs.getByName("debug")
            manifestPlaceholders["MAPS_API_KEY"] = keystoreProperties.getProperty("MAPS_API_KEY", "")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.ui.test.android)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx.v251)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)


    // compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.foundation)
    implementation(libs.foundation.layout)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material)
    implementation("androidx.navigation3:navigation3-runtime:1.0.0")
    implementation("androidx.navigation3:navigation3-ui:1.0.0")
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.foundation.android)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    // Accompanist lib for compose integration
    // implementation(libs.accompanist.pager)
    implementation(libs.accompanist.permissions)

    // Core library desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // gson
    implementation(libs.gson)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.androidx.room.testing)

    // coil
    implementation(libs.coil.compose)
    implementation(libs.coil.compose.v210)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp.urlconnection)

    // Multidex
    implementation(libs.multidex)

    // Google Maps and Location dependencies
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation (libs.kotlinx.coroutines.play.services)
    implementation(libs.android.maps.utils)
    implementation(libs.maps.ktx)
    implementation(libs.maps.utils.ktx)

    // Jetpack Compose integration for Google Maps
    implementation(libs.maps.compose)

    // Ad mob
    implementation(libs.playservices.ads)
    implementation(libs.user.messaging.platform)

    // Unity
    implementation(libs.unity.ads)

    // Preference
    implementation(libs.androidx.preference.ktx)

    // Ktor Client - Android
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.client.logging)

    // DataStore (preferences)
    implementation (libs.androidx.datastore.preferences)

    // testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}


/*apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-kapt'


configurations {
    ktlint
}

android {
    compileSdkVersion 34
    defaultConfig {
        applicationId "com.indiewalk.watchdog.earthquake"
        minSdkVersion 19
        targetSdkVersion 34
        versionCode 10
        versionName "2.0"
        testInstrumentationRunner 'androidx.test.runner.AndroidJUnitRunner'
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled true
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }

    productFlavors {
        production {
            dimension "EarthquakeWatchdog"
            resValue "string", "app_name", "EarthquakeWatchdog"
        }

        myTesting {
            dimension "EarthquakeWatchdog"
            resValue "string", "app_name", "EarthquakeWatchdog Test"
            applicationIdSuffix ".testing"
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11
    }

    lintOptions {
        abortOnError false
    }

    dataBinding {
        enabled true
    }

    viewBinding {
        enabled true
    }

    testOptions {
        unitTests {
            includeAndroidResources = true
            unitTests.returnDefaultValues = true
        }
    }
    namespace 'com.indiewalk.watchdog.earthquake'
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])

    implementation "androidx.appcompat:appcompat:$appcompat_vers"
    implementation "androidx.constraintlayout:constraintlayout:$constraintlayout_vers"
    implementation "androidx.legacy:legacy-support-core-utils:$legacy_support_vers"

    // recyclerview
    implementation "androidx.recyclerview:recyclerview:$recyclerview_vers"

    // floating button
    implementation "com.google.android.material:material:$material_vers"

    // Google play services
    implementation "com.google.android.gms:play-services-maps:$gps_maps_vers"
    implementation "com.google.android.gms:play-services-location:$gps_location_vers"

    // Room
    implementation "androidx.room:room-runtime:$room_vers"
    kapt "androidx.room:room-compiler:$room_vers"
    implementation "androidx.room:room-ktx:$room_vers"
    // test helpers
    testImplementation "androidx.room:room-testing:$room_vers"

    // LiveData dependencies
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_vers"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_vers"
    implementation "androidx.lifecycle:lifecycle-extensions:$lifecycle_ext_vers"

    // firebase
    implementation "com.firebase:firebase-jobdispatcher:$jobdispatcher_vers"

    //  Ads through Firebase
    // implementation 'com.google.firebase:firebase-ads:17.1.1'

    // google gms
    implementation "com.google.android.gms:play-services-ads:$gps_ads_vers"

    // GDPR
    implementation "com.google.android.ads.consent:consent-library:$consent_library_vers"
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"

    // https://ktlint.github.io/#getting-started
    ktlint 'com.pinterest:ktlint:0.39.0'

    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version"

    // Compose with bom
    def composeBom = platform("androidx.compose:compose-bom:$compose_bom_vers")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation "androidx.compose.runtime:runtime"
    implementation "androidx.compose.ui:ui"
    implementation "androidx.compose.foundation:foundation"
    implementation "androidx.compose.foundation:foundation-layout"
    implementation "androidx.compose.material:material"
    implementation "androidx.compose.material3:material3"
    implementation "androidx.compose.material:material-icons-extended"
    implementation "androidx.compose.runtime:runtime-livedata"
    implementation "androidx.compose.ui:ui-tooling"
    implementation "androidx.compose.ui:ui-tooling-preview"

    implementation "androidx.constraintlayout:constraintlayout-compose:$compose_constraint"
    implementation "androidx.lifecycle:lifecycle-viewmodel-compose:$compose_viewmodel"

    // accompanist lib for compose integration
    implementation 'com.google.accompanist:accompanist-pager:0.22.0-rc'

    // Retrofit
    implementation "com.squareup.retrofit2:retrofit:$retrofit_version"
    implementation "com.squareup.retrofit2:converter-gson:$retrofit_version"
    implementation "com.squareup.okhttp3:okhttp:$okhttp_version"
    implementation "com.squareup.okhttp3:logging-interceptor:$okhttp_version"
    implementation "com.squareup.okhttp3:okhttp-urlconnection:$okhttp_version"

    // Dagger - Hilt
    implementation "com.google.dagger:hilt-android:$hilt_version"
    kapt "com.google.dagger:hilt-android-compiler:$hilt_version"
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    kapt "androidx.hilt:hilt-compiler:1.0.0"
    // Hilt Navigation Compose
    implementation "androidx.hilt:hilt-navigation-compose:$hilt_nav_compose_vers"


    // TESTING DEPENDENCIES ----------------------------------------------------------------
    // Testing code should not be included in the main code.
    // Once https://issuetracker.google.com/128612536 is fixed this can be fixed.
    implementation "androidx.test:core:1.5.0"

    //  AndroidX Test - local unit tests - JVM testing
    testImplementation "junit:junit:4.13.2"
    testImplementation "org.hamcrest:hamcrest-all:1.3"
    testImplementation "androidx.arch.core:core-testing:2.2.0"
    testImplementation 'androidx.test:runner:1.5.2'
    testImplementation "androidx.test:core-ktx:1.5.0"
    testImplementation "androidx.test.ext:junit:1.1.5"
    testImplementation "org.robolectric:robolectric:4.3.1"
    testImplementation 'org.mockito:mockito-core:2.25.0'
    testImplementation 'org.mockito:mockito-inline:2.13.0'
    testImplementation "io.mockk:mockk:1.9.3"



    // AndroidX Test - Instrumented testing
    androidTestImplementation "junit:junit:4.13.2"
    androidTestImplementation 'androidx.test:runner:1.5.2'
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
    androidTestImplementation "androidx.arch.core:core-testing:2.2.0"
    androidTestImplementation 'androidx.test:rules:1.5.0'



    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.test.espresso:espresso-intents:3.5.1'
    androidTestImplementation 'androidx.test.espresso:espresso-contrib:3.1.0', {
        exclude group: 'com.android.support', module: 'support-annotations'
        exclude group: 'com.android.support', module: 'support-v4'
        exclude group: 'com.android.support', module: 'design'
        exclude group: 'com.android.support', module: 'recyclerview-v7'
    }



}*/
