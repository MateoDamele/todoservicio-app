package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PriceCheck
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import com.example.util.RouteEtaResult
import com.example.util.UruguayLocationProvider
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.OperationalRepository
import com.example.model.ActiveTechnicianTracking
import com.example.model.ChatSenderRole
import com.example.model.ClientNotification
import com.example.model.PendingQuote
import com.example.ui.components.ApplyWindowBackdropBlur
import com.example.ui.components.GlassDialogEntrance
import com.example.ui.components.ServiceChatPanel
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.NaranjaAlerta
import com.example.ui.theme.NaranjaAlertaFondo
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RojoUrgencia
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo

/**
 * Modal interactivo: Seguimiento en Vivo del Técnico (Estilo Uber)
 */
@Composable
fun LiveTrackingModal(
    tracking: ActiveTechnicianTracking,
    clientSenderName: String = "Cliente",
    isChatActive: Boolean = true,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val chatMessagesMap by OperationalRepository.serviceChatMessages.collectAsStateWithLifecycle()

    var dynamicEtaMinutes by remember(tracking.serviceCode) { mutableIntStateOf(tracking.etaMinutes.coerceAtLeast(1)) }
    var dynamicDistanceKm by remember(tracking.serviceCode) { mutableDoubleStateOf(tracking.distanceKm) }
    var etaSourceLabel by remember(tracking.serviceCode) { mutableStateOf(tracking.etaSource) }

    LaunchedEffect(
        tracking.serviceCode,
        tracking.currentLatitude,
        tracking.currentLongitude,
        tracking.destinationLatitude,
        tracking.destinationLongitude,
        tracking.isEnCamino
    ) {
        if (tracking.isEnCamino) {
            val eta = UruguayLocationProvider.calculateRealRouteEta(
                originLat = tracking.currentLatitude,
                originLng = tracking.currentLongitude,
                destLat = tracking.destinationLatitude,
                destLng = tracking.destinationLongitude
            )
            dynamicEtaMinutes = eta.etaMinutes
            dynamicDistanceKm = eta.distanceKm
            etaSourceLabel = eta.sourceLabel
        }
    }

    val formattedEtaBanner = remember(dynamicEtaMinutes, dynamicDistanceKm) {
        RouteEtaResult(dynamicEtaMinutes, dynamicDistanceKm, etaSourceLabel).toFormattedLabel()
    }

    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header con botón cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CelesteAcentoClaro
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Navigation,
                                contentDescription = null,
                                tint = CelesteAcento,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "MONITOREO EN VIVO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulCorporativo
                                )
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar",
                            tint = GrisTextoSecundario
                        )
                    }
                }

                Text(
                    text = tracking.serviceTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )

                // Simulación visual del Mapa de Ruta en vivo (Montevideo)
                // Visor de Mapa de Calles Real con Ruta en Vivo
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE9EEF5),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val destAddress = tracking.destinationAddress.ifBlank { tracking.location.ifBlank { "Montevideo, Uruguay" } }
                        val clientGeofences = remember(tracking) {
                            listOf(
                                com.example.ui.components.MapGeocenceItem(
                                    id = "dest-sede",
                                    name = tracking.location.ifBlank { "Sede Cliente" },
                                    address = destAddress,
                                    lat = tracking.destinationLatitude,
                                    lng = tracking.destinationLongitude,
                                    radiusMeters = 80
                                )
                            )
                        }
                        val clientMarkers = remember(tracking) {
                            listOf(
                                com.example.ui.components.MapMarkerItem(
                                    id = "tech-van",
                                    title = tracking.technicianName.ifBlank { "Técnico asignado" },
                                    subtitle = "En camino hacia ${tracking.serviceTitle}",
                                    lat = tracking.currentLatitude,
                                    lng = tracking.currentLongitude,
                                    type = "TECH",
                                    badgeText = "🚐",
                                    timeText = "$dynamicEtaMinutes min • ${String.format(Locale.US, "%.1f", dynamicDistanceKm)} km"
                                ),
                                com.example.ui.components.MapMarkerItem(
                                    id = "client-dest",
                                    title = "Destino: ${tracking.location.ifBlank { "Sede Cliente" }}",
                                    subtitle = destAddress,
                                    lat = tracking.destinationLatitude,
                                    lng = tracking.destinationLongitude,
                                    type = "CLIENT",
                                    badgeText = "🏢",
                                    timeText = "Llegada estimada"
                                )
                            )
                        }

                        val clientRoute = remember(tracking) {
                            val midLat = (tracking.currentLatitude + tracking.destinationLatitude) / 2.0
                            val midLng = (tracking.currentLongitude + tracking.destinationLongitude) / 2.0
                            listOf(
                                tracking.currentLatitude to tracking.currentLongitude,
                                midLat to midLng,
                                tracking.destinationLatitude to tracking.destinationLongitude
                            )
                        }

                        com.example.ui.components.RealStreetInteractiveMap(
                            centerLat = (tracking.currentLatitude + tracking.destinationLatitude) / 2.0,
                            centerLng = (tracking.currentLongitude + tracking.destinationLongitude) / 2.0,
                            zoomLevel = 14,
                            geofences = clientGeofences,
                            markers = clientMarkers,
                            routePolyline = clientRoute,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Badge flotante ETA dinámico real
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AzulMarca,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Speed,
                                    contentDescription = null,
                                    tint = CelesteAcento,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = formattedEtaBanner,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco
                                    )
                                )
                            }
                        }
                    }
                }

                com.example.ui.components.MapsGroundingAssistantCard(
                    defaultQuery = tracking.destinationAddress.ifBlank { tracking.location.ifBlank { "Montevideo, Uruguay" } },
                    lat = tracking.destinationLatitude,
                    lng = tracking.destinationLongitude,
                    title = "Referencias de la Sede"
                )

                // Datos del Técnico y Vehículo
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GrisNeutro,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(CelesteAcentoClaro, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Engineering,
                                    contentDescription = null,
                                    tint = AzulCorporativo,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tracking.technicianName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca
                                    )
                                )
                                Text(
                                    text = tracking.technicianRole,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario
                                    )
                                )
                                Text(
                                    text = "${tracking.vehicleModel} • ${tracking.vehiclePlate}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = AzulCorporativo,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        androidx.compose.material3.HorizontalDivider(color = GrisBorde.copy(alpha = 0.6f))

                        Text(
                            text = "Destino: ${tracking.location}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = AzulMarca,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Botones de Contacto Directo con el Técnico
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${tracking.technicianPhone.replace(" ", "")}")
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Llamar", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                    }

                    Button(
                        onClick = {
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=${tracking.technicianPhone.replace(" ", "").replace("+", "")}&text=Hola%20${Uri.encode(tracking.technicianName)},%20te%20escribo%20desde%20el%20Edificio%20para%20coordinar%20el%20acceso.")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeExito),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                    }
                }

                // Chat directo en tiempo real vinculado a la OT activa
                ServiceChatPanel(
                    orderCode = tracking.serviceCode,
                    messages = chatMessagesMap[tracking.serviceCode].orEmpty(),
                    currentRole = ChatSenderRole.CLIENTE,
                    counterpartyLabel = tracking.technicianName,
                    isChatActive = isChatActive,
                    isCompleted = !isChatActive,
                    initiallyExpanded = true,
                    onSendMessage = { msg ->
                        OperationalRepository.sendServiceChatMessage(
                            orderCode = tracking.serviceCode,
                            senderRole = ChatSenderRole.CLIENTE,
                            senderName = clientSenderName,
                            rawMessage = msg
                        )
                    }
                )
            }
        }
        }
    }
}

