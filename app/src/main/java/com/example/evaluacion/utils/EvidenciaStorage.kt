package com.example.evaluacion.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID

// Gestiona el almacenamiento privado y la conversión a Base64 de evidencias
object EvidenciaStorage {

    // Tamaño máximo permitido: 5 MB
    private const val MAX_BYTES = 5L * 1024 * 1024

    // Copia la imagen seleccionada al almacenamiento interno (Room)
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

    // Convierte un Bitmap (por ejemplo desde la cámara/webcam) a Base64 comprimido
    suspend fun bitmapABase64(
        bitmap: Bitmap,
        calidad: Int = 70
    ): String = withContext(Dispatchers.IO) {
        val bitmapFinal = redimensionarSiEsNecesario(bitmap)
        val salida = ByteArrayOutputStream()
        bitmapFinal.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
        Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
    }

    // Convierte una imagen desde galería (Uri) a Base64 comprimido
    suspend fun imagenABase64(
        context: Context,
        uri: Uri,
        calidad: Int = 70
    ): String = withContext(Dispatchers.IO) {
        val entrada = context.contentResolver.openInputStream(uri)
            ?: throw IOException("No se pudo abrir la imagen")

        val bitmapOriginal = entrada.use { BitmapFactory.decodeStream(it) }
            ?: throw IOException("No se pudo decodificar la imagen")

        bitmapABase64(bitmapOriginal, calidad)
    }

    // Decodifica un Base64 recibido desde MongoDB para mostrarlo en Compose
    fun base64ABitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    // Reduce el tamaño si supera 1024px para no sobrecargar MongoDB
    private fun redimensionarSiEsNecesario(bitmap: Bitmap): Bitmap {
        val maxLado = 1024
        return if (bitmap.width > maxLado || bitmap.height > maxLado) {
            val ratio = minOf(
                maxLado.toFloat() / bitmap.width,
                maxLado.toFloat() / bitmap.height
            )
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else {
            bitmap
        }
    }
}