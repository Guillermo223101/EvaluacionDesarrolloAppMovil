package com.example.integraciones

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

/** Datos ficticios o autorizados de la solicitud; nunca incluir credenciales. */
data class SolicitudCompartible(
    val codigo: String,
    val titulo: String,
    val categoria: String,
    val estado: String
)

enum class ResultadoCompartir {
    WHATSAPP_BUSINESS_ABIERTO,
    SELECTOR_ANDROID_ABIERTO,
    SIN_APLICACION_COMPATIBLE
}

object CompartirSolicitudWhatsApp {

    fun generarMensaje(solicitud: SolicitudCompartible): String = buildString {
        appendLine("*Nueva solicitud de soporte*")
        appendLine("Código: ${solicitud.codigo}")
        appendLine("Título: ${solicitud.titulo}")
        appendLine("Categoría: ${solicitud.categoria}")
        appendLine("Estado: ${solicitud.estado}")
        append("Seguimiento: ${solicitud.codigo}")
    }

    /** Abre la pantalla de compartir; NO envía ni confirma la entrega del mensaje. */
    fun abrirWhatsAppBusiness(
        context: Context,
        solicitud: SolicitudCompartible
    ): ResultadoCompartir {
        val mensaje = generarMensaje(solicitud)
        val intentBusiness = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
            setPackage("com.whatsapp.w4b")
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intentBusiness)
            return ResultadoCompartir.WHATSAPP_BUSINESS_ABIERTO
        } catch (_: ActivityNotFoundException) {
            // El emulador puede no tener WhatsApp Business instalado.
        } catch (_: SecurityException) {
            // Una política del dispositivo puede impedir abrirlo directamente.
        }

        val enviarTexto = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        val selector = Intent.createChooser(enviarTexto, "Compartir solicitud").apply {
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(selector)
            ResultadoCompartir.SELECTOR_ANDROID_ABIERTO
        } catch (_: ActivityNotFoundException) {
            ResultadoCompartir.SIN_APLICACION_COMPATIBLE
        } catch (_: SecurityException) {
            ResultadoCompartir.SIN_APLICACION_COMPATIBLE
        }
    }
}