/**
 * Modal interactivo: Consulta Técnica Exprés (Cámara / Video / Mensaje directo)
 */
@Composable
fun ExpressConsultationModal(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    var queryText by remember { mutableStateOf("") }
    var attachedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            attachedMediaUri = uri
            Toast.makeText(context, "Evidencia adjuntada correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(CelesteAcentoClaro, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Videocam,
                                contentDescription = null,
                                tint = CelesteAcento,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = "Consulta Exprés",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario)
                    }
                }

                if (isSubmitted) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VerdeExitoFondo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = VerdeExito)
                                Text(
                                    text = "¡Consulta Enviada!",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = VerdeExito
                                )
                            }
                            Text(
                                text = "Un ingeniero de guardia revisará su mensaje y responderá a la brevedad.",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = AzulMarca
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar", fontFamily = PoppinsFontFamily, color = Blanco)
                    }
                } else {
                    Text(
                        text = "Envíe una imagen o consulta rápida al equipo técnico.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario
                        )
                    )

                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        placeholder = { Text("Detalle su consulta técnica...", fontFamily = PoppinsFontFamily) },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (attachedMediaUri != null) VerdeExitoFondo else GrisNeutro,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (attachedMediaUri != null) VerdeExito.copy(alpha = 0.5f) else GrisBorde
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                mediaPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            }
                            .testTag("btn_express_attach_media")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (attachedMediaUri != null) Icons.Rounded.CheckCircle else Icons.Rounded.CameraAlt,
                                contentDescription = null,
                                tint = if (attachedMediaUri != null) VerdeExito else CelesteAcento
                            )
                            Text(
                                text = if (attachedMediaUri != null) {
                                    "Archivo adjunto: ${attachedMediaUri?.lastPathSegment ?: "Evidencia lista"}"
                                } else {
                                    "Adjuntar Foto o Video (Opcional)"
                                },
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = if (attachedMediaUri != null) VerdeExito else AzulCorporativo,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (queryText.isBlank()) {
                                Toast.makeText(context, "Ingrese una consulta antes de enviar", Toast.LENGTH_SHORT).show()
                            } else {
                                OperationalRepository.addNotification("Consulta Exprés Cliente: ${queryText.take(60)}")
                                isSubmitted = true
                                onSubmit()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_express_submit")
                    ) {
                        Icon(imageVector = Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar Consulta Inmediata", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                    }
                }
            }
        }
        }
    }
}

