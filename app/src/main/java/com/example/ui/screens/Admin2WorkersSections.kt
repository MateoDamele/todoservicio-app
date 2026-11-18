package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.PrecisionManufacturing
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.OperationalRepository
import com.example.model.AdminClientRecord
import com.example.model.ClientEquipmentAsset
import com.example.model.MasterServiceCatalogItem
import com.example.model.MetricCheckInRecord
import com.example.model.PendingQuote
import com.example.ui.theme.AmarilloAdvertencia
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RojoError
import com.example.ui.theme.VerdeExito
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.util.PdfReportGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

// ============================================================================
// 1. CATÁLOGO DE EQUIPOS / ACTIVOS POR CLIENTE (2WORKERS)
// ============================================================================
@Composable
fun AdminEquipmentCatalogSection(
    clientRecords: List<AdminClientRecord>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val equipmentList by OperationalRepository.equipmentAssets.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Todas") }
    var showNewEquipmentModal by remember { mutableStateOf(false) }

    val categories = listOf("Todas", "HVAC / Climatización", "Tablero Eléctrico", "Sanitaria / Bombas", "Generación de Emergencia")

    val filteredList = equipmentList.filter { asset ->
        val matchesSearch = asset.name.contains(searchQuery, ignoreCase = true) ||
                asset.assetCode.contains(searchQuery, ignoreCase = true) ||
                asset.clientCompanyName.contains(searchQuery, ignoreCase = true) ||
                asset.locationInBuilding.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "Todas" || asset.category.contains(selectedCategoryFilter.take(4), ignoreCase = true)
        matchesSearch && matchesCategory
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Catálogo de Equipos y Activos",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )
                Text(
                    text = "Gestión de maquinaria y activos edilicios vinculados por cliente",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario
                    )
                )
            }

            Button(
                onClick = { showNewEquipmentModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento, contentColor = Blanco),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "+ Nuevo Equipo",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                )
            }
        }

        // Buscador y Filtros
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar por código (#0827...), equipo o cliente...", fontFamily = PoppinsFontFamily) },
            leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null, tint = GrisTextoSecundario) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CelesteAcento,
                unfocusedBorderColor = GrisBorde,
                focusedContainerColor = Blanco,
                unfocusedContainerColor = Blanco
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Chips de categoría
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategoryFilter == cat,
                    onClick = { selectedCategoryFilter = cat },
                    label = { Text(cat, fontFamily = PoppinsFontFamily, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AzulMarca,
                        selectedLabelColor = Blanco,
                        containerColor = Blanco,
                        labelColor = AzulCorporativo
                    ),
                    border = BorderStroke(1.dp, if (selectedCategoryFilter == cat) AzulMarca else GrisBorde)
                )
            }
        }

        // Resumen métrico
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EquipmentSummaryCard(
                title = "Total Equipos",
                count = equipmentList.size.toString(),
                icon = Icons.Rounded.PrecisionManufacturing,
                color = AzulMarca,
                modifier = Modifier.weight(1f)
            )
            EquipmentSummaryCard(
                title = "Operativos",
                count = equipmentList.count { it.status == "Operativo" }.toString(),
                icon = Icons.Rounded.CheckCircle,
                color = VerdeExito,
                modifier = Modifier.weight(1f)
            )
            EquipmentSummaryCard(
                title = "En Alerta / Mantenimiento",
                count = equipmentList.count { it.status != "Operativo" }.toString(),
                icon = Icons.Rounded.Warning,
                color = AmarilloAdvertencia,
                modifier = Modifier.weight(1f)
            )
        }

        // Listado de Tarjetas de Equipos
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontraron equipos bajo los filtros seleccionados.", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredList.forEach { asset ->
                    EquipmentAssetCard(asset = asset)
                }
            }
        }
    }

    if (showNewEquipmentModal) {
        CreateEquipmentDialog(
            clientRecords = clientRecords,
            onDismiss = { showNewEquipmentModal = false },
            onSave = { newAsset ->
                OperationalRepository.addEquipmentAsset(newAsset)
                showNewEquipmentModal = false
                Toast.makeText(context, "Equipo ${newAsset.assetCode} registrado con éxito.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun EquipmentSummaryCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = BorderStroke(1.dp, GrisBorde),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(text = count, style = MaterialTheme.typography.titleMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca))
                Text(text = title, style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, color = GrisTextoSecundario, fontSize = 11.sp))
            }
        }
    }
}

