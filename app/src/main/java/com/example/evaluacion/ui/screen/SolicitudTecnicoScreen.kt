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
import com.example.evaluacion.viewmodel.SolicitudTecnicoViewModel
import java.util.Locale

@Composable
fun SolicitudesTecnicoScreen(
    viewModel: SolicitudTecnicoViewModel
) {

    val estado by viewModel.estado.collectAsState()

    var solicitudParaResolver by remember {
        mutableStateOf<SolicitudRemota?>(null)
    }

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
            "Mis solicitudes asignadas",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedButton(
            onClick = {
                viewModel.cargarSolicitudes()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !estado.cargando && !estado.actualizando
        ) {
            Text("Actualizar lista")
        }

        if (estado.cargando || estado.actualizando) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        estado.mensajeError?.let { mensaje ->
            Text(
                mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        estado.mensajeExito?.let { mensaje ->
            Text(mensaje)
        }

        Text(
            "Solicitudes asignadas: " +
                    estado.solicitudesAsignadas.size
        )

        if (!estado.cargando &&
            estado.solicitudesAsignadas.isEmpty() &&
            estado.mensajeError == null) {

            Text("No tienes solicitudes asignadas")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(estado.solicitudesAsignadas) { solicitud ->

                val estadoActual = solicitud.estado
                    .uppercase(Locale.ROOT)

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
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
                                    (solicitud.clienteCorreo
                                        ?: "Sin propietario")
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

                        when (estadoActual) {

                            "PENDIENTE", "RECIBIDA" -> {

                                Button(
                                    onClick = {
                                        val identificadorSolicitud =
                                            solicitud.id ?: return@Button

                                        viewModel.actualizarEstado(
                                            identificadorSolicitud,
                                            "EN_PROCESO"
                                        )
                                    },
                                    enabled = solicitud.id != null &&
                                            !estado.actualizando,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Iniciar atencion")
                                }
                            }

                            "EN_PROCESO" -> {

                                Button(
                                    onClick = {
                                        solicitudParaResolver = solicitud
                                    },
                                    enabled = solicitud.id != null &&
                                            !estado.actualizando,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Marcar como resuelta")
                                }
                            }

                            "RESUELTA" -> {

                                Text("Atencion finalizada")
                            }

                            else -> {

                                Text("Estado pendiente de revision")
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmar antes de finalizar una solicitud
    solicitudParaResolver?.let { solicitud ->

        AlertDialog(
            onDismissRequest = {
                if (!estado.actualizando) {
                    solicitudParaResolver = null
                }
            },

            title = {
                Text("Finalizar solicitud")
            },

            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "¿Marcar '${solicitud.titulo}' como RESUELTA?"
                    )

                    estado.mensajeError?.let { mensaje ->
                        Text(
                            mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {
                TextButton(
                    enabled = !estado.actualizando,
                    onClick = {
                        val identificadorSolicitud =
                            solicitud.id ?: return@TextButton

                        viewModel.actualizarEstado(
                            identificadorSolicitud,
                            "RESUELTA"
                        ) {
                            solicitudParaResolver = null
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !estado.actualizando,
                    onClick = {
                        solicitudParaResolver = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}