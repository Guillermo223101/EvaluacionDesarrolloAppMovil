
package com.example.evaluacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.evaluacion.viewmodel.UsuarioViewModel

@Composable
fun ResumenScreen(
    viewModel: UsuarioViewModel,
    onOpenQr: () -> Unit,
    onCrearSolicitud: () -> Unit,
    onVerSolicitudes: () -> Unit,
    onCerrarSesion: () -> Unit
) {

    // Datos del usuario que inició sesión
    val usuario by viewModel.usuarioAutenticado.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Resumen del Usuario",
            style = MaterialTheme.typography.headlineMedium
        )

        // Si no existe una sesión activa
        if (usuario == null) {

            Text("No has iniciado sesión")

            Button(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir al inicio de sesión")
            }

            return@Column
        }

        // Información del usuario autenticado
        Text("Nombre: ${usuario?.nombre.orEmpty()}")

        Text("Correo: ${usuario?.correo.orEmpty()}")

        Text("Dirección: ${usuario?.direccion.orEmpty()}")

        Text("Sesión iniciada correctamente")

        HorizontalDivider()

        Text(
            text = "Funciones de la aplicación",
            style = MaterialTheme.typography.titleMedium
        )

        // Lector QR
        Button(
            onClick = onOpenQr,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Abrir lector QR")
        }

        // Crear solicitud
        Button(
            onClick = onCrearSolicitud,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear nueva solicitud")
        }

        // Consultar solicitudes
        Button(
            onClick = onVerSolicitudes,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver solicitudes")
        }

        HorizontalDivider()

        // Cerrar sesión
        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}
