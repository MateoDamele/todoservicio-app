package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.FormatPaint
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Hvac
import androidx.compose.material.icons.rounded.PriceChange
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.WaterDrop
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.OperationalRepository
import com.example.model.ClientProfile
import com.example.model.ServiceHistoryItem
import com.example.ui.components.ApplyWindowBackdropBlur
import com.example.ui.components.GlassDialogEntrance
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
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo

/**
 * Modelo de rubro de cobertura incluido en el plan.
 */
data class PlanCoverageCredit(
    val id: String,
    val categoryName: String,
    val icon: ImageVector,
    val usedVisits: Int,
    val totalVisits: Int,
    val lastExecutionDate: String?,
    val isCompleted: Boolean,
    val tag: String? = null
)

/**
 * Modelo de visita mensual realizada en el marco del plan.
 */
data class MonthlyPlanVisitRecord(
    val id: String,
    val dateText: String,
    val category: String,
    val technicianName: String,
    val summary: String,
    val hasDec406Compliance: Boolean,
    val relatedHistoryItem: ServiceHistoryItem
)

@Composable
fun MonthlyPlanScreen(
    clientProfile: ClientProfile,
    onNavigateToRequestWork: (() -> Unit)? = null,
    onViewReport: ((ServiceHistoryItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val repoReports by OperationalRepository.serviceReports.collectAsStateWithLifecycle()
    val tenantReports = remember(repoReports, clientProfile) {
        val companyLower = clientProfile.companyName.trim().lowercase()
        repoReports.filter { item ->
            (clientProfile.clientId.isNotBlank() && item.clientId == clientProfile.clientId) ||
                (companyLower.isNotBlank() && item.companyName.trim().lowercase() == companyLower)
        }
    }

    val totalVisits = clientProfile.totalPlanVisits.coerceAtLeast(0)
    var localRequestedVisits by remember { mutableStateOf(0) }
    val usedVisits = (clientProfile.visitsThisMonth + localRequestedVisits).coerceAtLeast(0)

    // Animación suave del arco circular
    var animationTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationTrigger = true
    }

    val targetProgress = if (totalVisits > 0) (usedVisits.toFloat() / totalVisits.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTrigger) targetProgress else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "planCircularProgress"
    )

    val monthlyVisits = remember(tenantReports) {
        tenantReports.map { report ->
            MonthlyPlanVisitRecord(
                id = report.id,
                dateText = if (report.executionTime.isNotBlank()) "${report.executionDate} • ${report.executionTime}" else report.executionDate,
                category = report.category,
                technicianName = report.leadTechnician,
                summary = report.summary,
                hasDec406Compliance = report.certifiedDec406,
                relatedHistoryItem = report
            )
        }
    }

    val coverageCredits = remember(monthlyVisits, totalVisits) {
        if (totalVisits <= 0) {
            emptyList()
        } else {
            val elec = monthlyVisits.firstOrNull { it.category.contains("Elec", ignoreCase = true) }
            val hvac = monthlyVisits.firstOrNull { it.category.contains("Clima", ignoreCase = true) || it.category.contains("HVAC", ignoreCase = true) }
            val sani = monthlyVisits.firstOrNull { it.category.contains("Sani", ignoreCase = true) }
            val gen = monthlyVisits.firstOrNull {
                !it.category.contains("Elec", ignoreCase = true) &&
                    !it.category.contains("Clima", ignoreCase = true) &&
                    !it.category.contains("HVAC", ignoreCase = true) &&
                    !it.category.contains("Sani", ignoreCase = true)
            }
            listOf(
                PlanCoverageCredit(
                    id = "cov-01",
                    categoryName = "Mantenimiento Eléctrico Preventivo",
                    icon = Icons.Rounded.ElectricBolt,
                    usedVisits = if (elec != null) 1 else 0,
                    totalVisits = 1,
                    lastExecutionDate = elec?.dateText,
                    isCompleted = elec != null
                ),
                PlanCoverageCredit(
                    id = "cov-02",
                    categoryName = "Climatización (HVAC) / Filtros",
                    icon = Icons.Rounded.Hvac,
                    usedVisits = if (hvac != null) 1 else 0,
                    totalVisits = 1,
                    lastExecutionDate = hvac?.dateText,
                    isCompleted = hvac != null
                ),
                PlanCoverageCredit(
                    id = "cov-03",
                    categoryName = "Sanitaria y Desagües",
                    icon = Icons.Rounded.WaterDrop,
                    usedVisits = if (sani != null) 1 else 0,
                    totalVisits = 1,
                    lastExecutionDate = sani?.dateText,
                    isCompleted = sani != null
                ),
                PlanCoverageCredit(
                    id = "cov-04",
                    categoryName = "Mantenimiento General",
                    icon = Icons.Rounded.FormatPaint,
                    usedVisits = if (gen != null) 1 else 0,
                    totalVisits = 1,
                    lastExecutionDate = gen?.dateText,
                    isCompleted = gen != null
                )
            )
        }
    }

    // Modales interactivos
    var showRequestIncludedVisitDialog by remember { mutableStateOf(false) }
    var showUpgradePlanDialog by remember { mutableStateOf(false) }
    var selectedVisitReport by remember { mutableStateOf<ServiceHistoryItem?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GrisNeutro)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // ====================================================================
        // 2. TARJETA PRINCIPAL: INDICADOR VISUAL DE CONSUMO (GRÁFICO CIRCULAR)
        // ====================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plan_consumption_card")
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Gráfico Circular de Progreso
                    Box(
                        modifier = Modifier
                            .size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            val strokeWidth = 14.dp.toPx()
                            val arcSize = size.width - strokeWidth
                            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                            // Círculo de fondo (Gris Neutro)
                            drawArc(
                                color = Color(0xFFE2E8F0),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth)
                            )

                            // Arco de progreso (Celeste de Acento #4A90D9 con puntas redondeadas)
                            drawArc(
                                color = Color(0xFF4A90D9),
                                startAngle = -90f,
                                sweepAngle = animatedProgress * 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        // Texto central: "3 / 4" en Poppins Bold (#102C54) y etiqueta
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$usedVisits / $totalVisits",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontSize = 32.sp
                                )
                            )
                            Text(
                                text = "Visitas Utilizadas",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = AzulCorporativo,
                                    fontSize = 11.sp
                                )
                            )
                            val remainingVisits = (totalVisits - usedVisits).coerceAtLeast(0)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (remainingVisits > 0) VerdeExitoFondo else GrisNeutro,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "$remainingVisits Disponible",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remainingVisits > 0) VerdeExito else GrisTextoSecundario,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Banner de Estado del Plan
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AzulCorporativo,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Plan: ${clientProfile.planType ?: "Sin Plan Asignado"}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco
                                    )
                                )
                            }
                        }

                        if (clientProfile.planType != null) {
                            val nextMonthName = remember {
                                val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MONTH, 1) }
                                java.text.SimpleDateFormat("MMM", java.util.Locale("es", "UY"))
                                    .format(cal.time)
                                    .replaceFirstChar { it.uppercase() }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Autorenew,
                                    contentDescription = null,
                                    tint = CelesteAcento,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Renueva el 1° de $nextMonthName",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // ====================================================================
        // 4. ACCIONES RÁPIDAS DEL PLAN (BOTONES PRINCIPALES)
        // ====================================================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Botón 1: "Solicitar Visita Incluida en mi Plan" (Ancho completo, Celeste de Acento #4A90D9)
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showRequestIncludedVisitDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CelesteAcento,
                        contentColor = Blanco
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_request_included_visit")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Handyman,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Solicitar Visita Incluida en mi Plan",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco,
                            fontSize = 15.sp
                        )
                    )
                }

                // Botón 2: "Solicitar Ajuste o Ampliación de Plan" (Borde Azul Corporativo #1D4E89)
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showUpgradePlanDialog = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AzulCorporativo
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AzulCorporativo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_upgrade_plan")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PriceChange,
                        contentDescription = null,
                        tint = AzulCorporativo,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Solicitar Ajuste o Ampliación de Plan",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulCorporativo,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }

        // ====================================================================
        // 3. SECCIÓN DE DESGLOSE DE COBERTURA Y CRÉDITOS
        // ====================================================================
        item {
            Text(
                text = "Registro de Mantenimientos Realizados este Mes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            )
        }

        if (coverageCredits.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sin rubros de abono activos aún.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(coverageCredits, key = { it.id }) { credit ->
                CoverageCreditItemRow(credit = credit)
            }
        }

        // ====================================================================
        // 5. HISTORIAL MENSUAL DE VISITAS (TABLA / LISTA DE ACTIVIDAD)
        // ====================================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Visitas Realizadas este Mes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CelesteAcentoClaro
                ) {
                    Text(
                        text = "${monthlyVisits.size} Registradas",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulCorporativo
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (monthlyVisits.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = GrisTextoSecundario.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Sin visitas registradas este mes",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                        Text(
                            text = "Las visitas realizadas aparecerán aquí con su informe.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(monthlyVisits, key = { it.id }) { visit ->
                MonthlyVisitRecordCard(
                    visit = visit,
                    onViewReport = {
                        if (onViewReport != null) {
                            onViewReport(visit.relatedHistoryItem)
                        } else {
                            selectedVisitReport = visit.relatedHistoryItem
                        }
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(108.dp)) }
    }

    // Modal: Solicitar Visita Incluida en mi Plan
    if (showRequestIncludedVisitDialog) {
        RequestIncludedVisitModal(
            availableCategory = "Mantenimiento General",
            onDismiss = { showRequestIncludedVisitDialog = false },
            onConfirm = { requestedCategory, notes ->
                val createdTask = OperationalRepository.submitClientWorkRequest(
                    clientProfile = clientProfile,
                    title = "Visita de Plan: $requestedCategory",
                    category = requestedCategory,
                    location = clientProfile.address.ifBlank { clientProfile.companyName },
                    description = notes.ifBlank { "Visita mensual solicitada desde el portal" },
                    isPlanVisit = true
                )
                localRequestedVisits++
                showRequestIncludedVisitDialog = false
                Toast.makeText(context, "Visita ${createdTask.code} solicitada con éxito.", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Modal: Solicitar Ajuste o Ampliación de Plan
    if (showUpgradePlanDialog) {
        UpgradePlanModal(
            currentPlanName = clientProfile.planType ?: "Sin Plan Asignado",
            onDismiss = { showUpgradePlanDialog = false },
            onConfirmUpgrade = { planName ->
                OperationalRepository.addNotification("Solicitud de plan (${clientProfile.companyName}): $planName")
                showUpgradePlanDialog = false
                Toast.makeText(context, "Solicitud enviada para $planName.", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Modal de Reporte Técnico
    selectedVisitReport?.let { report ->
        TechnicalReportDialog(
            item = report,
            onDismiss = { selectedVisitReport = null }
        )
    }
}

/**
 * Fila del desglose de cobertura con contadores e iconos de estado.
 */
@Composable
private fun CoverageCreditItemRow(credit: PlanCoverageCredit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = if (credit.isCompleted) VerdeExitoFondo else CelesteAcentoClaro,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = credit.icon,
                    contentDescription = null,
                    tint = if (credit.isCompleted) VerdeExito else CelesteAcento,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = credit.categoryName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = AzulMarca,
                        fontSize = 13.5.sp
                    )
                )

                Text(
                    text = if (credit.isCompleted) "Última visita: ${credit.lastExecutionDate}" else "Disponible sin costo adicional",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = if (credit.isCompleted) GrisTextoSecundario else VerdeExito,
                        fontSize = 11.sp
                    )
                )
            }

            // Contador de cuota: 1 / 1 o 0 / 1 con icono
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (credit.isCompleted) VerdeExitoFondo else CelesteAcentoClaro
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (credit.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = if (credit.isCompleted) VerdeExito else CelesteAcento,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${credit.usedVisits} / ${credit.totalVisits} ${if (credit.isCompleted) "Realizado" else "Disponible"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (credit.isCompleted) VerdeExito else AzulCorporativo,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                if (credit.tag != null) {
                    Text(
                        text = credit.tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (credit.isCompleted) AzulCorporativo else CelesteAcento,
                            fontSize = 9.5.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta del historial mensual de visitas realizadas con botón "Ver Reporte de la Visita".
 */
@Composable
private fun MonthlyVisitRecordCard(
    visit: MonthlyPlanVisitRecord,
    onViewReport: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = visit.dateText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulCorporativo
                    )
                )

                if (visit.hasDec406Compliance) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AzulMarca.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = CelesteAcento,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Certificado",
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

            Text(
                text = visit.category,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            )

            Text(
                text = "${visit.technicianName} • ${visit.summary}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = PoppinsFontFamily,
                    color = GrisTextoSecundario
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onViewReport,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulCorporativo),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CelesteAcento),
                    modifier = Modifier.testTag("btn_view_visit_report_${visit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CelesteAcento
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ver Reporte de la Visita",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulCorporativo
                        )
                    )
                }
            }
        }
    }
}

/**
 * Modal interactivo: Solicitar Visita Incluida en mi Plan
 */
@Composable
private fun RequestIncludedVisitModal(
    availableCategory: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var locationSector by remember { mutableStateOf("Hall de Acceso / Áreas Comunes") }

    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(18.dp),
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
                        color = CelesteAcentoClaro
                    ) {
                        Text(
                            text = "CUPO INCLUIDO EN PLAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulCorporativo
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario)
                    }
                }

                Text(
                    text = "Coordinar Visita Técnica",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = VerdeExitoFondo,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Rubro disponible:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario
                            )
                        )
                        Text(
                            text = availableCategory,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = VerdeExito
                            )
                        )
                        Text(
                            text = "Esta visita se deduce directamente de su cuota mensual sin cobro extra.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = AzulMarca,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = locationSector,
                    onValueChange = { locationSector = it },
                    label = { Text("Sector o Ubicación", fontFamily = PoppinsFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Detalle de las reparaciones requeridas", fontFamily = PoppinsFontFamily) },
                    placeholder = { Text("Ej: Retoque de pintura en acceso y fijación de zócalos", fontFamily = PoppinsFontFamily) },
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { onConfirm(availableCategory, notes) },
                    colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_confirm_request_included_visit")
                ) {
                    Icon(imageVector = Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar y Despachar Visita", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                }
            }
        }
        }
    }
}

