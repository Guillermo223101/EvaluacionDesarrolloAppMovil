package com.example.evaluacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.evaluacion.viewmodel.SolicitudRemotaViewModel

@Composable
fun SolicitudesRemotasScreen(
    viewModel: SolicitudRemotaViewModel
){

    // Observar el estado del ViewModel
    val estado by viewModel.estado.collectAsState()

    // Consultar MongoDB al ingresar a la pantalla
    LaunchedEffect(viewModel) {
        viewModel.cargarSolicitudes()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Solicitudes en MongoDB",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Actualizar manualmente la lista
        Button(
            onClick = {
                viewModel.cargarSolicitudes()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !estado.cargando
        ) {
            Text("Actualizar solicitudes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar indicador de carga
        if (estado.cargando) {
            CircularProgressIndicator()

            Text("Consultando servidor...")
        }

        // Mostrar errores de conexión
        estado.error?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Mostrar solicitudes obtenidas
        if (!estado.cargando && estado.error == null) {

            if (estado.solicitudes.isEmpty()) {

                Text(
                    text = "No hay solicitudes en MongoDB",
                    style = MaterialTheme.typography.bodyLarge
                )

            } else {

                Text(
                    text = "Solicitudes encontradas: " +
                            estado.solicitudes.size
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(estado.solicitudes) { solicitud ->

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Text(
                                    text = solicitud.titulo,
                                    style = MaterialTheme
                                        .typography.titleMedium
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Text(
                                    text = solicitud.descripcion
                                )

                                Text(
                                    text = "Estado: ${solicitud.estado}"
                                )

                                Text(
                                    text = "Prioridad: ${solicitud.prioridad}"
                                )

                                Text(
                                    text = "ID: ${solicitud.id ?: "Sin ID"}",
                                    style = MaterialTheme
                                        .typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
