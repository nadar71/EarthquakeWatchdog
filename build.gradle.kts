// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins { // plugin declaration as alias
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

tasks.register("verifyStaticQuality") {
    group = "verification"
    description = "Runs the debug and release Android lint gates used by CI."
    dependsOn(":app:lintDebug", ":app:lintRelease")
}