/**
 * Modal interactivo: Solicitar Ajuste o Ampliación de Plan
 */
@Composable
private fun UpgradePlanModal(
    currentPlanName: String,
    onDismiss: () -> Unit,
    onConfirmUpgrade: (String) -> Unit
) {
    var selectedPlanOption by remember { mutableStateOf("Plan Edificio Integral (12 Visitas / mes)") }

    val options = listOf(
        Pair("Plan Corporativo Plus (8 Visitas / mes)", "Ideal para edificios de oficinas con grupo electrógeno."),
        Pair("Plan Edificio Integral (12 Visitas / mes)", "Mayor cobertura para comunidades residenciales y copropiedades."),
        Pair("Plan Industrial (16 Visitas / mes)", "Auditorías permanentes y mantenimiento intensivo.")
    )

    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(18.dp),
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
                    Text(
                        text = "Ampliación de Cobertura",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario)
                    }
                }

                Text(
                    text = "Plan actual: $currentPlanName. Seleccione el nivel de ampliación o visitas adicionales que desea incorporar a su contrato:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { (planName, desc) ->
                        val isSelected = selectedPlanOption == planName
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CelesteAcentoClaro else GrisNeutro,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CelesteAcento else GrisBorde
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedPlanOption = planName }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = planName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) AzulMarca else AzulCorporativo
                                    )
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { onConfirmUpgrade(selectedPlanOption) },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Solicitar Propuesta de Ampliación", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                }
            }
        }
        }
    }
}
