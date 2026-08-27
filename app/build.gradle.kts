import org.gradle.api.Action
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.kotlin.dsl.implementation
import org.gradle.kotlin.dsl.configure
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import java.util.Properties
import java.io.FileInputStream
import java.net.URI

val keystorePropertiesFile = providers.gradleProperty("releaseSecretsFile")
    .orNull
    ?.let(rootProject::file)
    ?: rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        FileInputStream(keystorePropertiesFile).use { load(it) }
    }
}
val firebaseConfigurationFile = project.file("google-services.json")
val hasFirebaseConfiguration = firebaseConfigurationFile.isFile

val requiredReleaseSecretNames = listOf(
    "release_keyAlias",
    "release_keyPassword",
    "release_storeFile",
    "release_storePassword",
    "MAPS_API_KEY_RELEASE",
    "ADMOB_APP_ID_RELEASE",
    "ADMOB_BANNER_ID_RELEASE",
    "PRIVACY_POLICY_URL_RELEASE"
)

fun releaseSecret(name: String): String? = sequenceOf(
    providers.gradleProperty(name).orNull,
    providers.environmentVariable(name).orNull,
    keystoreProperties.getProperty(name)
).firstOrNull { !it.isNullOrBlank() }

fun missingReleaseSecrets(): List<String> =
    requiredReleaseSecretNames.filter { releaseSecret(it).isNullOrBlank() }

fun validateProtectedReleaseConfiguration() {
    val appId = releaseSecret("ADMOB_APP_ID_RELEASE").orEmpty()
    val bannerId = releaseSecret("ADMOB_BANNER_ID_RELEASE").orEmpty()
    val privacyPolicyUrl = releaseSecret("PRIVACY_POLICY_URL_RELEASE").orEmpty()
    val testPublisher = "ca-app-pub-3940256099942544"

    check(Regex("ca-app-pub-[0-9]{16}~[0-9]{10}").matches(appId)) {
        "ADMOB_APP_ID_RELEASE has an unsupported format"
    }
    check(Regex("ca-app-pub-[0-9]{16}/[0-9]{10}").matches(bannerId)) {
        "ADMOB_BANNER_ID_RELEASE has an unsupported format"
    }
    check(!appId.startsWith(testPublisher) && !bannerId.startsWith(testPublisher)) {
        "Release AdMob configuration must not use Google test ids"
    }
    check(appId.substringBefore('~') == bannerId.substringBefore('/')) {
        "Release AdMob app and banner ids must use the same publisher"
    }

    val uri = runCatching { URI(privacyPolicyUrl) }.getOrNull()
    check(
        uri?.scheme == "https" &&
            !uri.host.isNullOrBlank() &&
            uri.userInfo == null &&
            uri.fragment == null
    ) {
        "PRIVACY_POLICY_URL_RELEASE must be a public HTTPS URL"
    }
}

fun validateReleaseTaskGraph() {
    val releasePackagingTaskNames = setOf(
        "assembleRelease",
        "bundleRelease",
        "minifyReleaseWithR8",
        "packageReleaseBundle",
        "signReleaseBundle"
    )

    gradle.taskGraph.whenReady(Action<TaskExecutionGraph> {
        val releasePackagingIsPlanned = allTasks.any { task ->
            task.project == project && task.name in releasePackagingTaskNames
        }

        if (releasePackagingIsPlanned) {
            val missing = missingReleaseSecrets()
            check(missing.isEmpty()) {
                "Missing required release secrets: ${missing.joinToString(", ")}"
            }
            check(hasFirebaseConfiguration) {
                "Missing required Firebase configuration: app/google-services.json. " +
                    "Provision it from the protected release environment before packaging a store build."
            }
            validateProtectedReleaseConfiguration()
        }
    })
}

plugins { // plugin application
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.androidx.baselineprofile)
}

if (hasFirebaseConfiguration) {
    pluginManager.apply("com.google.gms.google-services")
    pluginManager.apply("com.google.firebase.crashlytics")
}

validateReleaseTaskGraph()

