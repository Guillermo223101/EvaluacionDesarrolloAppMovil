package com.example.integraciones

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Úsalo dentro de un @Composable y llama al callback SOLO después del guardado
 * confirmado en la base de datos. No crea solicitudes ni altera el registro.
 */
@Composable
fun recordarNotificadorNuevaSolicitud(): (codigo: String, titulo: String) -> Unit {
    val context = LocalContext.current
    var pendiente by remember { mutableStateOf<Pair<String, String>?>(null) }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { autorizado ->
        if (autorizado) {
            pendiente?.let { (codigo, titulo) ->
                NotificacionesSolicitud.mostrarNuevaSolicitud(context, codigo, titulo)
            }
        }
        pendiente = null
    }

    return { codigo: String, titulo: String ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificacionesSolicitud.tienePermiso(context)
        ) {
            pendiente = codigo to titulo
            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NotificacionesSolicitud.mostrarNuevaSolicitud(context, codigo, titulo)
        }
    }
}