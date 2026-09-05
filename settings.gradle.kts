pluginManagement {
    repositories {
        google() // no filter
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EarthquakeWatchdog"
include(":app")
include(":benchmark")
