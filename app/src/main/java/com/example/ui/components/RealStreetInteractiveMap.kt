package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.LruCache
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.BuildConfig
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.PoppinsFontFamily
import com.example.util.URUGUAY_DEFAULT_LAT
import com.example.util.URUGUAY_DEFAULT_LNG
import com.example.util.rememberUruguayLocationState
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sinh
import kotlin.math.tan

/**
 * Modelo para geocercas en el mapa.
 */
data class MapGeocenceItem(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val radiusMeters: Int,
    val staffCount: Int = 1
)

/**
 * Modelo para marcadores de asistencia, técnicos o sedes en el mapa.
 */
data class MapMarkerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val lat: Double,
    val lng: Double,
    val type: String, // "E" (Entrada), "S" (Salida), "TECH" (Técnico), "CLIENT" (Cliente)
    val isAlert: Boolean = false,
    val badgeText: String = "",
    val timeText: String = ""
)

private object StreetTileCache {
    val cache = LruCache<String, ImageBitmap>(140)
    val inFlight = mutableSetOf<String>()
    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

    suspend fun loadTile(z: Int, x: Int, y: Int): ImageBitmap? {
        val key = "$z/$x/$y"
        cache.get(key)?.let { return it }

        synchronized(inFlight) {
            if (!inFlight.add(key)) return null
        }

        return try {
            withContext(Dispatchers.IO) {
                val urls = listOf(
                    "https://mt1.google.com/vt/lyrs=m&hl=es&x=$x&y=$y&z=$z",
                    "https://tile.openstreetmap.org/$z/$x/$y.png"
                )
                var loaded: ImageBitmap? = null
                for (url in urls) {
                    try {
                        val req = Request.Builder()
                            .url(url)
                            .header("User-Agent", "TodoservicioApp/1.0 (Uruguay Operational GPS)")
                            .build()
                        httpClient.newCall(req).execute().use { resp ->
                            if (resp.isSuccessful) {
                                resp.body?.byteStream()?.use { stream ->
                                    BitmapFactory.decodeStream(stream)?.let { bmp ->
                                        loaded = bmp.asImageBitmap()
                                    }
                                }
                            }
                        }
                        if (loaded != null) break
                    } catch (_: Throwable) {
                    }
                }
                loaded?.let { cache.put(key, it) }
                loaded
            }
        } finally {
            synchronized(inFlight) {
                inFlight.remove(key)
            }
        }
    }
}

/**
 * Abre la ubicación o dirección indicada directamente en Google Maps (con fallback web seguro).
 */
