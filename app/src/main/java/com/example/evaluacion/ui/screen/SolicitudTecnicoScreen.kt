package com.example.evaluacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import com.example.evaluacion.model.data.remote.SolicitudRemota
import com.example.evaluacion.viewmodel.SolicitudTecnicoViewModel
import java.util.Locale

@Composable
fun SolicitudesTecnicoScreen(
    viewModel: SolicitudTecnicoViewModel
) {

    val estado by viewModel.estado.collectAsState()

    var solicitudParaResolver by remember { mutableStateOf<SolicitudRemota?>(null) }
    var solicitudParaAvances by remember { mutableStateOf<SolicitudRemota?>(null) }
    var comentarioNuevo by remember { mutableStateOf("") }

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
                            "Categoría: " +
                                    (solicitud.categoria ?: "Sin categoría")
                        )


                        Text(
                            "Cliente: " +
                                    (solicitud.clienteCorreo
                                        ?: "Sin propietario")
                        )

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

                        OutlinedButton(
                            onClick = {
                                val identificadorSolicitud =
                                    solicitud.id ?: return@OutlinedButton

                                solicitudParaAvances = solicitud
                                comentarioNuevo = ""

                                viewModel.consultarAvances(identificadorSolicitud)
                            },
                            enabled = solicitud.id != null &&
                                    !estado.guardandoAvance,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver / registrar avances")
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

    // Dialogo para consultar y registrar avances
    solicitudParaAvances?.let { solicitud ->

        AlertDialog(
            onDismissRequest = {
                if (!estado.guardandoAvance) {
                    solicitudParaAvances = null
                }
            },

            title = {
                Text("Avances del técnico")
            },

            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        solicitud.titulo,
                        style = MaterialTheme.typography.titleMedium
                    )

                    HorizontalDivider()

                    Text("Historial de avances")

                    if (estado.cargandoAvances) {
                        CircularProgressIndicator()
                    } else if (estado.avances.isEmpty() &&
                        estado.errorAvances == null) {

                        Text("Todavía no hay avances registrados")
                    }

                    estado.avances.forEach { avance ->

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(4.dp)
                            ) {
                                Text(avance.comentario)

                                Text(
                                    "Técnico: ${avance.tecnicoCorreo}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    "Fecha UTC: ${avance.fechaHora ?: "Sin fecha"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    OutlinedTextField(
                        value = comentarioNuevo,
                        onValueChange = {
                            if (it.length <= 1000) {
                                comentarioNuevo = it
                            }
                        },
                        label = { Text("Nuevo comentario") },
                        placeholder = {
                            Text("Describa el trabajo realizado")
                        },
                        minLines = 3,
                        enabled = !estado.guardandoAvance,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("${comentarioNuevo.length}/1000 caracteres")

                    estado.errorAvances?.let { mensaje ->
                        Text(
                            mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    estado.mensajeAvances?.let { mensaje ->
                        Text(mensaje)
                    }
                }
            },

            confirmButton = {
                TextButton(
                    enabled = comentarioNuevo.isNotBlank() &&
                            !estado.guardandoAvance &&
                            !estado.cargandoAvances,

                    onClick = {
                        val identificadorSolicitud =
                            solicitud.id ?: return@TextButton

                        viewModel.registrarAvance(
                            identificadorSolicitud,
                            comentarioNuevo
                        ) {
                            comentarioNuevo = ""
                        }
                    }
                ) {
                    Text(
                        if (estado.guardandoAvance)
                            "Guardando..."
                        else
                            "Registrar avance"
                    )
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !estado.guardandoAvance,
                    onClick = {
                        solicitudParaAvances = null
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}
