package com.example.integraciones

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

/** Botón listo para agregar a la pantalla de DETALLE de una solicitud. */
@Composable
fun BotonCompartirSolicitudWhatsApp(
    solicitud: SolicitudCompartible,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var vistaPrevia by remember { mutableStateOf(false) }

    Button(onClick = { vistaPrevia = true }, modifier = modifier) {
        Text("Compartir por WhatsApp")
    }

    if (vistaPrevia) {
        AlertDialog(
            onDismissRequest = { vistaPrevia = false },
            title = { Text("Mensaje preparado") },
            text = { Text(CompartirSolicitudWhatsApp.generarMensaje(solicitud)) },
            dismissButton = {
                TextButton(onClick = { vistaPrevia = false }) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vistaPrevia = false
                    val resultado = CompartirSolicitudWhatsApp.abrirWhatsAppBusiness(
                        context, solicitud
                    )
                    val aviso = when (resultado) {
                        ResultadoCompartir.WHATSAPP_BUSINESS_ABIERTO ->
                            "WhatsApp Business abierto. Confirma el envío si corresponde."
                        ResultadoCompartir.SELECTOR_ANDROID_ABIERTO ->
                            "WhatsApp Business no disponible. Elige dónde compartir."
                        ResultadoCompartir.SIN_APLICACION_COMPATIBLE ->
                            "No se encontró una aplicación para compartir."
                    }
                    Toast.makeText(context, aviso, Toast.LENGTH_LONG).show()
                }) {
                    Text("Abrir WhatsApp Business")
                }
            }
        )
    }
}