fun openExternalGoogleMaps(
    context: Context,
    queryOrAddress: String,
    lat: Double? = null,
    lng: Double? = null
) {
    val encodedQuery = Uri.encode(queryOrAddress.ifBlank { "Montevideo, Uruguay" })
    val geoUri = if (lat != null && lng != null) {
        Uri.parse("geo:$lat,$lng?q=$lat,$lng($encodedQuery)")
    } else {
        Uri.parse("geo:0,0?q=$encodedQuery")
    }
    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(mapIntent)
    } catch (_: Exception) {
        try {
            val webUri = if (lat != null && lng != null) {
                Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            } else {
                Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedQuery")
            }
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        } catch (_: Exception) {
            Toast.makeText(context, "No se encontró una app de mapas disponible", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Visor cartográfico HD interactivo de Uruguay (Montevideo, Canelones y todo el país).
 * - Solicita permisos nativos GPS al abrir y permite centrar con "Mi Ubicación".
 * - Usa Google Maps SDK nativo cuando MAPS_API_KEY es válida y mosaicos HD de calles
 *   con nombres claros de avenidas, rutas y barrios cuando la clave aún no fue configurada.
 */
@Composable
fun RealStreetInteractiveMap(
    centerLat: Double = URUGUAY_DEFAULT_LAT,
    centerLng: Double = URUGUAY_DEFAULT_LNG,
    zoomLevel: Int = 14,
    geofences: List<MapGeocenceItem> = emptyList(),
    markers: List<MapMarkerItem> = emptyList(),
    routePolyline: List<Pair<Double, Double>> = emptyList(),
    onMarkerClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    showMyLocationButton: Boolean = true,
    autoRequestLocation: Boolean = true,
    centerOnUserLocationIfNoTarget: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    val locationState = rememberUruguayLocationState(autoRequestOnLaunch = autoRequestLocation)

    val effectiveCenterLat = remember(
        centerOnUserLocationIfNoTarget,
        locationState.userLat,
        centerLat
    ) {
        if (centerOnUserLocationIfNoTarget && locationState.userLat != null) {
            locationState.userLat!!
        } else {
            centerLat
        }
    }
    val effectiveCenterLng = remember(
        centerOnUserLocationIfNoTarget,
        locationState.userLng,
        centerLng
    ) {
        if (centerOnUserLocationIfNoTarget && locationState.userLng != null) {
            locationState.userLng!!
        } else {
            centerLng
        }
    }

    var overrideFocusLat by remember { mutableStateOf<Double?>(null) }
    var overrideFocusLng by remember { mutableStateOf<Double?>(null) }
    var overrideFocusZoom by remember { mutableStateOf<Int?>(null) }
    var focusTriggerTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(effectiveCenterLat, effectiveCenterLng, zoomLevel) {
        overrideFocusLat = null
        overrideFocusLng = null
        overrideFocusZoom = null
    }

    val targetLat = overrideFocusLat ?: effectiveCenterLat
    val targetLng = overrideFocusLng ?: effectiveCenterLng
    val targetZoom = overrideFocusZoom ?: zoomLevel

    val isMapsKeyValid = remember(context) {
        val buildKey = BuildConfig.MAPS_API_KEY.trim()
        val manifestKey = try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty().trim()
        } catch (_: Exception) {
            ""
        }
        val resolvedKey = manifestKey.ifBlank { buildKey }
        resolvedKey.isNotBlank() &&
            resolvedKey != "MY_MAPS_API_KEY" &&
            resolvedKey != "YOUR_API_KEY" &&
            !resolvedKey.startsWith("MY_")
    }

    val isPlayServicesAvailable = remember(context) {
        try {
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        } catch (_: Throwable) {
            false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE9EEF5))
            .testTag("google_maps_container")
    ) {
        if (!isPlayServicesAvailable || !isMapsKeyValid) {
            SlippyTileUruguayMap(
                centerLat = targetLat,
                centerLng = targetLng,
                zoomLevel = targetZoom,
                focusTriggerTick = focusTriggerTick,
                userLat = locationState.userLat,
                userLng = locationState.userLng,
                hasUserFix = locationState.hasPermission && locationState.userLat != null,
                geofences = geofences,
                markers = markers,
                routePolyline = routePolyline,
                onMarkerClick = { currentOnMarkerClick(it) }
            )
        } else {
            val mapView = remember {
                try {
                    MapsInitializer.initialize(context.applicationContext, MapsInitializer.Renderer.LATEST, null)
                } catch (_: Throwable) {
                }
                MapView(context).apply {
                    onCreate(Bundle())
                }
            }

            var googleMapInstance by remember { mutableStateOf<GoogleMap?>(null) }

            DisposableEffect(lifecycleOwner, mapView) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START -> mapView.onStart()
                        Lifecycle.Event.ON_RESUME -> mapView.onResume()
                        Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                        Lifecycle.Event.ON_STOP -> mapView.onStop()
                        Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    try {
                        mapView.onPause()
                        mapView.onStop()
                        mapView.onDestroy()
                    } catch (_: Throwable) {
                    }
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    mapView.apply {
                        getMapAsync { gMap ->
                            gMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                            gMap.uiSettings.apply {
                                isZoomControlsEnabled = true
                                isCompassEnabled = true
                                isMapToolbarEnabled = false
                                isMyLocationButtonEnabled = false
                                isScrollGesturesEnabled = true
                                isZoomGesturesEnabled = true
                                isRotateGesturesEnabled = true
                            }
                            gMap.setOnMarkerClickListener { marker ->
                                val tagId = marker.tag as? String
                                if (!tagId.isNullOrBlank()) {
                                    currentOnMarkerClick(tagId)
                                }
                                false
                            }
                            googleMapInstance = gMap
                        }
                    }
                },
                update = {
                    val gMap = googleMapInstance ?: return@AndroidView
                    applyMyLocationLayer(gMap, locationState.hasPermission)
                    renderGoogleMapOverlays(
                        context = context,
                        googleMap = gMap,
                        centerLat = targetLat,
                        centerLng = targetLng,
                        zoomLevel = targetZoom,
                        geofences = geofences,
                        markers = markers,
                        routePolyline = routePolyline
                    )
                }
            )

            DisposableEffect(
                googleMapInstance,
                targetLat,
                targetLng,
                targetZoom,
                focusTriggerTick,
                locationState.hasPermission,
                geofences,
                markers,
                routePolyline
            ) {
                googleMapInstance?.let { gMap ->
                    applyMyLocationLayer(gMap, locationState.hasPermission)
                    renderGoogleMapOverlays(
                        context = context,
                        googleMap = gMap,
                        centerLat = targetLat,
                        centerLng = targetLng,
                        zoomLevel = targetZoom,
                        geofences = geofences,
                        markers = markers,
                        routePolyline = routePolyline
                    )
                }
                onDispose { }
            }
        }

        // Botón compacto inferior izquierdo: abrir punto en Google Maps externo
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Blanco.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, GrisBorde),
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    val target = markers.firstOrNull()
                    openExternalGoogleMaps(
                        context = context,
                        queryOrAddress = target?.subtitle ?: "Montevideo, Uruguay",
                        lat = target?.lat ?: targetLat,
                        lng = target?.lng ?: targetLng
                    )
                }
                .testTag("open_external_google_maps_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Map,
                    contentDescription = "Abrir en Google Maps",
                    tint = AzulCorporativo,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Google Maps",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca,
                        fontSize = 10.sp
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = CelesteAcento,
                    modifier = Modifier.size(11.dp)
                )
            }
        }

        // Botón flotante derecho: "Mi Ubicación" para centrar al instante en el GPS real
        if (showMyLocationButton) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Blanco.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, GrisBorde),
                shadowElevation = 5.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        locationState.requestMyLocation()
                        val uLat = locationState.userLat
                        val uLng = locationState.userLng
                        if (uLat != null && uLng != null) {
                            overrideFocusLat = uLat
                            overrideFocusLng = uLng
                            overrideFocusZoom = 16
                            focusTriggerTick++
                        } else {
                            Toast.makeText(context, "Buscando ubicación GPS real...", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .testTag("btn_my_location_center")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (locationState.isFetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = AzulCorporativo
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = "Mi Ubicación",
                            tint = AzulMarca,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "Mi Ubicación",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun applyMyLocationLayer(googleMap: GoogleMap, hasPermission: Boolean) {
    try {
        googleMap.isMyLocationEnabled = hasPermission
    } catch (_: SecurityException) {
    }
}

private fun renderGoogleMapOverlays(
    context: Context,
    googleMap: GoogleMap,
    centerLat: Double,
    centerLng: Double,
    zoomLevel: Int,
    geofences: List<MapGeocenceItem>,
    markers: List<MapMarkerItem>,
    routePolyline: List<Pair<Double, Double>>
) {
    googleMap.clear()

    // 1. Geocercas translúcidas reales en metros
    geofences.forEach { g ->
        val pos = LatLng(g.lat, g.lng)
        googleMap.addCircle(
            CircleOptions()
                .center(pos)
                .radius(g.radiusMeters.toDouble())
                .fillColor(0x261E4B82)
                .strokeColor(0xFF0D284E.toInt())
                .strokeWidth(4f)
        )
        googleMap.addMarker(
            MarkerOptions()
                .position(pos)
                .title(g.name)
                .snippet("${g.address} • Radio ${g.radiusMeters}m")
                .icon(createGeofenceBadgeIcon(context, g.name))
        )
    }

    // 2. Ruta Polyline en vivo
    if (routePolyline.size >= 2) {
        val latLngs = routePolyline.map { LatLng(it.first, it.second) }
        googleMap.addPolyline(
            PolylineOptions()
                .addAll(latLngs)
                .color(0xFF1E4B82.toInt())
                .width(10f)
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
        )
    }

    // 3. Pines de marcas E (#10B981), S (#1E4B82), Alerta (#F59E0B), Técnico, Cliente
    markers.forEach { m ->
        val pos = LatLng(m.lat, m.lng)
        val snippet = buildString {
            append(m.subtitle)
            if (m.timeText.isNotBlank()) append(" • ${m.timeText}")
            if (m.isAlert) append(" • Fuera de rango")
        }
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(pos)
                .title(m.title)
                .snippet(snippet)
                .icon(createCircularPinIcon(context, m))
        )
        marker?.tag = m.id
    }

    // 4. Vuelo suave de cámara
    try {
        if (routePolyline.size >= 2) {
            val boundsBuilder = LatLngBounds.Builder()
            routePolyline.forEach { boundsBuilder.include(LatLng(it.first, it.second)) }
            markers.forEach { boundsBuilder.include(LatLng(it.lat, it.lng)) }
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 90), 550, null)
        } else {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(centerLat, centerLng),
                    zoomLevel.toFloat()
                ),
                550,
                null
            )
        }
    } catch (_: Exception) {
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(centerLat, centerLng),
                zoomLevel.toFloat()
            )
        )
    }
}

