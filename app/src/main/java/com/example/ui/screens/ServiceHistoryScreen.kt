package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.OperationalRepository
import com.example.model.ClientProfile
import com.example.model.ServiceHistoryItem
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo

@Composable
fun ServiceHistoryScreen(
    clientProfile: ClientProfile,
    onViewReport: (ServiceHistoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val allReports by OperationalRepository.serviceReports.collectAsStateWithLifecycle()
    val historyItems = remember(allReports, clientProfile) {
        val companyLower = clientProfile.companyName.trim().lowercase()
        allReports.filter { item ->
            (clientProfile.clientId.isNotBlank() && item.clientId == clientProfile.clientId) ||
                (companyLower.isNotBlank() && item.companyName.trim().lowercase() == companyLower)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GrisNeutro)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "HISTORIAL Y CERTIFICACIONES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = CelesteAcento,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Reportes Técnicos",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )
                }
            }
        }

        if (historyItems.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.HistoryEdu,
                            contentDescription = null,
                            tint = GrisTextoSecundario.copy(alpha = 0.6f),
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            text = "Sin registros aún",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                        Text(
                            text = "Los informes firmados por el técnico aparecerán aquí.",
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
            items(historyItems, key = { it.id }) { item ->
                HistoryCard(item = item, onDownload = { onViewReport(item) })
            }
        }

        item { Spacer(modifier = Modifier.height(108.dp)) }
    }
}

@Composable
private fun HistoryCard(
    item: ServiceHistoryItem,
    onDownload: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CelesteAcentoClaro
                ) {
                    Text(
                        text = item.serviceCode,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulCorporativo
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = item.executionDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = GrisTextoSecundario
                    )
                )
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            )

            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    color = GrisTextoSecundario
                )
            )

            // Badge de certificación Dec. 406/88
            if (item.certifiedDec406) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VerdeExitoFondo,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VerdeExito.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = VerdeExito,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Certificado Técnico Aprobado",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = VerdeExito
                            )
                        )
                    }
                }
            }

            androidx.compose.material3.HorizontalDivider(color = GrisBorde.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Firma Responsable:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario,
                            fontSize = 10.5.sp
                        )
                    )
                    Text(
                        text = item.signedBy,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulCorporativo,
                            fontSize = 11.5.sp
                        )
                    )
                }

                OutlinedButton(
                    onClick = onDownload,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AzulCorporativo
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CelesteAcento),
                    modifier = Modifier.testTag("btn_report_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CelesteAcento
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ver Informe",
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
