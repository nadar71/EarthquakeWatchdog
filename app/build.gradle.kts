import org.gradle.api.Action
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.kotlin.dsl.implementation
import org.gradle.kotlin.dsl.configure
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import java.util.Properties
import java.io.FileInputStream

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
    "MAPS_API_KEY_RELEASE"
)

fun releaseSecret(name: String): String? = sequenceOf(
    providers.gradleProperty(name).orNull,
    providers.environmentVariable(name).orNull,
    keystoreProperties.getProperty(name)
).firstOrNull { !it.isNullOrBlank() }

fun missingReleaseSecrets(): List<String> =
    requiredReleaseSecretNames.filter { releaseSecret(it).isNullOrBlank() }

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
        targetSdk = 35
        versionCode = 11
        versionName = "3.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = ""
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
}

tasks.register("validateReleaseSecrets") {
    group = "verification"
    description = "Checks that release signing and Maps secrets are configured."

    doLast {
        val missing = missingReleaseSecrets()
        check(missing.isEmpty()) {
            "Missing required release secrets: ${missing.joinToString(", ")}"
        }
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