@Composable
private fun EquipmentAssetCard(asset: ClientEquipmentAsset) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = BorderStroke(1.dp, GrisBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AzulMarca.copy(alpha = 0.08f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Rounded.Build, contentDescription = null, tint = AzulCorporativo, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text(
                            text = asset.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
                        )
                        Text(
                            text = "${asset.brandModel} • Código: ${asset.assetCode}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, color = GrisTextoSecundario, fontSize = 12.sp)
                        )
                    }
                }

                val (badgeBg, badgeText, statusColor) = when (asset.status) {
                    "Operativo" -> Triple(VerdeExito.copy(alpha = 0.15f), "Operativo", VerdeExito)
                    "Alerta" -> Triple(RojoError.copy(alpha = 0.15f), "Alerta", RojoError)
                    else -> Triple(AmarilloAdvertencia.copy(alpha = 0.15f), "En Mant.", AmarilloAdvertencia)
                }

                Surface(shape = RoundedCornerShape(8.dp), color = badgeBg) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = statusColor),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Cliente / Ubicación", style = MaterialTheme.typography.bodySmall.copy(color = GrisTextoSecundario, fontSize = 11.sp))
                    Text(text = asset.clientCompanyName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AzulMarca))
                    Text(text = asset.locationInBuilding, style = MaterialTheme.typography.bodySmall.copy(color = GrisTextoSecundario, fontSize = 11.sp))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Último Service", style = MaterialTheme.typography.bodySmall.copy(color = GrisTextoSecundario, fontSize = 11.sp))
                    Text(text = asset.lastMaintenanceDate, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = AzulCorporativo))
                    Text(text = "Próx: ${asset.nextMaintenanceDate}", style = MaterialTheme.typography.bodySmall.copy(color = CelesteAcento, fontSize = 11.sp))
                }
            }
        }
    }
}

