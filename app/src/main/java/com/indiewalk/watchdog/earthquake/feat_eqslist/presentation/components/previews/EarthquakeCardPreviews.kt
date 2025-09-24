package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.previews

import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.EarthquakeCard
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.FeatureDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.GeometryDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.PropertiesDTO

// Use your QuickSand-based Typography from your theme module
// import your Typography symbol; adjust the import to your package.

@Preview(
    name = "Earthquake cards – Light",
    showBackground = true,
    backgroundColor = 0xFFF7F9FC,
    widthDp = 360
)
@Composable
fun EarthquakeCardPreviewLight() {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        // typography = Typography
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC))
                .padding(vertical = 8.dp)
        ) {
            Column {
                EarthquakeCard(
                    eq = sampleFeature1(),
                    distanceKm = 15236.0,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                EarthquakeCard(
                    eq = sampleFeature2(),
                    distanceKm = 7826.0,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Preview(
    name = "Earthquake cards – Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 360
)
@Composable
fun EarthquakeCardPreviewDark() {
    EarthquakeCardPreviewLight()
}

/* ------------------ sample data ------------------ */

private fun sampleFeature1() = FeatureDTO(
    type = "Feature",
    id = "sample-ci-001",
    properties = PropertiesDTO(
        mag = 5.0,
        place = "76 km West of Macquarie Island",
        time = 1583196360000,  // Mar 03, 2020 2:06 am (example)
        updated = 1583199960000,
        tz = null,
        url = "https://earthquake.usgs.gov/earthquakes/eventpage/sample-ci-001",
        detail = null,
        felt = null,
        cdi = null,
        mmi = null,
        alert = null,
        status = "reviewed",
        tsunami = 0,
        sig = 385,
        net = "ci",
        code = "001",
        ids = ",sample-ci-001,",
        sources = "ci",
        types = "origin,phase-data",
        nst = 25,
        dmin = 0.123,
        rms = 0.76,
        gap = 45.0,
        magType = "mb",
        type = "earthquake"
    ),
    geometry = GeometryDTO(
        type = "Point",
        coordinates = listOf(158.95, -54.5, 10.0) // lon, lat, depth(km)
    )
)

private fun sampleFeature2() = FeatureDTO(
    type = "Feature",
    id = "sample-us-002",
    properties = PropertiesDTO(
        mag = 4.9,
        place = "Near the Chagos Archipelago region",
        time = 1583267460000,  // Mar 03, 2020 6:31 pm (example)
        updated = 1583271060000,
        tz = null,
        url = "https://earthquake.usgs.gov/earthquakes/eventpage/sample-us-002",
        detail = null,
        felt = null,
        cdi = null,
        mmi = null,
        alert = null,
        status = "reviewed",
        tsunami = 0,
        sig = 369,
        net = "us",
        code = "002",
        ids = ",sample-us-002,",
        sources = "us",
        types = "origin,phase-data",
        nst = 19,
        dmin = 0.234,
        rms = 0.68,
        gap = 60.0,
        magType = "mb",
        type = "earthquake"
    ),
    geometry = GeometryDTO(
        type = "Point",
        coordinates = listOf(72.0, -6.0, 12.0)
    )
)
