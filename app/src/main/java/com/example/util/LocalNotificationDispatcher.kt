package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.AnnouncementItem
import com.example.model.AnnouncementPriority

/**
 * Despachador nativo de notificaciones push locales en Android para avisos y comunicados.
 */
object LocalNotificationDispatcher {

    private const val CHANNEL_ID = "todoservicio_announcements"
    private const val CHANNEL_NAME = "Avisos y Comunicados"
    private const val CHANNEL_DESC = "Comunicados oficiales de Todoservicio para Limpieza, Técnicos y Clientes"

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESC
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun dispatchAnnouncementPush(context: Context, item: AnnouncementItem): Boolean {
        return try {
            ensureChannel(context)
            if (!hasNotificationPermission(context)) return false

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                item.id.hashCode(),
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val androidPriority = when (item.priority) {
                AnnouncementPriority.IMPORTANTE -> NotificationCompat.PRIORITY_HIGH
                AnnouncementPriority.MEDIO -> NotificationCompat.PRIORITY_DEFAULT
                AnnouncementPriority.BAJO -> NotificationCompat.PRIORITY_LOW
            }

            val prefix = when (item.priority) {
                AnnouncementPriority.IMPORTANTE -> "Importante • "
                AnnouncementPriority.MEDIO -> ""
                AnnouncementPriority.BAJO -> ""
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.todoservicio_logo_hd)
                .setContentTitle("$prefix${item.title}")
                .setContentText(item.body)
                .setPriority(androidPriority)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val attachedBitmap = item.imageUri?.let { uriStr ->
                try {
                    context.contentResolver.openInputStream(Uri.parse(uriStr))?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } catch (_: Exception) {
                    null
                }
            }

            if (attachedBitmap != null) {
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(attachedBitmap)
                        .setSummaryText(item.body)
                )
            } else {
                builder.setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(item.body)
                )
            }

            NotificationManagerCompat.from(context).notify(item.id.hashCode(), builder.build())
            true
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Envía una notificación push local cuando un técnico inicia traslado hacia un cliente.
     */
    fun dispatchServiceTransitPush(
        context: Context,
        orderCode: String,
        title: String,
        message: String
    ): Boolean {
        return try {
            ensureChannel(context)
            if (!hasNotificationPermission(context)) return false

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                orderCode.hashCode(),
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.todoservicio_logo_hd)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            NotificationManagerCompat.from(context).notify("transit_$orderCode".hashCode(), builder.build())
            true
        } catch (_: Throwable) {
            false
        }
    }
}