// ============================================================================
// 2. CATÁLOGO DE TAREAS Y SERVICIOS (EXCLUSIVO ADMINISTRACIÓN)
// ============================================================================
@Composable
fun AdminMasterServicesSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val masterServices by OperationalRepository.masterServices.collectAsStateWithLifecycle()
    var showNewServiceModal by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<MasterServiceCatalogItem?>(null) }
    var serviceToDelete by remember { mutableStateOf<MasterServiceCatalogItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Catálogo de Tareas",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
                )
                Text(
                    text = "Tareas y servicios para asignación de órdenes (${masterServices.size})",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                )
            }

            Button(
                onClick = { showNewServiceModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento, contentColor = Blanco),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_new_catalog_task")
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nueva Tarea", style = MaterialTheme.typography.labelMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold))
            }
        }

        if (masterServices.isEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Build, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(34.dp))
                    Text("Sin tareas en catálogo. Toca + Nueva Tarea.", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario, fontSize = 13.sp)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            masterServices.forEach { srv ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(shape = RoundedCornerShape(8.dp), color = AzulCorporativo.copy(alpha = 0.1f)) {
                                    Text(
                                        text = srv.code,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AzulCorporativo),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                if (srv.dec406Compliant) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = VerdeExito.copy(alpha = 0.15f)) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Rounded.Security, contentDescription = null, tint = VerdeExito, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Dec. 406", fontSize = 10.sp, color = VerdeExito, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { serviceToEdit = srv },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("edit_catalog_${srv.code}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Editar ${srv.name}",
                                        tint = AzulCorporativo,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { serviceToDelete = srv },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("delete_catalog_${srv.code}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Eliminar ${srv.name}",
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = srv.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
                        )

                        if (srv.description.isNotBlank()) {
                            Text(
                                text = srv.description,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Duración est: ${srv.estimatedDurationHours} hs",
                                    style = MaterialTheme.typography.bodySmall.copy(color = GrisTextoSecundario, fontSize = 11.sp)
                                )
                            }

                            Text(
                                text = srv.category,
                                style = MaterialTheme.typography.bodySmall.copy(color = AzulCorporativo, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showNewServiceModal) {
        MasterServiceEditorDialog(
            initialItem = null,
            nextIndex = masterServices.size + 1,
            onDismiss = { showNewServiceModal = false },
            onSave = { service ->
                OperationalRepository.addMasterService(service)
                showNewServiceModal = false
                Toast.makeText(context, "Tarea '${service.name}' añadida al catálogo.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    serviceToEdit?.let { editing ->
        MasterServiceEditorDialog(
            initialItem = editing,
            nextIndex = masterServices.size + 1,
            onDismiss = { serviceToEdit = null },
            onSave = { updated ->
                OperationalRepository.updateMasterService(updated)
                serviceToEdit = null
                Toast.makeText(context, "Tarea '${updated.name}' actualizada.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    serviceToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { serviceToDelete = null },
            title = {
                Text(
                    text = "Eliminar tarea del catálogo",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            },
            text = {
                Text(
                    text = "¿Confirmas eliminar '${target.name}' (${target.code}) del catálogo maestro?",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    color = GrisTextoSecundario
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        OperationalRepository.deleteMasterService(target.id)
                        Toast.makeText(context, "Tarea eliminada del catálogo.", Toast.LENGTH_SHORT).show()
                        serviceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Blanco)
                ) {
                    Text("Eliminar", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { serviceToDelete = null }) {
                    Text("Cancelar", color = GrisTextoSecundario)
                }
            }
        )
    }
}

// ============================================================================
// 3. MÉTRICAS Y CHECK-IN DE COLABORADORES (2WORKERS)
// ============================================================================
@Composable
fun AdminMetricsCheckInSection(modifier: Modifier = Modifier) {
    val checkIns by OperationalRepository.metricCheckIns.collectAsStateWithLifecycle()
    val localeEs = remember { Locale("es", "UY") }
    val todayLabel = remember {
        "Hoy ${SimpleDateFormat("d 'de' MMMM", localeEs).format(Date())}"
    }
    val punctualityText = remember(checkIns) {
        if (checkIns.isEmpty()) "0%" else {
            val onTime = checkIns.count { it.punctualityStatus.contains("Puntual", ignoreCase = true) }
            "${(onTime * 100) / checkIns.size}%"
        }
    }
    val totalHoursText = remember(checkIns) {
        val total = checkIns.sumOf { it.totalHoursWorked }
        String.format(Locale.US, "%.1f hs", total)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Métricas y Check-in",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
            )
            Text(
                text = "Asistencia con validación GPS",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
            )
        }

        // Métricas destacadas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Tasa de Puntualidad", style = MaterialTheme.typography.bodySmall.copy(color = GrisTextoSecundario, fontSize = 11.sp))
                    Text(text = punctualityText, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = VerdeExito))
                    Text(text = "Geocercas verificadas", style = MaterialTheme.typography.labelSmall.copy(color = GrisTextoSecundario, fontSize = 10.sp))
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Horas de Campo", style = MaterialTheme.typography.bodySmall.copy(color = GrisTextoSecundario, fontSize = 11.sp))
                    Text(text = totalHoursText, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = AzulMarca))
                    Text(text = todayLabel, style = MaterialTheme.typography.labelSmall.copy(color = GrisTextoSecundario, fontSize = 10.sp))
                }
            }
        }

        Text(
            text = "Registros de Ingreso",
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
        )

        if (checkIns.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Timer, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(34.dp))
                    Text("Sin registros de check-in aún", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario, fontSize = 13.sp)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            checkIns.forEach { rec ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (rec.isGeofenceVerified) VerdeExito.copy(alpha = 0.15f) else AmarilloAdvertencia.copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (rec.isGeofenceVerified) Icons.Rounded.Verified else Icons.Rounded.LocationOn,
                                        contentDescription = null,
                                        tint = if (rec.isGeofenceVerified) VerdeExito else AmarilloAdvertencia,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = rec.collaboratorName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
                                )
                                Text(
                                    text = "${rec.collaboratorRole} • ${rec.destinationCompany}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, color = GrisTextoSecundario, fontSize = 11.sp)
                                )
                                Text(
                                    text = "Entrada: ${rec.checkInTime} ${if (rec.checkOutTime != null) "• Salida: ${rec.checkOutTime}" else "• En Curso"}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, color = AzulCorporativo, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (rec.punctualityStatus.contains("Puntual")) VerdeExito.copy(alpha = 0.12f) else AmarilloAdvertencia.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = rec.punctualityStatus,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (rec.punctualityStatus.contains("Puntual")) VerdeExito else AmarilloAdvertencia,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${rec.totalHoursWorked} hs",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. EXPORTAR REPORTES PDF / EXCEL (2WORKERS)
// ============================================================================
@Composable
fun AdminExportReportsSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val localeEs = remember { Locale("es", "UY") }
    val currentPeriod = remember {
        val raw = SimpleDateFormat("MMMM yyyy", localeEs).format(Date())
        raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
    }
    val currentPeriodCode = remember {
        SimpleDateFormat("MMyy", Locale.US).format(Date())
    }
    var selectedReportType by remember { mutableStateOf("Bitácora Mensual de Trabajos") }
    val selectedPeriod = currentPeriod
    val tasks by OperationalRepository.operationalTasks.collectAsStateWithLifecycle()
    val quotes by OperationalRepository.quotes.collectAsStateWithLifecycle()

    val reportTypes = listOf(
        "Bitácora Mensual de Trabajos",
        "Auditoría Técnica de Seguridad",
        "Planilla de Asistencia & Check-in",
        "Consolidado de Cotizaciones"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Exportar Reportes",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
            )
            Text(
                text = "Documentos oficiales PDF y planillas CSV",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            border = BorderStroke(1.dp, GrisBorde),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Tipo de Documento",
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reportTypes.forEach { type ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedReportType == type) CelesteAcento.copy(alpha = 0.12f) else GrisNeutro,
                            border = BorderStroke(1.dp, if (selectedReportType == type) CelesteAcento else GrisBorde),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReportType = type }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(
                                        imageVector = if (type.contains("Excel") || type.contains("Planilla")) Icons.Rounded.TableChart else Icons.Rounded.Description,
                                        contentDescription = null,
                                        tint = if (selectedReportType == type) CelesteAcento else GrisTextoSecundario
                                    )
                                    Text(
                                        text = type,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = if (selectedReportType == type) FontWeight.Bold else FontWeight.Normal,
                                            color = AzulMarca
                                        )
                                    )
                                }

                                if (selectedReportType == type) {
                                    Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = CelesteAcento, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val pdfFile = PdfReportGenerator.generatePdfReport(
                                context,
                                PdfReportGenerator.ReportData(
                                    orderCode = "REP-$currentPeriodCode",
                                    serviceTitle = selectedReportType,
                                    clientName = "Consolidado Todoservicio",
                                    companyName = "Todoservicio",
                                    clientAddress = "Montevideo y Canelones, Uruguay",
                                    leadTechnician = "Coordinación Operativa",
                                    category = "Consolidado ($selectedPeriod)",
                                    executionDate = selectedPeriod,
                                    tasksExecuted = tasks.map { "${it.code}: ${it.title} (${it.clientName})" }
                                )
                            )
                            if (pdfFile != null) {
                                PdfReportGenerator.openPdf(context, pdfFile)
                            } else {
                                Toast.makeText(context, "Error al generar PDF.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzulMarca, contentColor = Blanco),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar PDF", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val csv = buildString {
                                    appendLine("Tipo,Codigo,Titulo,Cliente,Estado,Fecha")
                                    tasks.forEach { t ->
                                        appendLine("OT,${t.code},\"${t.title.replace("\"", "'")}\",\"${t.clientName.replace("\"", "'")}\",${t.status.label},${t.scheduledDate}")
                                    }
                                    quotes.forEach { q ->
                                        appendLine("COT,${q.quoteCode},\"${q.title.replace("\"", "'")}\",\"${q.clientCompanyName.replace("\"", "'")}\",${if (q.isApproved) "Aprobada" else "Pendiente"},${q.issueDate}")
                                    }
                                }
                                val csvFile = File(context.cacheDir, "Todoservicio_${currentPeriodCode}.csv").apply { writeText(csv) }
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", csvFile)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Compartir Planilla CSV"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Planilla exportada: Todoservicio_${currentPeriodCode}.csv", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, VerdeExito),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeExito),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar Excel", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5. EMISOR DE PRESUPUESTOS Y COTIZACIONES (2WORKERS)
// ============================================================================
@Composable
fun AdminQuotesEmitterSection(
    clientRecords: List<AdminClientRecord>,
    modifier: Modifier = Modifier
) {
    val quotes by OperationalRepository.quotes.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showCreateQuoteModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cotizaciones",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
                )
                Text(
                    text = "Presupuestos corporativos",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                )
            }

            Button(
                onClick = {
                    if (clientRecords.isEmpty()) {
                        Toast.makeText(context, "Primero crea un cliente en Clientes y Sedes.", Toast.LENGTH_SHORT).show()
                    } else {
                        showCreateQuoteModal = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento, contentColor = Blanco),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Emitir", style = MaterialTheme.typography.labelMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold))
            }
        }

        if (quotes.isEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Description, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(34.dp))
                    Text("Sin cotizaciones aún. Toca Emitir para comenzar.", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario, fontSize = 13.sp)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            quotes.forEach { quote ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(shape = RoundedCornerShape(6.dp), color = AzulCorporativo.copy(alpha = 0.1f)) {
                                    Text(
                                        text = quote.quoteCode,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AzulCorporativo),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = quote.clientCompanyName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AzulMarca)
                                )
                            }

                            val (statusBg, statusText, statusCol) = if (quote.isApproved) {
                                Triple(VerdeExito.copy(alpha = 0.15f), "Aprobado por Cliente", VerdeExito)
                            } else {
                                Triple(AmarilloAdvertencia.copy(alpha = 0.15f), "En Revisión", AmarilloAdvertencia)
                            }

                            Surface(shape = RoundedCornerShape(8.dp), color = statusBg) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = statusCol),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = quote.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
                        )

                        Text(
                            text = quote.scopeDescription,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Rounded.AttachFile, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "${quote.attachedFileName} (${quote.attachedFileSize})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CelesteAcento, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                )
                            }

                            Text(
                                text = "$${quote.amountUsd.toInt()} USD",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateQuoteModal) {
        CreateQuoteDialog(
            clientRecords = clientRecords,
            onDismiss = { showCreateQuoteModal = false },
            onSave = { newQuote ->
                OperationalRepository.sendQuote(newQuote)
                showCreateQuoteModal = false
            }
        )
    }
}

