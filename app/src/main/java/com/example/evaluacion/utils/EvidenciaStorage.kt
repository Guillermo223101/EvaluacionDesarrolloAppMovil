package com.example.evaluacion.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

// Gestiona el almacenamiento privado de evidencias
object EvidenciaStorage {

    // Tamaño máximo permitido: 5 MB
    private const val MAX_BYTES = 5L * 1024 * 1024

    // Copia la imagen seleccionada al almacenamiento interno
    suspend fun copiarImagen(
        context: Context,
        uri: Uri
    ): String = withContext(Dispatchers.IO) {

        // Comprobar el tipo de imagen
        val extension = when (
            context.contentResolver.getType(uri)
        ) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> throw IOException(
                "Solo se permiten imágenes JPG o PNG"
            )
        }

        // Crear carpeta privada para las evidencias
        val carpeta = File(context.filesDir, "evidencias")

        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw IOException("No se pudo crear la carpeta")
        }

        // Generar un nombre único para cada imagen
        val archivo = File(
            carpeta,
            "evidencia_${UUID.randomUUID()}.$extension"
        )

        try {

            // Abrir la imagen seleccionada
            val entrada = context.contentResolver
                .openInputStream(uri)
                ?: throw IOException("No se pudo abrir la imagen")

            // Copiar la imagen al archivo privado
            entrada.use { origen ->
                archivo.outputStream().use { destino ->

                    val buffer = ByteArray(8192)
                    var total = 0L

                    while (true) {

                        val bytes = origen.read(buffer)

                        if (bytes == -1) break

                        total += bytes

                        if (total > MAX_BYTES) {
                            throw IOException(
                                "La imagen supera los 5 MB"
                            )
                        }

                        destino.write(buffer, 0, bytes)
                    }
                }
            }

            // Devolver la ruta del archivo guardado
            archivo.absolutePath

        } catch (e: Exception) {

            // Eliminar archivos incompletos si ocurre un error
            archivo.delete()
            throw e
        }
    }
}
