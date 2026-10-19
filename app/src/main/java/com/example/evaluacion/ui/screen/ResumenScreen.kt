
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
    onVerSolicitudes: () -> Unit
) {

    // Obtener los datos del usuario
    val estado by viewModel.estado.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Título
        Text(
            text = "Resumen del Registro",
            style = MaterialTheme.typography.headlineMedium
        )

        // Información del usuario
        Text("Nombre: ${estado.nombre}")
        Text("Correo: ${estado.correo}")
        Text("Dirección: ${estado.direccion}")

        Text(
            "Registro de demostración: sin contraseña almacenada"
        )

        Text(
            "Términos: ${
                if (estado.aceptaTerminos)
                    "Aceptados"
                else
                    "No aceptados"
            }"
        )

        HorizontalDivider()

        Text(
            text = "Funciones de la aplicación",
            style = MaterialTheme.typography.titleMedium
        )

        // Abrir lector QR
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

        // Consultar solicitudes guardadas
        Button(
            onClick = onVerSolicitudes,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver solicitudes")
        }
    }
}