// ============================================================================
// MODALES DE REGISTRO
// ============================================================================

@Composable
fun CreateEquipmentDialog(
    clientRecords: List<AdminClientRecord>,
    onDismiss: () -> Unit,
    onSave: (ClientEquipmentAsset) -> Unit
) {
    val context = LocalContext.current
    var assetCode by remember { mutableStateOf("#EQ-" + (1000..9999).random()) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("HVAC / Climatización") }
    var selectedClient by remember { mutableStateOf(clientRecords.firstOrNull()?.companyName.orEmpty()) }
    var locationInBuilding by remember { mutableStateOf("") }
    var brandModel by remember { mutableStateOf("") }
    var isClientDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Registrar Nuevo Equipo en Cliente", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = assetCode,
                    onValueChange = { assetCode = it },
                    label = { Text("Código de Activo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Equipo (ej. Aire Acondicionado 3)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = brandModel,
                    onValueChange = { brandModel = it },
                    label = { Text("Marca y Modelo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown de Cliente
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedClient,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cliente / Empresa") },
                        trailingIcon = {
                            IconButton(onClick = { isClientDropdownExpanded = true }) {
                                Icon(Icons.Rounded.FilterList, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isClientDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = isClientDropdownExpanded,
                        onDismissRequest = { isClientDropdownExpanded = false }
                    ) {
                        clientRecords.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.companyName, fontFamily = PoppinsFontFamily) },
                                onClick = {
                                    selectedClient = c.companyName
                                    isClientDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = locationInBuilding,
                    onValueChange = { locationInBuilding = it },
                    label = { Text("Ubicación en el Edificio (ej. Azotea)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || selectedClient.isBlank()) {
                        Toast.makeText(context, "Ingresa el nombre del equipo y el cliente.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onSave(
                        ClientEquipmentAsset(
                            id = UUID.randomUUID().toString(),
                            assetCode = assetCode,
                            name = name.trim(),
                            category = category,
                            clientCompanyName = selectedClient,
                            locationInBuilding = if (locationInBuilding.isNotBlank()) locationInBuilding else "Sector Principal",
                            brandModel = if (brandModel.isNotBlank()) brandModel else "Standard",
                            status = "Operativo"
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento, contentColor = Blanco)
            ) {
                Text("Guardar Equipo", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = GrisTextoSecundario) }
        }
    )
}

@Composable
fun MasterServiceEditorDialog(
    initialItem: MasterServiceCatalogItem?,
    nextIndex: Int = 10,
    onDismiss: () -> Unit,
    onSave: (MasterServiceCatalogItem) -> Unit
) {
    val context = LocalContext.current
    val isEditing = initialItem != null
    val defaultCode = remember(initialItem, nextIndex) {
        initialItem?.code ?: "TAR-${nextIndex.toString().padStart(2, '0')}"
    }
    var code by remember(initialItem) { mutableStateOf(defaultCode) }
    var name by remember(initialItem) { mutableStateOf(initialItem?.name.orEmpty()) }
    var category by remember(initialItem) { mutableStateOf(initialItem?.category ?: "Mantenimiento Edilicio") }
    var description by remember(initialItem) { mutableStateOf(initialItem?.description.orEmpty()) }
    var hours by remember(initialItem) { mutableStateOf(initialItem?.estimatedDurationHours?.toString() ?: "2.0") }
    var dec406Compliant by remember(initialItem) { mutableStateOf(initialItem?.dec406Compliant ?: true) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val availableCategories = listOf(
        "Mantenimiento Edilicio",
        "Climatización (HVAC)",
        "Electricidad",
        "Sanitaria y Bombas",
        "Limpieza Especializada",
        "Seguridad en Altura (Dec. 406/88)",
        "Cubiertas y Pluviales",
        "Albañilería y Pintura"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Editar Tarea del Catálogo" else "Nueva Tarea en Catálogo",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                color = AzulMarca
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código (ej. TAR-10)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la Tarea / Servicio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Rubro / Categoría") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { categoryDropdownExpanded = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Rounded.FilterList, contentDescription = "Elegir rubro")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        availableCategories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontFamily = PoppinsFontFamily) },
                                onClick = {
                                    category = option
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Alcance técnico y detalles") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Duración estimada (hs)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cumple Decreto 406/88",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = AzulMarca
                        )
                        Text(
                            text = "Protocolo de seguridad edilicia",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = GrisTextoSecundario
                        )
                    }
                    Switch(
                        checked = dec406Compliant,
                        onCheckedChange = { dec406Compliant = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Blanco, checkedTrackColor = VerdeExito)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isBlank()) {
                        Toast.makeText(context, "Ingresa el nombre de la tarea.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onSave(
                        MasterServiceCatalogItem(
                            id = initialItem?.id ?: UUID.randomUUID().toString(),
                            code = code.trim().ifBlank { defaultCode },
                            name = cleanName,
                            category = category.trim().ifBlank { "Mantenimiento Edilicio" },
                            description = description.trim(),
                            estimatedDurationHours = hours.toDoubleOrNull() ?: 2.0,
                            dec406Compliant = dec406Compliant,
                            basePriceUsd = initialItem?.basePriceUsd ?: 0.0
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento, contentColor = Blanco)
            ) {
                Text(
                    text = if (isEditing) "Guardar Cambios" else "Agregar Tarea",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = GrisTextoSecundario)
            }
        }
    )
}

@Composable
fun CreateMasterServiceDialog(
    onDismiss: () -> Unit,
    onSave: (MasterServiceCatalogItem) -> Unit
) {
    MasterServiceEditorDialog(
        initialItem = null,
        onDismiss = onDismiss,
        onSave = onSave
    )
}

@Composable
fun CreateQuoteDialog(
    clientRecords: List<AdminClientRecord>,
    onDismiss: () -> Unit,
    onSave: (PendingQuote) -> Unit
) {
    val context = LocalContext.current
    val localeEs = remember { Locale("es", "UY") }
    val yearStr = remember { SimpleDateFormat("yyyy", Locale.US).format(Date()) }
    var quoteCode by remember { mutableStateOf("COT-$yearStr-" + (100..999).random()) }
    var selectedClient by remember { mutableStateOf(clientRecords.firstOrNull()?.companyName.orEmpty()) }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Climatización & Tableros") }
    var amountUsd by remember { mutableStateOf("450") }
    var scopeDescription by remember { mutableStateOf("") }
    var isClientDropdownOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Emitir Nueva Cotización Corporativa", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = quoteCode,
                    onValueChange = { quoteCode = it },
                    label = { Text("Código de Cotización") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de cliente
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedClient,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cliente Destinatario") },
                        trailingIcon = {
                            IconButton(onClick = { isClientDropdownOpen = true }) {
                                Icon(Icons.Rounded.FilterList, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isClientDropdownOpen = true }
                    )
                    DropdownMenu(
                        expanded = isClientDropdownOpen,
                        onDismissRequest = { isClientDropdownOpen = false }
                    ) {
                        clientRecords.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.companyName, fontFamily = PoppinsFontFamily) },
                                onClick = {
                                    selectedClient = c.companyName
                                    isClientDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del Trabajo / Presupuesto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountUsd,
                    onValueChange = { amountUsd = it },
                    label = { Text("Monto Total (USD)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = scopeDescription,
                    onValueChange = { scopeDescription = it },
                    label = { Text("Memoria Descriptiva y Alcance") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || selectedClient.isBlank()) {
                        Toast.makeText(context, "Selecciona cliente e ingresa un título.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val dateFmt = SimpleDateFormat("d 'de' MMMM, yyyy", localeEs)
                    val issue = dateFmt.format(Date())
                    val validCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 15) }
                    val valid = dateFmt.format(validCal.time)
                    onSave(
                        PendingQuote(
                            id = UUID.randomUUID().toString(),
                            quoteCode = quoteCode,
                            clientCompanyName = selectedClient,
                            title = title.trim(),
                            category = category,
                            amountUsd = amountUsd.toDoubleOrNull() ?: 350.0,
                            issueDate = issue,
                            validUntil = valid,
                            scopeDescription = if (scopeDescription.isNotBlank()) scopeDescription else "Mano de obra especializada, materiales e informe técnico.",
                            isApproved = false,
                            attachedFileName = "Presupuesto_${quoteCode}.pdf",
                            attachedFileSize = "1.2 MB",
                            attachedFileType = "Documento PDF"
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento, contentColor = Blanco)
            ) {
                Text("Emitir al Cliente", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
