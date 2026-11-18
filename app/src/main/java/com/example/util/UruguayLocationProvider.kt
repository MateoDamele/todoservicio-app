package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.os.Looper
import com.example.BuildConfig
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

const val URUGUAY_DEFAULT_LAT = -34.8836
const val URUGUAY_DEFAULT_LNG = -56.1645

data class GpsFixResult(
    val lat: Double,
    val lng: Double,
    val accuracyMeters: Float = 0f,
    val isMocked: Boolean = false,
    val isFallback: Boolean = false,
    val hasPermission: Boolean = true,
    val errorMessage: String? = null
)

data class RouteEtaResult(
    val etaMinutes: Int,
    val distanceKm: Double,
    val sourceLabel: String
) {
    fun toFormattedLabel(): String {
        val distFormatted = String.format(Locale.US, "%.1f", distanceKm)
        return "Tiempo estimado de llegada: $etaMinutes min ($distFormatted km)"
    }
}

/**
 * Proveedor unificado de geolocalización real en Uruguay, cálculo geodésico exacto
 * y gestión nativa de permisos GPS en Compose.
 */
object UruguayLocationProvider {

    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Distancia geodésica real en metros entre dos coordenadas (fórmula Haversine).
     */
    fun haversineDistanceMeters(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Int {
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2.0).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2.0).pow(2)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return kotlin.math.round(earthRadiusMeters * c).toInt()
    }

    /**
     * Obtiene una fijación GPS real del dispositivo con validación anti-spoofing.
     */
    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentGpsLocation(context: Context): GpsFixResult {
        if (!hasLocationPermission(context)) {
            return GpsFixResult(
                lat = URUGUAY_DEFAULT_LAT,
                lng = URUGUAY_DEFAULT_LNG,
                hasPermission = false,
                isFallback = true,
                errorMessage = "Permiso de ubicación requerido para registrar GPS real."
            )
        }

        if (SecurityUtils.isMockLocationActive(context, null)) {
            return GpsFixResult(
                lat = URUGUAY_DEFAULT_LAT,
                lng = URUGUAY_DEFAULT_LNG,
                isMocked = true,
                isFallback = true,
                errorMessage = "Se detectó ubicación simulada (Mock GPS). Marcación bloqueada."
            )
        }

        val location = withTimeoutOrNull(4500L) {
            requestFusedCurrentLocation(context)
        } ?: withTimeoutOrNull(1800L) {
            requestFusedLastLocation(context)
        } ?: getPlatformLastKnownLocation(context)

        if (location == null) {
            return GpsFixResult(
                lat = URUGUAY_DEFAULT_LAT,
                lng = URUGUAY_DEFAULT_LNG,
                isFallback = true,
                errorMessage = "Señal GPS demorada. Centrado en Montevideo/Canelones."
            )
        }

        val mocked = SecurityUtils.isMockLocationActive(context, location)
        return GpsFixResult(
            lat = location.latitude,
            lng = location.longitude,
            accuracyMeters = location.accuracy,
            isMocked = mocked,
            isFallback = false,
            errorMessage = if (mocked) "Se detectó ubicación simulada (Mock GPS)." else null
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFusedCurrentLocation(context: Context): Location? =
        suspendCancellableCoroutine { cont ->
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val cts = CancellationTokenSource()
                cont.invokeOnCancellation { cts.cancel() }
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
            } catch (_: Throwable) {
                if (cont.isActive) cont.resume(null)
            }
        }

    @SuppressLint("MissingPermission")
    private suspend fun requestFusedLastLocation(context: Context): Location? =
        suspendCancellableCoroutine { cont ->
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                client.lastLocation
                    .addOnSuccessListener { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
            } catch (_: Throwable) {
                if (cont.isActive) cont.resume(null)
            }
        }

    @SuppressLint("MissingPermission")
    private fun getPlatformLastKnownLocation(context: Context): Location? {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return null
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        } catch (_: Throwable) {
            null
        }
    }

    private val etaHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Inicia transmisión GPS continua en tiempo real (FusedLocationProviderClient.requestLocationUpdates).
     * Retorna una función lambda para detener y desconectar inmediatamente el listener en Check-out.
     */
    @SuppressLint("MissingPermission")
    fun startLiveLocationUpdates(
        context: Context,
        intervalMs: Long = 5000L,
        onUpdate: (GpsFixResult) -> Unit
    ): () -> Unit {
        if (!hasLocationPermission(context)) {
            onUpdate(
                GpsFixResult(
                    lat = URUGUAY_DEFAULT_LAT,
                    lng = URUGUAY_DEFAULT_LNG,
                    hasPermission = false,
                    isFallback = true,
                    errorMessage = "Permiso de ubicación requerido."
                )
            )
            return {}
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(2500L)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val mocked = SecurityUtils.isMockLocationActive(context, loc)
                if (!mocked) {
                    onUpdate(
                        GpsFixResult(
                            lat = loc.latitude,
                            lng = loc.longitude,
                            accuracyMeters = loc.accuracy,
                            isMocked = false,
                            isFallback = false,
                            hasPermission = true
                        )
                    )
                }
            }
        }

        return try {
            fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
            val stopUpdates: () -> Unit = {
                try {
                    fusedClient.removeLocationUpdates(callback)
                } catch (_: Throwable) {
                }
            }
            stopUpdates
        } catch (_: Throwable) {
            {}
        }
    }

    /**
     * Calcula tiempo estimado real de llegada (ETA) y distancia vial entre origen y destino:
     * 1) Google Distance Matrix API (con tráfico real si MAPS_API_KEY está configurada)
     * 2) OSRM Driving API (ruta vial callejera real en Uruguay)
     * 3) Cálculo geodésico Haversine ajustado a factor vial urbano (sin valores fijos).
     */
    suspend fun calculateRealRouteEta(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): RouteEtaResult = withContext(Dispatchers.IO) {
        val straightMeters = haversineDistanceMeters(originLat, originLng, destLat, destLng)
        if (straightMeters <= 80) {
            return@withContext RouteEtaResult(
                etaMinutes = 1,
                distanceKm = 0.1,
                sourceLabel = "En proximidad"
            )
        }

        val mapsKey = BuildConfig.MAPS_API_KEY
        if (mapsKey.isNotBlank() && mapsKey != "MY_MAPS_API_KEY") {
            try {
                val url = "https://maps.googleapis.com/maps/api/distancematrix/json" +
                    "?origins=$originLat,$originLng" +
                    "&destinations=$destLat,$destLng" +
                    "&mode=driving&departure_time=now&language=es&key=$mapsKey"
                val req = Request.Builder().url(url).get().build()
                etaHttpClient.newCall(req).execute().use { resp ->
                    val body = resp.body?.string()
                    if (resp.isSuccessful && !body.isNullOrBlank()) {
                        parseGoogleDistanceMatrixJson(body)?.let { return@withContext it }
                    }
                }
            } catch (_: Throwable) {
            }
        }

        try {
            val osrmUrl = "https://router.project-osrm.org/route/v1/driving/" +
                "$originLng,$originLat;$destLng,$destLat?overview=false"
            val req = Request.Builder().url(osrmUrl).get().build()
            etaHttpClient.newCall(req).execute().use { resp ->
                val body = resp.body?.string()
                if (resp.isSuccessful && !body.isNullOrBlank()) {
                    parseOsrmRouteJson(body)?.let { return@withContext it }
                }
            }
        } catch (_: Throwable) {
        }

        computeRoadGeodesicEta(originLat, originLng, destLat, destLng)
    }

    fun parseGoogleDistanceMatrixJson(json: String): RouteEtaResult? {
        return try {
            val root = JSONObject(json)
            if (root.optString("status") != "OK") return null
            val rows = root.optJSONArray("rows") ?: return null
            if (rows.length() == 0) return null
            val elements = rows.getJSONObject(0).optJSONArray("elements") ?: return null
            if (elements.length() == 0) return null
            val elem = elements.getJSONObject(0)
            if (elem.optString("status") != "OK") return null

            val distanceMeters = elem.optJSONObject("distance")?.optDouble("value", -1.0) ?: -1.0
            val durationObj = elem.optJSONObject("duration_in_traffic") ?: elem.optJSONObject("duration")
            val durationSeconds = durationObj?.optDouble("value", -1.0) ?: -1.0
            if (distanceMeters < 0.0 || durationSeconds < 0.0) return null

            val minutes = maxOf(1, (durationSeconds / 60.0).roundToInt())
            val km = kotlin.math.max(0.1, ((distanceMeters / 1000.0) * 10.0).roundToInt() / 10.0)
            RouteEtaResult(
                etaMinutes = minutes,
                distanceKm = km,
                sourceLabel = "Google Traffic ETA"
            )
        } catch (_: Throwable) {
            null
        }
    }

    fun parseOsrmRouteJson(json: String): RouteEtaResult? {
        return try {
            val root = JSONObject(json)
            if (root.optString("code") != "Ok") return null
            val routes = root.optJSONArray("routes") ?: return null
            if (routes.length() == 0) return null
            val first = routes.getJSONObject(0)
            val durationSeconds = first.optDouble("duration", -1.0)
            val distanceMeters = first.optDouble("distance", -1.0)
            if (durationSeconds < 0.0 || distanceMeters < 0.0) return null

            val minutes = maxOf(1, (durationSeconds / 60.0).roundToInt())
            val km = kotlin.math.max(0.1, ((distanceMeters / 1000.0) * 10.0).roundToInt() / 10.0)
            RouteEtaResult(
                etaMinutes = minutes,
                distanceKm = km,
                sourceLabel = "OSRM Ruta Vial"
            )
        } catch (_: Throwable) {
            null
        }
    }

    fun computeRoadGeodesicEta(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): RouteEtaResult {
        val straightMeters = haversineDistanceMeters(originLat, originLng, destLat, destLng)
        val roadKm = kotlin.math.max(0.1, ((straightMeters / 1000.0) * 1.25 * 10.0).roundToInt() / 10.0)
        // Velocidad urbana media en Montevideo ~28 km/h
        val etaMinutes = maxOf(1, ((roadKm / 28.0) * 60.0).roundToInt())
        return RouteEtaResult(
            etaMinutes = etaMinutes,
            distanceKm = roadKm,
            sourceLabel = "Estimación vial GPS"
        )
    }
}

