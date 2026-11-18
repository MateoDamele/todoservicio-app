package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.LocalDatabaseStore
import com.example.data.OperationalRepository
import com.example.model.AnnouncementItem
import com.example.model.AnnouncementPriority
import com.example.model.AnnouncementTargetRole
import com.example.model.ClientNotification
import com.example.model.UserAccount
import com.example.ui.theme.AmbarCalido
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.PoppinsFontFamily
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

private val PriorityImportantRed = Color(0xFFEF4444)
private val PriorityMediumAmber = Color(0xFFF59E0B)
private val PriorityLowSlate = Color(0xFF335C81)

/**
 * Ícono limpio y blanco de campanita para cabeceras (Limpieza, Técnicos, Clientes)
 * con círculo rojo (#EF4444) cuando existen avisos o alertas sin leer.
 */
@Composable
fun HeaderNotificationBell(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .testTag("btn_header_notification_bell"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Avisos y notificaciones",
            tint = Blanco,
            modifier = Modifier.size(22.dp)
        )

        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .size(if (unreadCount > 9) 17.dp else 15.dp)
                    .background(PriorityImportantRed, CircleShape)
                    .border(1.2.dp, AzulMarca, CircleShape)
                    .testTag("bell_unread_badge"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                    color = Blanco,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Solicita permiso POST_NOTIFICATIONS en Android 13+ de forma no intrusiva al entrar a la sesión.
 */
@Composable
fun RequestAndroidNotificationPermissionEffect() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

/**
 * Modal de Administración para publicar Avisos / Comunicados segmentados con prioridad,
 * imagen adjunta y notificación push local inmediata.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateAnnouncementDialog(
    authorName: String,
    onDismiss: () -> Unit,
    onAnnouncementPublished: (AnnouncementItem) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(AnnouncementPriority.MEDIO) }
    val selectedTargets = remember {
        mutableStateListOf(
            AnnouncementTargetRole.LIMPIEZA,
            AnnouncementTargetRole.TECNICOS
        )
    }
    var attachedImageUri by remember { mutableStateOf<String?>(null) }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedImageUri = LocalDatabaseStore.persistPickedImageUri(context, uri)
        }
    }

    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            attachedImageUri = persistCameraBitmap(context, bitmap)
        }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraPreviewLauncher.launch(null)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val previewBitmap = rememberPersistedBitmap(attachedImageUri)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.93f)
                    .heightIn(max = 680.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cabecera del Modal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(AzulMarca.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Campaign,
                                    contentDescription = null,
                                    tint = AzulMarca,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Nuevo Aviso / Comunicado",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario)
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título del aviso") },
                        placeholder = { Text("ej. Cambio de protocolo de acceso") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulCorporativo,
                            unfocusedBorderColor = GrisBorde
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("announcement_title_input")
                    )

                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Cuerpo del mensaje") },
                        placeholder = { Text("Redactá el comunicado para el equipo o clientes...") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulCorporativo,
                            unfocusedBorderColor = GrisBorde
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("announcement_body_input")
                    )

                    // Adjuntar Imagen (Opcional)
                    Text(
                        text = "Imagen Adjunta (opcional)",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AzulMarca
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                galleryPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AzulCorporativo),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_announcement_pick_image")
                        ) {
                            Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = AzulCorporativo, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Subir Imagen",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = AzulMarca,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val hasCam = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasCam) {
                                    cameraPreviewLauncher.launch(null)
                                } else {
                                    cameraPermLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier.weight(0.75f)
                        ) {
                            Icon(Icons.Rounded.PhotoCamera, contentDescription = null, tint = AzulCorporativo, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Cámara",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = AzulMarca,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                        }
                    }

                    if (previewBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, GrisBorde, RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                bitmap = previewBitmap,
                                contentDescription = "Vista previa adjunta",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .clickable { attachedImageUri = null }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Quitar imagen",
                                        tint = Blanco,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Nivel de Prioridad (selección única)
                    Text(
                        text = "Nivel de Prioridad",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AzulMarca
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnnouncementPriority.values().forEach { priority ->
                            val isSelected = selectedPriority == priority
                            val accent = priorityColor(priority)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) accent.copy(alpha = 0.14f) else GrisNeutro,
                                border = BorderStroke(1.3.dp, if (isSelected) accent else GrisBorde),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedPriority = priority }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(accent, CircleShape)
                                    )
                                    Text(
                                        text = priority.label,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) accent else GrisTextoSecundario,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Destinatarios (selección múltiple)
                    Text(
                        text = "Destinatarios",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AzulMarca
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnnouncementTargetRole.values().forEach { target ->
                            val isSelected = selectedTargets.contains(target)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AzulMarca else GrisNeutro,
                                border = BorderStroke(1.dp, if (isSelected) AzulMarca else GrisBorde),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (isSelected) selectedTargets.remove(target)
                                        else selectedTargets.add(target)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = Blanco,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Text(
                                        text = target.label,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Blanco else AzulMarca,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Button(
                        onClick = {
                            if (title.isBlank() || body.isBlank()) {
                                Toast.makeText(context, "Completá título y mensaje del aviso", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedTargets.isEmpty()) {
                                Toast.makeText(context, "Seleccioná al menos un grupo destinatario", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            val published = OperationalRepository.publishAnnouncement(
                                context = context,
                                title = title,
                                body = body,
                                priority = selectedPriority,
                                targetRoles = selectedTargets.toList(),
                                imageUri = attachedImageUri,
                                authorName = authorName
                            )
                            onAnnouncementPublished(published)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_publish_announcement")
                    ) {
                        Icon(Icons.Rounded.Campaign, contentDescription = null, tint = Blanco, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Publicar y Notificar",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modal con efecto Blur para visualizar los avisos ordenados por fecha y prioridad,
 * ampliar imágenes adjuntas y marcar los avisos como leídos al abrir/cerrar.
 */
@Composable
fun AnnouncementsInboxModal(
    currentUser: UserAccount,
    announcements: List<AnnouncementItem>,
    extraAlerts: List<ClientNotification> = emptyList(),
    onDismiss: () -> Unit
) {
    var enlargedImageUri by remember { mutableStateOf<String?>(null) }

    val sortedAnnouncements = remember(announcements) {
        announcements.sortedWith(
            compareByDescending<AnnouncementItem> { it.createdAtMillis / 86_400_000L }
                .thenByDescending { it.priority.sortWeight }
                .thenByDescending { it.createdAtMillis }
        )
    }

    LaunchedEffect(currentUser.uid, currentUser.role, announcements.size) {
        OperationalRepository.markAnnouncementsAsRead(currentUser.uid, currentUser.role)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.93f)
                    .heightIn(max = 660.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = AzulMarca,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Avisos y Comunicados",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (sortedAnnouncements.isEmpty() && extraAlerts.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = GrisNeutro,
                                border = BorderStroke(1.dp, GrisBorde),
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
                                        imageVector = Icons.Outlined.Notifications,
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
                            sortedAnnouncements.forEach { item ->
                                val badgeColor = priorityColor(item.priority)
                                val attachedBitmap = rememberPersistedBitmap(item.imageUri)

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = GrisNeutro,
                                    border = BorderStroke(1.2.dp, badgeColor.copy(alpha = 0.45f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = badgeColor.copy(alpha = 0.14f),
                                                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
                                            ) {
                                                Text(
                                                    text = item.priority.label,
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = badgeColor,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = item.createdAtLabel,
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 10.sp,
                                                color = GrisTextoSecundario
                                            )
                                        }

                                        Text(
                                            text = item.title,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = AzulMarca
                                        )

                                        Text(
                                            text = item.body,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 12.sp,
                                            color = GrisTextoSecundario,
                                            lineHeight = 17.sp
                                        )

                                        if (attachedBitmap != null && item.imageUri != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(135.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(1.dp, GrisBorde, RoundedCornerShape(10.dp))
                                                    .clickable { enlargedImageUri = item.imageUri }
                                            ) {
                                                Image(
                                                    bitmap = attachedBitmap,
                                                    contentDescription = "Imagen adjunta del aviso",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color.Black.copy(alpha = 0.55f),
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(6.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        Icon(Icons.Rounded.ZoomIn, contentDescription = null, tint = Blanco, modifier = Modifier.size(12.dp))
                                                        Text("Ampliar", color = Blanco, fontSize = 9.5.sp, fontFamily = PoppinsFontFamily)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            extraAlerts.forEach { notif ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (notif.isUnread) AzulCorporativo.copy(alpha = 0.08f) else GrisNeutro,
                                    border = BorderStroke(1.dp, GrisBorde),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = notif.title,
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = AzulMarca,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
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
                                            fontSize = 11.5.sp,
                                            color = GrisTextoSecundario
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Entendido", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                    }
                }
            }
        }
    }

    // Visor de Imagen Adjunta en Grande
    enlargedImageUri?.let { uriStr ->
        val fullBitmap = rememberPersistedBitmap(uriStr)
        if (fullBitmap != null) {
            Dialog(
                onDismissRequest = { enlargedImageUri = null },
                properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
            ) {
                ApplyWindowBackdropBlur()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.72f))
                        .clickable { enlargedImageUri = null }
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = fullBitmap,
                        contentDescription = "Imagen del comunicado ampliada",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Surface(
                        shape = CircleShape,
                        color = Blanco,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 24.dp, end = 6.dp)
                            .size(40.dp)
                            .clickable { enlargedImageUri = null }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Close, contentDescription = "Cerrar imagen", tint = AzulMarca)
                        }
                    }
                }
            }
        }
    }
}

private fun priorityColor(priority: AnnouncementPriority): Color {
    return when (priority) {
        AnnouncementPriority.IMPORTANTE -> PriorityImportantRed
        AnnouncementPriority.MEDIO -> PriorityMediumAmber
        AnnouncementPriority.BAJO -> PriorityLowSlate
    }
}

private fun persistCameraBitmap(context: Context, bitmap: Bitmap): String? {
    return try {
        val mediaDir = File(context.filesDir, "persisted_media").apply {
            if (!exists()) mkdirs()
        }
        val targetFile = File(mediaDir, "cam_${UUID.randomUUID()}.jpg")
        FileOutputStream(targetFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        if (targetFile.exists() && targetFile.length() > 0L) {
            Uri.fromFile(targetFile).toString()
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
