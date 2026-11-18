package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Destinos de navegación principal de la aplicación Todoservicio.
 * Módulos desglosados para el Portal Cliente y Operativa.
 */
enum class AppDestination(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val testTag: String
) {
    INICIO(
        title = "INICIO",
        subtitle = "Resumen del plan y accesos rápidos",
        icon = Icons.Rounded.Home,
        testTag = "nav_item_inicio"
    ),
    PLAN_MENSUAL(
        title = "OPERACIONES Y PLAN",
        subtitle = "Estado de visitas contratadas vs. visitas realizadas",
        icon = Icons.Rounded.Assessment,
        testTag = "nav_item_plan_mensual"
    ),
    SOLICITAR_TRABAJO(
        title = "SOLICITAR TRABAJO",
        subtitle = "Flujo simple por pasos para pedir servicio técnico",
        icon = Icons.Rounded.Build,
        testTag = "nav_item_solicitar_trabajo"
    ),
    CALENDARIO(
        title = "CALENDARIO OPERATIVO",
        subtitle = "Planificación mensual interactiva y órdenes de trabajo",
        icon = Icons.Rounded.CalendarMonth,
        testTag = "nav_item_calendario"
    ),
    MAPA_GPS(
        title = "SEGUIMIENTO GPS EN VIVO",
        subtitle = "Desplazamiento de cuadrilla en tiempo real hacia su sede",
        icon = Icons.Rounded.LocationOn,
        testTag = "nav_item_mapa_gps"
    ),
    DOBLE_FIRMA(
        title = "DOBLE FIRMA Y CONFORMIDAD",
        subtitle = "Visor dedicado para revisar intervención en curso y firmar",
        icon = Icons.Rounded.Draw,
        testTag = "nav_item_doble_firma"
    ),
    HISTORIAL_SERVICIOS(
        title = "REPORTES TÉCNICOS",
        subtitle = "Historial cronológico de hojas de servicio (PDFs) con buscador",
        icon = Icons.Rounded.Description,
        testTag = "nav_item_reportes_tecnicos"
    ),
    SOPORTE_CONSULTAS(
        title = "HISTORIAL Y CALIDAD (NPS)",
        subtitle = "Registro de trabajos completados con encuestas de satisfacción",
        icon = Icons.Rounded.Star,
        testTag = "nav_item_nps_calidad"
    ),
    ACCESOS(
        title = "GESTIÓN DE ACCESOS",
        subtitle = "Alta de cuentas y perfiles corporativos",
        icon = Icons.Rounded.Group,
        testTag = "nav_item_accesos"
    )
}

/**
 * Pestañas de la barra de navegación ergonómica inferior universal para smartphone:
 * [ 🏠 INICIO ] • [ 📅 CALENDARIO ] • [ 🗺️ MAPA GPS ] • [ 👥 ACCESOS ] • [ ☰ MENÚ ]
 */
enum class MobileBottomTab(
    val label: String,
    val icon: ImageVector,
    val destination: AppDestination,
    val testTag: String
) {
    INICIO("INICIO", Icons.Rounded.Home, AppDestination.INICIO, "tab_inicio"),
    CALENDARIO("CALENDARIO", Icons.Rounded.CalendarMonth, AppDestination.CALENDARIO, "tab_calendario"),
    MAPA_GPS("MAPA GPS", Icons.Rounded.LocationOn, AppDestination.MAPA_GPS, "tab_mapa_gps"),
    ACCESOS("ACCESOS", Icons.Rounded.Group, AppDestination.ACCESOS, "tab_accesos"),
    MENU("MENÚ", Icons.Rounded.Menu, AppDestination.INICIO, "tab_menu")
}

/**
 * Pestañas exclusivas para el Portal de Clientes Corporativos (Cero Administración):
 * [ Inicio ] • [ Mis Reportes ] • [ Técnico en Camino ] • [ Solicitar Servicio ] • [ Firma de Conformidad ]
 */
enum class ClientBottomTab(
    val label: String,
    val icon: ImageVector,
    val destination: AppDestination,
    val testTag: String
) {
    INICIO("Inicio", Icons.Rounded.Home, AppDestination.INICIO, "tab_client_inicio"),
    REPORTES("Reportes", Icons.Rounded.Description, AppDestination.HISTORIAL_SERVICIOS, "tab_client_reportes"),
    EN_CAMINO("En Camino", Icons.Rounded.LocationOn, AppDestination.MAPA_GPS, "tab_client_en_camino"),
    SOLICITAR("Solicitar", Icons.Rounded.Build, AppDestination.SOLICITAR_TRABAJO, "tab_client_solicitar"),
    CONFORMIDAD("Firma", Icons.Rounded.Draw, AppDestination.DOBLE_FIRMA, "tab_client_firma")
}

