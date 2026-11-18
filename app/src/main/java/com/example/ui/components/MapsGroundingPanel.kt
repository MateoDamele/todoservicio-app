package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MapsGroundingResult
import com.example.data.MapsGroundingServiceLocator
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
import kotlinx.coroutines.launch

/**
 * Panel compacto de Maps Grounding para verificar accesos, referencias y direcciones
 * en Montevideo y Canelones, cumpliendo con los requisitos de atribución a Google Maps.
 */
@Composable
fun MapsGroundingAssistantCard(
    defaultQuery: String,
    lat: Double,
    lng: Double,
    title: String = "Referencias de Ubicación",
    initiallyExpanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var expanded by remember { mutableStateOf(initiallyExpanded) }
    var queryText by remember(defaultQuery) { mutableStateOf(defaultQuery) }
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<MapsGroundingResult?>(null) }

    fun executeQuery(q: String) {
        if (q.isBlank() || isLoading) return
        scope.launch {
            isLoading = true
            try {
                result = MapsGroundingServiceLocator.repository.queryLocationContext(
                    query = q,
                    lat = lat,
                    lng = lng
                )
            } finally {
                isLoading = false
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Blanco,
        border = BorderStroke(1.dp, GrisBorde),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("maps_grounding_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                        if (expanded && result == null && queryText.isNotBlank()) {
                            executeQuery(queryText)
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
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
                            .size(30.dp)
                            .background(CelesteAcentoClaro, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Place,
                            contentDescription = null,
                            tint = AzulCorporativo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontSize = 12.5.sp
                            )
                        )
                        Text(
                            text = "Google Maps",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color(0xFF5E5E5E),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Contraer referencias" else "Expandir referencias",
                    tint = GrisTextoSecundario
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = queryText,
                            onValueChange = { queryText = it },
                            placeholder = {
                                Text(
                                    text = "Consultar dirección o zona...",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { executeQuery(queryText) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AzulCorporativo,
                                unfocusedBorderColor = GrisBorde,
                                focusedTextColor = AzulMarca,
                                unfocusedTextColor = AzulMarca
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("maps_grounding_input")
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AzulCorporativo,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { executeQuery(queryText) }
                                .testTag("maps_grounding_search_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Blanco,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = "Consultar ubicación",
                                        tint = Blanco,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    result?.let { res ->
                        if (res.isDemoPlaceholder && !res.notice.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = NaranjaAlertaFondo,
                                border = BorderStroke(1.dp, NaranjaAlerta.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Info,
                                        contentDescription = null,
                                        tint = NaranjaAlerta,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = res.notice,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            color = Color(0xFF9A3412),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GrisNeutro,
                            border = BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = res.answer,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = AzulMarca,
                                        fontSize = 12.sp
                                    )
                                )

                                if (res.sources.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Google Maps",
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = Color(0xFF5E5E5E),
                                            maxLines = 1,
                                            softWrap = false
                                        )

                                        res.sources.forEach { src ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Blanco,
                                                border = BorderStroke(1.dp, GrisBorde),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        try {
                                                            context.startActivity(
                                                                Intent(Intent.ACTION_VIEW, Uri.parse(src.uri))
                                                            )
                                                        } catch (_: Exception) {
                                                            Toast.makeText(
                                                                context,
                                                                "No se pudo abrir el enlace de Google Maps",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Map,
                                                            contentDescription = null,
                                                            tint = CelesteAcento,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Text(
                                                            text = src.title,
                                                            style = MaterialTheme.typography.labelMedium.copy(
                                                                fontFamily = PoppinsFontFamily,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = AzulCorporativo,
                                                                fontSize = 11.5.sp
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                                        contentDescription = "Abrir fuente en Google Maps",
                                                        tint = GrisTextoSecundario,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
