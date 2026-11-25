package com.example.evaluacion.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

import com.example.evaluacion.model.data.remote.SolicitudRemota
import com.example.evaluacion.utils.EvidenciaStorage
import com.example.evaluacion.viewmodel.SolicitudRemotaViewModel
import java.util.Locale

@Composable
fun SolicitudesRemotasScreen(
    viewModel: SolicitudRemotaViewModel
) {

    val estado by viewModel.estado.collectAsState()

    // Solicitud seleccionada para cada operacion
    var solicitudParaPrioridad by remember { mutableStateOf<SolicitudRemota?>(null) }
    var solicitudParaTecnico by remember { mutableStateOf<SolicitudRemota?>(null) }
    var prioridadNueva by remember { mutableStateOf("MEDIA") }
    var correoTecnico by remember { mutableStateOf("") }
    val prioridades = listOf("BAJA", "MEDIA", "ALTA")

    LaunchedEffect(viewModel) {
        viewModel.cargarSolicitudes()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Supervision de solicitudes",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = { viewModel.cargarSolicitudes() },
            enabled = !estado.cargando && !estado.procesando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Actualizar solicitudes")
        }

        if (estado.cargando || estado.procesando) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        estado.error?.let { mensaje ->
            Text(
                mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        estado.mensajeExito?.let { mensaje ->
            Text(mensaje)
        }

        Text("Solicitudes: ${estado.solicitudes.size}")

        if (!estado.cargando && estado.solicitudes.isEmpty()) {
            Text("No hay solicitudes para supervisar")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(estado.solicitudes) { solicitud ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {

                        Text(
                            solicitud.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(solicitud.descripcion)

                        Text("Estado: ${solicitud.estado}")

                        Text("Prioridad: ${solicitud.prioridad}")

                        Text(
                            "Cliente: " +
                                    (solicitud.clienteCorreo ?: "Sin propietario")
                        )

                        Text(
                            "Tecnico: " +
                                    (solicitud.tecnicoCorreo ?: "Sin asignar")
                        )

                        // Mostrar foto de evidencia si la solicitud tiene una guardada
                        solicitud.evidenciaBase64?.let { base64 ->
                            val bitmapGuardado = remember(base64) {
                                EvidenciaStorage.base64ABitmap(base64)
                            }
                            bitmapGuardado?.let { bmp ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Evidencia de la solicitud",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                enabled = solicitud.id != null && !estado.procesando,
                                onClick = {
                                    solicitudParaPrioridad = solicitud

                                    val prioridadActual =
                                        solicitud.prioridad
                                            .uppercase(Locale.ROOT)

                                    prioridadNueva =
                                        if (prioridadActual in prioridades)
                                            prioridadActual
                                        else
                                            "MEDIA"
                                }
                            ) {
                                Text("Prioridad")
                            }

                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                enabled = solicitud.id != null &&
                                        !estado.procesando,
                                onClick = {
                                    solicitudParaTecnico = solicitud
                                    correoTecnico =
                                        solicitud.tecnicoCorreo.orEmpty()
                                }
                            ) {
                                Text("Asignar")
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogo: cambiar prioridad
    solicitudParaPrioridad?.let { solicitud ->

        AlertDialog(
            onDismissRequest = {
                if (!estado.procesando) {
                    solicitudParaPrioridad = null
                }
            },

            title = {
                Text("Cambiar prioridad")
            },

            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Text(solicitud.titulo)

                    prioridades.forEach { prioridad ->

                        Row {
                            RadioButton(
                                selected = prioridadNueva == prioridad,
                                onClick = {
                                    prioridadNueva = prioridad
                                },
                                enabled = !estado.procesando
                            )

                            TextButton(
                                onClick = {
                                    prioridadNueva = prioridad
                                },
                                enabled = !estado.procesando
                            ) {
                                Text(prioridad)
                            }
                        }
                    }

                    estado.error?.let { mensaje ->
                        Text(
                            mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {
                TextButton(
                    enabled = !estado.procesando,
                    onClick = {
                        val identificadorSolicitud =
                            solicitud.id ?: return@TextButton

                        viewModel.cambiarPrioridad(
                            identificadorSolicitud,
                            prioridadNueva
                        ) {
                            solicitudParaPrioridad = null
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !estado.procesando,
                    onClick = {
                        solicitudParaPrioridad = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialogo: asignar tecnico
    solicitudParaTecnico?.let { solicitud ->

        AlertDialog(
            onDismissRequest = {
                if (!estado.procesando) {
                    solicitudParaTecnico = null
                }
            },

            title = {
                Text("Asignar tecnico")
            },

            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Text(solicitud.titulo)

                    OutlinedTextField(
                        value = correoTecnico,
                        onValueChange = {
                            correoTecnico = it
                        },
                        label = {
                            Text("Correo del tecnico")
                        },
                        singleLine = true
                    )

                    estado.error?.let { mensaje ->
                        Text(
                            mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {
                TextButton(
                    enabled = !estado.procesando &&
                            correoTecnico.isNotBlank(),
                    onClick = {
                        val identificadorSolicitud =
                            solicitud.id ?: return@TextButton

                        viewModel.asignarTecnico(
                            identificadorSolicitud,
                            correoTecnico
                        ) {
                            solicitudParaTecnico = null
                        }
                    }
                ) {
                    Text("Asignar")
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !estado.procesando,
                    onClick = {
                        solicitudParaTecnico = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}