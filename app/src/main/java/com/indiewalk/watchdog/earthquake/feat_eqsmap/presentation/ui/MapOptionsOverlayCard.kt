package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.MapType
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_LNG
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components.LocationPicker


@Composable
fun MapOptionsOverlayCard(
    modifier: Modifier = Modifier,
    isManualPositionOn: Boolean,                               // persisted flag
    hasLocationPermissions: Boolean,
    mapType: MapType,
    settings: AppSettings,
    onManualPositionToggle: (Boolean) -> Unit,                 // UI toggle
    onManualPositionConfirmed: (LatLng, LocationInfo) -> Unit, // OK clicked, update LatLng, location info
    onMapTypeChange: (MapType) -> Unit,
    onDismiss: () -> Unit
) {
    var showLocationPicker by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }) {

        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                // Manual position
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isManualPositionOn,
                        onCheckedChange = { checked ->
                            onManualPositionToggle(checked)
                            if (checked) {
                                showLocationPicker = true // open picker; persistence manual position happens on OK
                            } else {
                                // Uncheck handled in parent (restore & recenter)
                            }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        modifier = Modifier
                            .clickable{
                                showLocationPicker = true
                            },
                        text = stringResource(R.string.maps_set_position_manually),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Text("Map type", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))

                MapTypeOption(
                    label = "Road map",
                    value = MapType.NORMAL,
                    selected = mapType == MapType.NORMAL,
                    onSelect = { onMapTypeChange(MapType.NORMAL) }
                )
                MapTypeOption(
                    label = "Hybrid",
                    value = MapType.HYBRID,
                    selected = mapType == MapType.HYBRID,
                    onSelect = { onMapTypeChange(MapType.HYBRID) }
                )
                MapTypeOption(
                    label = "Satellite",
                    value = MapType.SATELLITE,
                    selected = mapType == MapType.SATELLITE,
                    onSelect = { onMapTypeChange(MapType.SATELLITE) }
                )
                MapTypeOption(
                    label = "Terrain",
                    value = MapType.TERRAIN,
                    selected = mapType == MapType.TERRAIN,
                    onSelect = { onMapTypeChange(MapType.TERRAIN) }
                )
            }
        }
    }

    // Location picker
    if (showLocationPicker) {
        LocationPicker(
            initialFallback = if (settings.manualLocOn) settings.manualPosition
                              else settings.userPosition,
            userPosition = settings.userPosition,
            hasLocationPermissions = hasLocationPermissions,
            isManualOn = settings.manualLocOn,
            onLocationSelected = { latLng, address ->
                // save on prefs, recenter, close overlay in parent
                onManualPositionConfirmed(latLng, address)
                showLocationPicker = false
            },
            onDismiss = {
                showLocationPicker = false
            },
        )
    }
}

@Composable
private fun MapTypeOption(
    label: String,
    value: MapType,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable { onSelect() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}


// -------------------------------------- Previews --------------------------------------------------
@Preview(showBackground = true)
@Composable
private fun MapOptionsOverlayCardPreview() {
    val isManualPositionOn = remember { mutableStateOf(false) }
    val mapType = remember { mutableStateOf(MapType.NORMAL) }
    val manualPosition = remember { mutableStateOf(LatLng(DEFAULT_LAT, DEFAULT_LNG)) }
    val manualAddress = remember { mutableStateOf<LocationInfo?>(null) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(16.dp)
    ) {
        MapOptionsOverlayCard(
            modifier = Modifier.align(Alignment.TopEnd),
            isManualPositionOn = isManualPositionOn.value,
            onManualPositionToggle = { isManualPositionOn.value = it },
            onManualPositionConfirmed = { latLng, address ->
                manualPosition.value = latLng
                manualAddress.value = address
                                        },
            hasLocationPermissions = false,
            mapType = mapType.value,
            onMapTypeChange = { mapType.value = it },
            settings = AppSettings(
                userPosition = LatLng(DEFAULT_LAT, DEFAULT_LNG),
                manualLocOn = false,
                unitSystem = UnitSystem.METRIC,
            ),
            onDismiss = {}
        )
    }
}
