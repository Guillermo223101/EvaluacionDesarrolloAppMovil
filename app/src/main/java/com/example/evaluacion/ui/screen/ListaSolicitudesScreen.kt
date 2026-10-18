
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
                            Text(
                                text = "Solicitud #${solicitud.id}"
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
                            Text(
                                text = solicitud.descripcion,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
