package com.example.integraciones

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/** Notificaciones LOCALES: aparecen solo en el dispositivo que ejecuta esta app. */
object NotificacionesSolicitud {
    private const val ID_CANAL = "solicitudes_nuevas"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                ID_CANAL,
                "Solicitudes nuevas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos al registrar una solicitud nueva"
            }
            val gestor = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            gestor.createNotificationChannel(canal)
        }
    }

    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun mostrarNuevaSolicitud(
        context: Context,
        codigo: String,
        titulo: String
    ): Boolean {
        if (!tienePermiso(context)) return false

        val gestor = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !gestor.areNotificationsEnabled()) {
            return false
        }
        crearCanal(context)

        val notificacion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, ID_CANAL)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }

        notificacion
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nueva solicitud registrada")
            .setContentText("$codigo: $titulo")
            .setStyle(Notification.BigTextStyle().bigText("Solicitud $codigo creada: $titulo"))
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            notificacion.setPriority(Notification.PRIORITY_HIGH)
        }

        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { abrirApp ->
            val pendiente = PendingIntent.getActivity(
                context,
                codigo.hashCode(),
                abrirApp,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificacion.setContentIntent(pendiente)
        }

        return try {
            gestor.notify(codigo.hashCode() and Int.MAX_VALUE, notificacion.build())
            true
        } catch (_: SecurityException) {
            false
        }
    }
}