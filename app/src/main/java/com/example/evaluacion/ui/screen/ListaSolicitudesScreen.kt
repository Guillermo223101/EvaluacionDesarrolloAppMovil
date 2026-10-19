package com.example.evaluacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.evaluacion.viewmodel.SolicitudViewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import androidx.compose.animation.AnimatedContent


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun ListaSolicitudesScreen(
    viewModel: SolicitudViewModel,
    onNuevaSolicitud: () -> Unit
) {

    // Observamos las solicitudes almacenadas en Room
    val solicitudes by viewModel.solicitudes.collectAsState(
        initial = emptyList()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Título de la pantalla
        Text(
            text = "Mis Solicitudes",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Contador animado de solicitudes registradas
        AnimatedContent(
            targetState = solicitudes.size,
            label = "contador_solicitudes"
        ) { cantidad ->

            Text(
                text = "Solicitudes registradas: $cantidad",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para abrir el formulario
        Button(
            onClick = onNuevaSolicitud,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nueva Solicitud")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Comprobar si existen solicitudes
        if (solicitudes.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay solicitudes registradas",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        } else {

            // Lista de solicitudes
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = solicitudes,
                    key = { it.id }
                ) { solicitud ->

                    // Tarjeta de cada solicitud
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            // Título
                            Text(
                                text = solicitud.titulo,
                                style = MaterialTheme.typography.titleMedium
                            )

                            // Identificador
                            // Generar código de seguimiento a partir del ID
                            val codigo = "SOL-${solicitud.id.toString().padStart(4, '0')}"
                            // Mostrar código de seguimiento
                            Text(
                                text = "Código: $codigo",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )

                            // Categoría
                            Text(
                                text = "Categoría: ${solicitud.categoria}"
                            )

                            // Estado de la solicitud
                            Text(
                                text = "Estado: ${solicitud.estado}",
                                color = MaterialTheme.colorScheme.primary
                            )

                            HorizontalDivider()

                            // Descripción del problema
                            // Estado para mostrar u ocultar la descripción
                            var mostrarDescripcion by rememberSaveable(
                                solicitud.id
                            ) {
                                mutableStateOf(false)
                            }

                            // Botón que controla la animación
                            TextButton(
                                onClick = {
                                    mostrarDescripcion = !mostrarDescripcion
                                }
                            ) {
                                Text(
                                    text = if (mostrarDescripcion)
                                        "Ocultar descripción"
                                    else
                                        "Ver descripción"
                                )
                            }

                            // Animación de entrada y salida
                            AnimatedVisibility(
                                visible = mostrarDescripcion,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {

                                Text(
                                    text = solicitud.descripcion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }



                            // Mostrar fotografía si la solicitud tiene evidencia
                            solicitud.evidenciaPath?.let { ruta ->

                                HorizontalDivider()

                                Text(
                                    text = "Fotografía de evidencia",
                                    style = MaterialTheme.typography.labelLarge
                                )

                                FotoEvidencia(ruta = ruta)
                            }

                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun FotoEvidencia(ruta: String) {

    // Cargar la fotografía desde el almacenamiento privado
    val resultado by produceState<Pair<Boolean, Bitmap?>>(
        initialValue = false to null,
        key1 = ruta
    ) {

        val imagen = withContext(Dispatchers.IO) {

            try {

                val archivo = File(ruta)

                if (!archivo.isFile) {
                    null
                } else {

                    // Consultar las dimensiones originales
                    val medidas = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }

                    BitmapFactory.decodeFile(ruta, medidas)

                    if (medidas.outWidth <= 0 ||
                        medidas.outHeight <= 0
                    ) {
                        null
                    } else {

                        // Reducir imágenes grandes antes de cargarlas
                        var escala = 1

                        while (
                            medidas.outWidth / escala > 900 ||
                            medidas.outHeight / escala > 900
                        ) {
                            escala *= 2
                        }

                        BitmapFactory.decodeFile(
                            ruta,
                            BitmapFactory.Options().apply {
                                inSampleSize = escala
                            }
                        )
                    }
                }

            } catch (e: Exception) {
                null
            }
        }

        value = true to imagen
    }

    val (cargada, bitmap) = resultado

    if (!cargada) {

        Text("Cargando fotografía...")

    } else if (bitmap != null) {

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Fotografía de evidencia",
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentScale = ContentScale.Crop
        )

    } else {

        Text(
            text = "La fotografía no está disponible",
            color = MaterialTheme.colorScheme.error
        )
    }
}

