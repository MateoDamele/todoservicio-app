package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.NaranjaAlerta
import com.example.ui.theme.NaranjaAlertaFondo
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo
import com.example.util.GoogleMapsParseResult
import com.example.util.GoogleMapsUrlParser
import com.example.util.URUGUAY_DEFAULT_LAT
import com.example.util.URUGUAY_DEFAULT_LNG
import com.example.util.UruguayLocationProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Selector de ubicación mediante Enlace de Google Maps (URL Parser + Mini Mapa Interactivo HD).
 * Extrae Lat/Lng automáticamente de links largos, parámetros ?q=, coordenadas puras
 * o enlaces acortados (maps.app.goo.gl) y muestra vista previa en tiempo real en Uruguay.
 */
@Composable
fun GoogleMapsLinkPickerField(
    linkValue: String,
    onLinkValueChange: (String) -> Unit,
    previewName: String,
    previewAddress: String,
    resolvedLat: Double,
    resolvedLng: Double,
    hasResolvedCoordinates: Boolean,
    geofenceRadiusMeters: Int,
    onRadiusSelected: ((Int) -> Unit)? = null,
    onCoordinatesResolved: (lat: Double, lng: Double, placeLabel: String?, normalizedUrl: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var isResolving by remember { mutableStateOf(false) }
    var parseError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(linkValue) {
        val trimmed = linkValue.trim()
        if (trimmed.isBlank()) {
            isResolving = false
            parseError = null
            return@LaunchedEffect
        }

        val syncPoint = GoogleMapsUrlParser.parseCoordinatesFromText(trimmed)
        if (syncPoint != null) {
            isResolving = false
            parseError = null
            onCoordinatesResolved(syncPoint.lat, syncPoint.lng, syncPoint.placeLabel, trimmed)
            return@LaunchedEffect
        }

        if (GoogleMapsUrlParser.looksLikeUrl(trimmed)) {
            delay(280)
            isResolving = true
            parseError = null
            when (val res = GoogleMapsUrlParser.resolveAndParseGoogleMapsUrl(trimmed)) {
                is GoogleMapsParseResult.Success -> {
                    isResolving = false
                    parseError = null
                    onCoordinatesResolved(res.point.lat, res.point.lng, res.point.placeLabel, trimmed)
                }
                is GoogleMapsParseResult.Error -> {
                    isResolving = false
                    parseError = res.message
                }
            }
        } else {
            isResolving = false
            parseError = "Pegá un link de Google Maps o coordenadas (-34.9011, -56.1645)"
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = linkValue,
            onValueChange = onLinkValueChange,
            label = { Text("Pegar enlace de Google Maps", fontSize = 12.sp) },
            placeholder = { Text("https://maps.app.goo.gl/... o @lat,lng", fontSize = 11.5.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Link,
                    contentDescription = "Enlace Google Maps",
                    tint = AzulCorporativo,
                    modifier = Modifier.size(19.dp)
                )
            },
            trailingIcon = {
                if (isResolving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = AzulCorporativo
                    )
                } else {
                    IconButton(
                        onClick = {
                            val clipText = clipboard.getText()?.text?.trim().orEmpty()
                            if (clipText.isNotBlank()) {
                                onLinkValueChange(clipText)
                            } else {
                                scope.launch {
                                    val fix = UruguayLocationProvider.fetchCurrentGpsLocation(context)
                                    if (fix.hasPermission && !fix.isMocked) {
                                        val coords = String.format(
                                            java.util.Locale.US,
                                            "%.6f, %.6f",
                                            fix.lat,
                                            fix.lng
                                        )
                                        onLinkValueChange("https://www.google.com/maps?q=$coords")
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = "Pegar o usar ubicación",
                            tint = CelesteAcento,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Blanco,
                unfocusedContainerColor = Blanco,
                focusedBorderColor = AzulCorporativo,
                unfocusedBorderColor = GrisBorde
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("google_maps_link_input")
        )

        if (hasResolvedCoordinates) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = VerdeExitoFondo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = VerdeExito,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Ubicación extraída: ${String.format(java.util.Locale.US, "%.5f", resolvedLat)}, ${String.format(java.util.Locale.US, "%.5f", resolvedLng)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = VerdeExito,
                            fontSize = 10.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else if (!parseError.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NaranjaAlertaFondo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = NaranjaAlerta,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = parseError!!,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = Color(0xFF9A3412),
                            fontSize = 10.5.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (onRadiusSelected != null) {
            Text(
                text = "Radio de Tolerancia de Geocerca:",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = AzulMarca
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(50, 100, 150).forEach { radius ->
                    val isSelected = geofenceRadiusMeters == radius
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AzulCorporativo else GrisNeutro,
                        border = BorderStroke(1.dp, if (isSelected) AzulCorporativo else GrisBorde),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onRadiusSelected(radius) }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$radius m",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Blanco else AzulMarca,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Vista previa inmediata en mini-mapa interactivo con pin y perímetro
        val mapLat = if (hasResolvedCoordinates) resolvedLat else URUGUAY_DEFAULT_LAT
        val mapLng = if (hasResolvedCoordinates) resolvedLng else URUGUAY_DEFAULT_LNG
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, GrisBorde, RoundedCornerShape(14.dp))
                .background(Color(0xFFE9EEF5))
                .testTag("mini_map_preview")
        ) {
            RealStreetInteractiveMap(
                centerLat = mapLat,
                centerLng = mapLng,
                zoomLevel = if (hasResolvedCoordinates) 16 else 12,
                geofences = if (hasResolvedCoordinates) {
                    listOf(
                        MapGeocenceItem(
                            id = "preview-geo",
                            name = previewName.ifBlank { "Sede / Edificio" },
                            address = previewAddress.ifBlank { "Uruguay" },
                            lat = mapLat,
                            lng = mapLng,
                            radiusMeters = geofenceRadiusMeters
                        )
                    )
                } else {
                    emptyList()
                },
                markers = if (hasResolvedCoordinates) {
                    listOf(
                        MapMarkerItem(
                            id = "preview-pin",
                            title = previewName.ifBlank { "Ubicación" },
                            subtitle = previewAddress.ifBlank { "Coordenada verificada" },
                            lat = mapLat,
                            lng = mapLng,
                            type = "CLIENT",
                            badgeText = "S"
                        )
                    )
                } else {
                    emptyList()
                },
                showMyLocationButton = false,
                autoRequestLocation = false,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