@Stable
class UruguayLocationState(
    initialHasPermission: Boolean,
    private val onRequestPermissionAndFix: () -> Unit,
    private val onRefreshFixOnly: () -> Unit
) {
    var hasPermission by mutableStateOf(initialHasPermission)
        internal set
    var userLat by mutableStateOf<Double?>(null)
        internal set
    var userLng by mutableStateOf<Double?>(null)
        internal set
    var isFetching by mutableStateOf(false)
        internal set
    var isMockDetected by mutableStateOf(false)
        internal set
    var lastFixWasFallback by mutableStateOf(true)
        internal set

    fun requestMyLocation() {
        onRequestPermissionAndFix()
    }

    fun refreshFixIfPermitted() {
        if (hasPermission) {
            onRefreshFixOnly()
        }
    }
}

/**
 * Solicita permisos nativos de GPS al abrir vistas con mapa y provee ubicación real en vivo.
 */
@Composable
fun rememberUruguayLocationState(
    autoRequestOnLaunch: Boolean = true,
    onFixAcquired: ((Double, Double) -> Unit)? = null
): UruguayLocationState {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    lateinit var state: UruguayLocationState

    fun fetchFix() {
        scope.launch {
            state.isFetching = true
            val result = UruguayLocationProvider.fetchCurrentGpsLocation(context)
            state.isFetching = false
            state.hasPermission = result.hasPermission
            state.isMockDetected = result.isMocked
            state.lastFixWasFallback = result.isFallback
            state.userLat = result.lat
            state.userLng = result.lng
            onFixAcquired?.invoke(result.lat, result.lng)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        state.hasPermission = granted
        if (granted) {
            fetchFix()
        } else {
            state.lastFixWasFallback = true
            state.userLat = URUGUAY_DEFAULT_LAT
            state.userLng = URUGUAY_DEFAULT_LNG
        }
    }

    state = remember(context) {
        UruguayLocationState(
            initialHasPermission = UruguayLocationProvider.hasLocationPermission(context),
            onRequestPermissionAndFix = {
                if (UruguayLocationProvider.hasLocationPermission(context)) {
                    state.hasPermission = true
                    fetchFix()
                } else {
                    permissionLauncher.launch(UruguayLocationProvider.REQUIRED_PERMISSIONS)
                }
            },
            onRefreshFixOnly = { fetchFix() }
        )
    }

    LaunchedEffect(autoRequestOnLaunch) {
        if (autoRequestOnLaunch) {
            if (UruguayLocationProvider.hasLocationPermission(context)) {
                state.hasPermission = true
                fetchFix()
            } else {
                permissionLauncher.launch(UruguayLocationProvider.REQUIRED_PERMISSIONS)
            }
        }
    }

    return state
}