/**
 * Modal interactivo: Ver y Aprobar Cotización Pendiente
 */
@Composable
fun QuoteApprovalModal(
    quote: PendingQuote,
    onDismiss: () -> Unit,
    onApprove: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NaranjaAlertaFondo
                    ) {
                        Text(
                            text = quote.quoteCode,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = NaranjaAlerta
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario)
                    }
                }

                Text(
                    text = quote.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GrisNeutro,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Monto Total:", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                            Text(
                                "USD ${quote.amountUsd}",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontSize = 18.sp
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Validez de la Oferta:", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                            Text(quote.validUntil, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium, color = AzulCorporativo)
                        }
                    }
                }

                Text(
                    text = "Alcance Técnico:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )

                Text(
                    text = quote.scopeDescription,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario,
                        lineHeight = 20.sp
                    )
                )

                if (quote.isApproved) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VerdeExitoFondo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = VerdeExito)
                            Text(
                                text = "Cotización Aprobada. Cuadrilla coordinada.",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = VerdeExito
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeExito),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_confirm_approve_quote")
                    ) {
                        Icon(imageVector = Icons.Rounded.PriceCheck, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aprobar Cotización Oficial", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                    }
                }
            }
        }
        }
    }
}

/**
 * Diálogo de Notificaciones del Portal Cliente
 */
@Composable
fun ClientNotificationsDialog(
    notifications: List<ClientNotification>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Notifications, contentDescription = null, tint = CelesteAcento)
                        Text(
                            text = "Centro de Avisos",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario)
                    }
                }

                if (notifications.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GrisNeutro,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = null,
                                tint = GrisTextoSecundario.copy(alpha = 0.55f),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Sin avisos pendientes",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 13.sp,
                                color = GrisTextoSecundario,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    notifications.forEach { notif ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (notif.isUnread) CelesteAcentoClaro.copy(alpha = 0.5f) else GrisNeutro,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = notif.title,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = AzulMarca
                                    )
                                    Text(
                                        text = notif.timeAgo,
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 10.sp,
                                        color = GrisTextoSecundario
                                    )
                                }
                                Text(
                                    text = notif.message,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.sp,
                                    color = GrisTextoSecundario
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", fontFamily = PoppinsFontFamily, color = Blanco)
                }
            }
        }
        }
    }
}