android {
    namespace = "com.indiewalk.watchdog.earthquake"
    compileSdk = 36

    lint {
        abortOnError = true
        checkDependencies = true
        checkReleaseBuilds = true
        lintConfig = rootProject.file("config/lint/lint.xml")
    }

    signingConfigs {
        create("release") {
            keyAlias = releaseSecret("release_keyAlias")
            keyPassword = releaseSecret("release_keyPassword")
            storeFile = releaseSecret("release_storeFile")?.let(rootProject::file)
            storePassword = releaseSecret("release_storePassword")
        }
    }


    defaultConfig {
        applicationId = "com.indiewalk.watchdog.earthquake"
        minSdk = 26
        targetSdk = 36
        versionCode = 11
        versionName = "3.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = ""
        manifestPlaceholders["EXTERNAL_SDK_AUTO_INIT_ENABLED"] = "false"
        resValue("string", "admob_key_app_id", "ca-app-pub-3940256099942544~3347511713")
        resValue("string", "admob_key_bottom_banner", "ca-app-pub-3940256099942544/6300978111")
        resValue("string", "privacy_policy_public_url", "")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["MAPS_API_KEY"] = releaseSecret("MAPS_API_KEY_RELEASE") ?: ""
            manifestPlaceholders["CRASHLYTICS_COLLECTION_ENABLED"] = "true"
            resValue("string", "admob_key_app_id", releaseSecret("ADMOB_APP_ID_RELEASE") ?: "")
            resValue("string", "admob_key_bottom_banner", releaseSecret("ADMOB_BANNER_ID_RELEASE") ?: "")
            resValue("string", "privacy_policy_public_url", releaseSecret("PRIVACY_POLICY_URL_RELEASE") ?: "")
            if (hasFirebaseConfiguration) {
                extensions.configure<CrashlyticsExtension> {
                    mappingFileUploadEnabled = providers.gradleProperty("crashlyticsMappingUploadEnabled")
                        .orNull
                        ?.toBooleanStrictOrNull()
                        ?: false
                }
            }
        }

        debug {
            signingConfig = signingConfigs.getByName("debug")
            manifestPlaceholders["MAPS_API_KEY"] = releaseSecret("MAPS_API_KEY_DEBUG")
                ?: releaseSecret("MAPS_API_KEY")
                ?: ""
            manifestPlaceholders["CRASHLYTICS_COLLECTION_ENABLED"] = "false"
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
        buildConfig = true
    }
    sourceSets["androidTest"].assets.srcDir("$projectDir/schemas")
    sourceSets["main"].res.exclude("values/ads_key_ids.xml")
}

androidComponents {
    onVariants { variant ->
        if (variant.name == "nonMinifiedRelease") {
            variant.manifestPlaceholders.put("EXTERNAL_SDK_AUTO_INIT_ENABLED", "false")
            variant.manifestPlaceholders.put("CRASHLYTICS_COLLECTION_ENABLED", "false")
        }
    }
}

tasks.register("validateReleaseSecrets") {
    group = "verification"
    description = "Checks that release signing and Maps secrets are configured."

    doLast {
        val missing = missingReleaseSecrets()
        check(missing.isEmpty()) {
            "Missing required release secrets: ${missing.joinToString(", ")}"
        }
        validateProtectedReleaseConfiguration()
    }
}

tasks.register("validateStoreReleaseConfiguration") {
    group = "verification"
    description = "Checks Firebase configuration and explicit Crashlytics mapping upload for a store artifact."
    dependsOn("validateReleaseSecrets")

    doLast {
        check(hasFirebaseConfiguration) {
            "Missing required Firebase configuration: app/google-services.json. " +
                "Provision it from the protected release environment before packaging a store build."
        }
        check(providers.gradleProperty("crashlyticsMappingUploadEnabled").orNull == "true") {
            "Store release requires -PcrashlyticsMappingUploadEnabled=true to upload the R8 mapping file."
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)


    // compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.foundation)
    implementation(libs.foundation.layout)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material)
    implementation("androidx.navigation3:navigation3-runtime:1.0.0")
    implementation("androidx.navigation3:navigation3-ui:1.0.0")
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.core)

    // hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    ksp(libs.hilt.android.compiler)

    // Accompanist lib for compose integration
    // implementation(libs.accompanist.pager)
    implementation(libs.accompanist.permissions)

    // Core library desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // gson
    implementation(libs.gson)

    // Firebase Crashlytics. The Gradle plugins are enabled only when a local or CI
    // google-services.json is present; release packaging validates that it is required.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.profileinstaller)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.androidx.room.testing)

    // Google Maps and Location dependencies
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation (libs.kotlinx.coroutines.play.services)

    // Jetpack Compose integration for Google Maps
    implementation(libs.maps.compose)

    // Ad mob
    implementation(libs.playservices.ads)
    implementation(libs.user.messaging.platform)

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
    testImplementation(libs.ktor.client.mock)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    baselineProfile(project(":benchmark"))

}

baselineProfile {
    // Generation is an explicit connected-device task; release packaging uses checked-in rules.
    automaticGenerationDuringBuild = false
}
