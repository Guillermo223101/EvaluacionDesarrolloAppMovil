package com.example.evaluacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.evaluacion.viewmodel.SolicitudClienteViewModel

@Composable
fun SolicitudesClienteScreen(viewModel: SolicitudClienteViewModel) {

    val estado by viewModel.estado.collectAsState()

    var titulo by rememberSaveable {mutableStateOf("")}

    var descripcion by rememberSaveable {mutableStateOf("")}

    LaunchedEffect(viewModel) {viewModel.consultarSolicitudes()}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Mis solicitudes en MongoDB",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Titulo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripcion") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Button(
            onClick = {
                viewModel.crearSolicitud(
                    titulo,
                    descripcion
                ) {
                    titulo = ""
                    descripcion = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !estado.guardando
        ) {
            Text(
                if (estado.guardando)
                    "Guardando..."
                else
                    "Guardar en MongoDB"
            )
        }

        OutlinedButton(
            onClick = { viewModel.consultarSolicitudes() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !estado.cargando
        ) {
            Text("Actualizar lista")
        }

        estado.mensajeError?.let { mensaje ->
            Text(
                mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (estado.cargando) {
            CircularProgressIndicator()
        }

        Text("Solicitudes: ${estado.solicitudes.size}")

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(estado.solicitudes) { solicitud ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            solicitud.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(solicitud.descripcion)

                        Text("Estado: ${solicitud.estado}")

                        Text("Prioridad: ${solicitud.prioridad}")
                    }
                }
            }
        }
    }
}