private fun createCircularPinIcon(context: Context, item: MapMarkerItem): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val sizePx = (36f * density).toInt().coerceAtLeast(48)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val fillColor = when {
        item.isAlert -> 0xFFF59E0B.toInt()
        item.type == "E" -> 0xFF10B981.toInt()
        item.type == "S" -> 0xFF1E4B82.toInt()
        item.type == "CLIENT" -> 0xFF0D284E.toInt()
        else -> 0xFF1E4B82.toInt()
    }

    val label = when {
        item.isAlert -> "!"
        item.type == "E" -> "E"
        item.type == "S" -> "S"
        item.type == "CLIENT" -> "S"
        item.badgeText.isNotBlank() && item.badgeText.all { it.isLetterOrDigit() } -> item.badgeText.take(2)
        else -> "T"
    }

    val radius = sizePx / 2f
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(radius, radius, radius - 1f, borderPaint)

    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(radius, radius, radius - 3f * density, circlePaint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 12f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val textY = radius - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(label, radius, textY, textPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun createGeofenceBadgeIcon(context: Context, name: String): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val shortName = if (name.length > 16) "${name.take(15)}…" else name
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 10f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val textWidth = textPaint.measureText(shortName)
    val padH = 10f * density
    val heightPx = (24f * density).toInt().coerceAtLeast(32)
    val widthPx = (textWidth + padH * 2f).toInt().coerceAtLeast(64)

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0D284E.toInt()
        style = Paint.Style.FILL
    }
    val corner = 8f * density
    canvas.drawRoundRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), corner, corner, bgPaint)

    val textY = (heightPx / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(shortName, padH, textY, textPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Mapa Callejero Real en HD mediante mosaicos Web Mercator de Google Maps / OSM,
 * con nombres nítidos de calles de Uruguay, zoom táctil fluido, vuelo animado y geocercas a escala.
 */
@Composable
private fun SlippyTileUruguayMap(
    centerLat: Double,
    centerLng: Double,
    zoomLevel: Int,
    focusTriggerTick: Int,
    userLat: Double?,
    userLng: Double?,
    hasUserFix: Boolean,
    geofences: List<MapGeocenceItem>,
    markers: List<MapMarkerItem>,
    routePolyline: List<Pair<Double, Double>>,
    onMarkerClick: (String) -> Unit
) {
    val density = LocalDensity.current.density
    val scope = rememberCoroutineScope()
    val tileRenderPx = remember(density) { (256f * density.coerceIn(1.35f, 2.2f)) }

    val animLat = remember { Animatable(centerLat.toFloat()) }
    val animLng = remember { Animatable(centerLng.toFloat()) }
    val animZoom = remember { Animatable(zoomLevel.toFloat().coerceIn(5f, 18f)) }

    var isUserPanning by remember { mutableStateOf(false) }

    LaunchedEffect(centerLat, centerLng, zoomLevel, focusTriggerTick) {
        isUserPanning = false
        launch {
            animLat.animateTo(
                targetValue = centerLat.toFloat(),
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }
        launch {
            animLng.animateTo(
                targetValue = centerLng.toFloat(),
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }
        launch {
            animZoom.animateTo(
                targetValue = zoomLevel.toFloat().coerceIn(5f, 18f),
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }
    }

    val loadedTiles = remember { mutableStateMapOf<String, ImageBitmap>() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDEFF2))
            .pointerInput(tileRenderPx) {
                detectTransformGestures { _, pan, zoomChange, _ ->
                    isUserPanning = true
                    scope.launch {
                        val curZoom = animZoom.value
                        val newZoom = (curZoom + (ln(zoomChange.toDouble()) / ln(2.0)).toFloat()).coerceIn(5f, 18.5f)
                        val worldPx = tileRenderPx.toDouble() * 2.0.pow(newZoom.toDouble())
                        val centerPxX = lngToWorldX(animLng.value.toDouble(), worldPx) - pan.x
                        val centerPxY = latToWorldY(animLat.value.toDouble(), worldPx) - pan.y
                        val newLat = worldYToLat(centerPxY, worldPx).coerceIn(-85.0, 85.0)
                        val newLng = worldXToLng(centerPxX, worldPx).coerceIn(-180.0, 180.0)
                        animZoom.snapTo(newZoom)
                        animLat.snapTo(newLat.toFloat())
                        animLng.snapTo(newLng.toFloat())
                    }
                }
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val heightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        val currentLat = animLat.value.toDouble()
        val currentLng = animLng.value.toDouble()
        val currentZoom = animZoom.value.toDouble()
        val tileZoom = currentZoom.roundToInt().coerceIn(4, 18)

        val worldScaleAtCurrentZoom = tileRenderPx.toDouble() * 2.0.pow(currentZoom)
        val centerWorldX = lngToWorldX(currentLng, worldScaleAtCurrentZoom)
        val centerWorldY = latToWorldY(currentLat, worldScaleAtCurrentZoom)
        val topLeftWorldX = centerWorldX - widthPx / 2.0
        val topLeftWorldY = centerWorldY - heightPx / 2.0

        val tileWorldSize = tileRenderPx.toDouble() * 2.0.pow(currentZoom - tileZoom)
        val maxTileIndex = (1 shl tileZoom) - 1
        val minTileX = floor(topLeftWorldX / tileWorldSize).toInt()
        val maxTileX = floor((topLeftWorldX + widthPx) / tileWorldSize).toInt()
        val minTileY = floor(topLeftWorldY / tileWorldSize).toInt().coerceAtLeast(0)
        val maxTileY = floor((topLeftWorldY + heightPx) / tileWorldSize).toInt().coerceAtMost(maxTileIndex)

        // Precarga de mosaicos visibles en background
        LaunchedEffect(tileZoom, minTileX, maxTileX, minTileY, maxTileY) {
            for (ty in minTileY..maxTileY) {
                for (tx in minTileX..maxTileX) {
                    val wrappedX = ((tx % (1 shl tileZoom)) + (1 shl tileZoom)) % (1 shl tileZoom)
                    val key = "$tileZoom/$wrappedX/$ty"
                    if (!loadedTiles.containsKey(key)) {
                        val cached = StreetTileCache.cache.get(key)
                        if (cached != null) {
                            loadedTiles[key] = cached
                        } else {
                            launch {
                                val bmp = StreetTileCache.loadTile(tileZoom, wrappedX, ty)
                                if (bmp != null) {
                                    loadedTiles[key] = bmp
                                }
                            }
                        }
                    }
                }
            }
        }

        fun toScreenOffset(lat: Double, lng: Double): Offset {
            val wx = lngToWorldX(lng, worldScaleAtCurrentZoom)
            val wy = latToWorldY(lat, worldScaleAtCurrentZoom)
            return Offset((wx - topLeftWorldX).toFloat(), (wy - topLeftWorldY).toFloat())
        }

        ComposeCanvas(modifier = Modifier.fillMaxSize()) {
            // 1. Mosaicos reales de calles HD (Uruguay)
            for (ty in minTileY..maxTileY) {
                for (tx in minTileX..maxTileX) {
                    val wrappedX = ((tx % (1 shl tileZoom)) + (1 shl tileZoom)) % (1 shl tileZoom)
                    val key = "$tileZoom/$wrappedX/$ty"
                    val screenX = (tx * tileWorldSize - topLeftWorldX).toInt()
                    val screenY = (ty * tileWorldSize - topLeftWorldY).toInt()
                    val drawDim = tileWorldSize.roundToInt() + 1

                    val tileBmp = loadedTiles[key] ?: StreetTileCache.cache.get(key)
                    if (tileBmp != null) {
                        drawImage(
                            image = tileBmp,
                            dstOffset = IntOffset(screenX, screenY),
                            dstSize = IntSize(drawDim, drawDim)
                        )
                    } else {
                        drawRect(
                            color = Color(0xFFE6ECF2),
                            topLeft = Offset(screenX.toFloat(), screenY.toFloat()),
                            size = androidx.compose.ui.geometry.Size(drawDim.toFloat(), drawDim.toFloat())
                        )
                        drawRect(
                            color = Color(0xFFD6E0EA),
                            topLeft = Offset(screenX.toFloat(), screenY.toFloat()),
                            size = androidx.compose.ui.geometry.Size(drawDim.toFloat(), drawDim.toFloat()),
                            style = Stroke(width = 1f)
                        )
                    }
                }
            }

            // 2. Geocercas circulares con radio real en metros
            val baseMetersPerPixel = 156543.03392 / 2.0.pow(currentZoom) / (tileRenderPx / 256.0)
            geofences.forEach { geo ->
                val center = toScreenOffset(geo.lat, geo.lng)
                val metersPerPxAtLat = baseMetersPerPixel * cos(Math.toRadians(geo.lat)).coerceAtLeast(0.2)
                val radiusPx = (geo.radiusMeters / metersPerPxAtLat).toFloat().coerceIn(22f, 220f)
                drawCircle(
                    color = Color(0x281E4B82),
                    radius = radiusPx,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF0D284E),
                    radius = radiusPx,
                    center = center,
                    style = Stroke(width = 3f)
                )
            }

            // 3. Polilínea de ruta en vivo
            if (routePolyline.size >= 2) {
                for (i in 0 until routePolyline.size - 1) {
                    val s1 = toScreenOffset(routePolyline[i].first, routePolyline[i].second)
                    val s2 = toScreenOffset(routePolyline[i + 1].first, routePolyline[i + 1].second)
                    drawLine(
                        color = Color(0xFF1E4B82),
                        start = s1,
                        end = s2,
                        strokeWidth = 7f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                    )
                }
            }

            // 4. Punto GPS vivo del usuario ("Mi Ubicación")
            if (hasUserFix && userLat != null && userLng != null) {
                val uPos = toScreenOffset(userLat, userLng)
                drawCircle(
                    color = Color(0x332563EB),
                    radius = 22f,
                    center = uPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 10.5f,
                    center = uPos
                )
                drawCircle(
                    color = Color(0xFF2563EB),
                    radius = 7.5f,
                    center = uPos
                )
            }
        }

        // Etiquetas de geocercas (Edificios / Sedes)
        geofences.forEach { geo ->
            val pos = toScreenOffset(geo.lat, geo.lng)
            if (pos.x in -100f..(widthPx + 100f) && pos.y in -60f..(heightPx + 60f)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AzulMarca.copy(alpha = 0.90f),
                    shadowElevation = 3.dp,
                    modifier = Modifier.offset {
                        IntOffset((pos.x - 52f).roundToInt(), (pos.y + 12f).roundToInt())
                    }
                ) {
                    Text(
                        text = geo.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco,
                            fontSize = 9.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Pines interactivos diferenciados: E (#10B981), S (#1E4B82), Alerta (#F59E0B)
        markers.forEach { m ->
            val pos = toScreenOffset(m.lat, m.lng)
            if (pos.x in -80f..(widthPx + 80f) && pos.y in -80f..(heightPx + 80f)) {
                val pinColor = when {
                    m.isAlert -> Color(0xFFF59E0B)
                    m.type == "E" -> Color(0xFF10B981)
                    m.type == "S" -> Color(0xFF1E4B82)
                    m.type == "CLIENT" -> AzulMarca
                    else -> AzulCorporativo
                }
                val label = when {
                    m.isAlert -> "!"
                    m.type == "E" -> "E"
                    m.type == "S" -> "S"
                    m.badgeText.isNotBlank() -> m.badgeText.take(3)
                    else -> m.type.take(2)
                }
                Surface(
                    shape = CircleShape,
                    color = pinColor,
                    shadowElevation = 5.dp,
                    border = BorderStroke(2.dp, Blanco),
                    modifier = Modifier
                        .size(32.dp)
                        .offset {
                            IntOffset((pos.x - 16f * density).roundToInt(), (pos.y - 16f * density).roundToInt())
                        }
                        .clip(CircleShape)
                        .clickable { onMarkerClick(m.id) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Blanco,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Controles compactos +/- de Zoom en esquina superior derecha
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Blanco.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, GrisBorde),
            shadowElevation = 3.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            scope.launch {
                                animZoom.animateTo(
                                    (animZoom.value + 1f).coerceAtMost(18f),
                                    tween(220)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Acercar", tint = AzulMarca, modifier = Modifier.size(16.dp))
                }
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 1.dp)
                        .background(GrisBorde)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            scope.launch {
                                animZoom.animateTo(
                                    (animZoom.value - 1f).coerceAtLeast(6f),
                                    tween(220)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Remove, contentDescription = "Alejar", tint = AzulMarca, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun lngToWorldX(lng: Double, worldPx: Double): Double =
    ((lng + 180.0) / 360.0) * worldPx

private fun latToWorldY(lat: Double, worldPx: Double): Double {
    val clamped = lat.coerceIn(-85.0511, 85.0511)
    val sinLat = kotlin.math.sin(Math.toRadians(clamped))
    val yNorm = 0.5 - ln((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * PI)
    return yNorm * worldPx
}

private fun worldXToLng(worldX: Double, worldPx: Double): Double =
    (worldX / worldPx) * 360.0 - 180.0

private fun worldYToLat(worldY: Double, worldPx: Double): Double {
    val n = PI - (2.0 * PI * worldY) / worldPx
    return Math.toDegrees(atan(sinh(n)))
}
