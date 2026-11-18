package com.example.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.InstitutionalHeaderBrush
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RojoUrgencia

@Composable
fun rememberPersistedBitmap(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    return remember(uriString) {
        if (uriString.isNullOrBlank()) null
        else {
            try {
                context.contentResolver.openInputStream(Uri.parse(uriString))?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}

/**
 * Cápsula compacta y minimalista de usuario para cabeceras:
 * [ Foto de Perfil circular 32x32 ] + [ Nombre de pila 13sp SemiBold blanco ].
 */
@Composable
fun UserProfileHeaderCapsule(
    firstName: String,
    avatarUrl: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val avatarBitmap = rememberPersistedBitmap(avatarUrl)
    val displayName = firstName.trim().substringBefore(" ").ifBlank { "Usuario" }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Blanco.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, Blanco.copy(alpha = 0.22f)),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .let { mod -> if (onClick != null) mod.clickable { onClick() } else mod }
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AzulCorporativo),
                contentAlignment = Alignment.Center
            ) {
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap,
                        contentDescription = "Foto de perfil de $displayName",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Perfil de $displayName",
                        tint = Blanco,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = displayName,
                color = Blanco,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PoppinsFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 86.dp)
            )
        }
    }
}

/**
 * Barra superior institucional compacta protegida con SafeArea.
 */
@Composable
fun TopAppBarTodoservicio(
    title: String = "TODOSERVICIO",
    subtitle: String? = null,
    onMenuClick: (() -> Unit)? = null,
    hasUnreadNotifications: Boolean = false,
    unreadNotificationCount: Int = 0,
    onNotificationsClick: () -> Unit,
    onGuideClick: (() -> Unit)? = null,
    companyGreeting: String? = null,
    selectedBranch: String = "",
    availableBranches: List<String> = emptyList(),
    onBranchChange: (String) -> Unit = {},
    onCreateClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    userFirstName: String = "",
    userAvatarUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val safeTop = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val topInsetPadding = (safeTop + 8.dp).coerceAtLeast(32.dp)
    var isBranchMenuExpanded by remember { mutableStateOf(false) }
    var currentBranch by remember(selectedBranch, availableBranches) {
        mutableStateOf(selectedBranch.ifBlank { availableBranches.firstOrNull().orEmpty() })
    }

    Surface(
        color = AzulMarca,
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(InstitutionalHeaderBrush)
                .padding(top = topInsetPadding, bottom = 12.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.todoservicio_logo_hd),
                    contentDescription = "Todoservicio",
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.CenterStart,
                    modifier = Modifier
                        .height(32.dp)
                        .weight(1f, fill = false)
                        .testTag("official_logo_header")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onCreateClick != null) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AzulCorporativo,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onCreateClick() }
                                .testTag("btn_top_bar_crear")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Crear",
                                    tint = Blanco,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Crear",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco,
                                        fontSize = 11.5.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    if (onGuideClick != null) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Blanco.copy(alpha = 0.14f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onGuideClick() }
                                .testTag("btn_top_bar_guide")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = "Guía Rápida",
                                tint = Blanco,
                                modifier = Modifier
                                    .padding(7.dp)
                                    .size(17.dp)
                            )
                        }
                    }

                    val badgeCount = unreadNotificationCount.coerceAtLeast(if (hasUnreadNotifications) 1 else 0)
                    HeaderNotificationBell(
                        unreadCount = badgeCount,
                        onClick = onNotificationsClick,
                        modifier = Modifier.testTag("btn_app_bar_notifications")
                    )

                    if (userFirstName.isNotBlank()) {
                        UserProfileHeaderCapsule(
                            firstName = userFirstName,
                            avatarUrl = userAvatarUrl,
                            onClick = onProfileClick ?: onMenuClick
                        )
                    } else if (onProfileClick != null) {
                        UserProfileHeaderCapsule(
                            firstName = title,
                            avatarUrl = userAvatarUrl,
                            onClick = onProfileClick
                        )
                    }
                }
            }

            if (availableBranches.isNotEmpty() || !subtitle.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!companyGreeting.isNullOrBlank()) {
                        Text(
                            text = companyGreeting,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Blanco.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }

                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Blanco.copy(alpha = 0.14f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = availableBranches.isNotEmpty()) {
                                    isBranchMenuExpanded = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Business,
                                    contentDescription = null,
                                    tint = CelesteAcentoClaro,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = currentBranch.ifBlank { subtitle?.takeIf { it.isNotBlank() } ?: "Sede" },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = CelesteAcentoClaro,
                                        fontSize = 10.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 170.dp)
                                )
                                if (availableBranches.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDropDown,
                                        contentDescription = "Cambiar sede",
                                        tint = CelesteAcentoClaro,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        if (availableBranches.isNotEmpty()) {
                            DropdownMenu(
                                expanded = isBranchMenuExpanded,
                                onDismissRequest = { isBranchMenuExpanded = false }
                            ) {
                                availableBranches.forEach { branch ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = branch,
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = if (branch == currentBranch) FontWeight.Bold else FontWeight.Normal,
                                                color = if (branch == currentBranch) AzulMarca else AzulCorporativo,
                                                fontSize = 12.sp
                                            )
                                        },
                                        onClick = {
                                            currentBranch = branch
                                            onBranchChange(branch)
                                            isBranchMenuExpanded = false
                                        }
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
