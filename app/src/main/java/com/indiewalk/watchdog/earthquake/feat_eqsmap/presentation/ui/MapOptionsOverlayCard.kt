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
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LNG
import com.indiewalk.watchdog.earthquake.core.data.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.model.AppSettings
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components.LocationPickerNoPermissionsReq


@Composable
fun MapOptionsOverlayCard(
    modifier: Modifier = Modifier,
    manualPosition: Boolean,                          // persisted flag
    onManualPositionChange: (Boolean) -> Unit,        // UI toggle
    onManualPositionConfirmed: (LatLng) -> Unit,      // OK from picker
    mapType: MapType,
    onMapTypeChange: (MapType) -> Unit,
    settings: AppSettings,
    onDismiss: () -> Unit
) {

    // var initialLocation = LatLng(settings.position.latitude, settings.position.longitude)
    var selectedLocation by remember { mutableStateOf("") }
    var showLocationPicker by remember { mutableStateOf(false) }
    var selectedCoordinates by remember { mutableStateOf(settings.userPosition) }


    Box(modifier = Modifier
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
                        checked = manualPosition,
                        onCheckedChange = { checked ->
                            onManualPositionChange(checked)
                            if (checked) {
                                // open picker; persist only on OK
                                showLocationPicker = true
                            } else {
                                // Uncheck handled in parent (restore & recenter)
                            }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Set position manually", style = MaterialTheme.typography.bodyLarge)
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

    // LOCATION PICKER
    if (showLocationPicker) {
        LocationPickerNoPermissionsReq(
            /*initialLat = selectedCoordinates.latitude,
            initialLng = selectedCoordinates.longitude,*/
            initialFallback = settings.userPosition,
            onLocationSelected = { locationName, latLng ->
                selectedLocation = locationName
                selectedCoordinates = latLng
                onManualPositionConfirmed(latLng) // persist + recenter + close overlay in parent
                selectedLocation = locationName
                showLocationPicker = false
            },
            onDismiss = {
                showLocationPicker = false
            },
            onLocationChange = { }
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
    val manualPosition = remember { mutableStateOf(false) }
    val mapType = remember { mutableStateOf(MapType.NORMAL) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(16.dp)
    ) {
        MapOptionsOverlayCard(
            modifier = Modifier.align(Alignment.TopEnd),
            manualPosition = manualPosition.value,
            onManualPositionChange = { manualPosition.value = it },
            mapType = mapType.value,
            onMapTypeChange = { mapType.value = it },
            settings = AppSettings(
                userPosition = LatLng(DEFAULT_LAT, DEFAULT_LNG),
                manualLocOn = false,
                unitSystem = UnitSystem.METRIC,
            ),
            onManualPositionConfirmed = {},
            onDismiss = {}
        )
    }
}


