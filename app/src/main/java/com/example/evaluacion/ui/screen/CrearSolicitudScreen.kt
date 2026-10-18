package com.example.evaluacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.evaluacion.viewmodel.SolicitudViewModel

@Composable
fun CrearSolicitudScreen(
    viewModel: SolicitudViewModel,
    onGuardada: () -> Unit
) {

    // Obtener estado desde el ViewModel
    val estado by viewModel.estado.collectAsState()

    // Estado local del menú de categorías
    var menuAbierto by remember {
        mutableStateOf(false)
    }

    // Categorías disponibles
    val categorias = listOf(
        "Hardware",
        "Software",
        "Redes"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Título de la pantalla
        Text(
            text = "Nueva Solicitud",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Complete los datos de su requerimiento"
        )

        // Campo título
        OutlinedTextField(
            value = estado.titulo,
            onValueChange = viewModel::onTituloChange,
            label = { Text("Título") },
            placeholder = {
                Text("Ej: Problema con computador")
            },
            isError = estado.errores.titulo != null,
            supportingText = {
                estado.errores.titulo?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            enabled = !estado.guardando,
            modifier = Modifier.fillMaxWidth()
        )

        // Campo descripción
        OutlinedTextField(
            value = estado.descripcion,
            onValueChange = viewModel::onDescripcionChange,
            label = { Text("Descripción") },
            placeholder = {
                Text("Describa el problema")
            },
            minLines = 3,
            isError = estado.errores.descripcion != null,
            supportingText = {
                estado.errores.descripcion?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            enabled = !estado.guardando,
            modifier = Modifier.fillMaxWidth()
        )

        // Seleccionar categoría
        Text(
            text = "Categoría",
            style = MaterialTheme.typography.titleMedium
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedButton(
                onClick = {
                    menuAbierto = true
                },
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = estado.categoria.ifBlank {
                        "Seleccionar categoría"
                    }
                )
            }

            DropdownMenu(
                expanded = menuAbierto,
                onDismissRequest = {
                    menuAbierto = false
                }
            ) {

                categorias.forEach { categoria ->

                    DropdownMenuItem(
                        text = {
                            Text(categoria)
                        },
                        onClick = {
                            viewModel.onCategoriaChange(
                                categoria
                            )
                            menuAbierto = false
                        }
                    )
                }
            }
        }

        // Error de categoría
        estado.errores.categoria?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Error al guardar en Room
        estado.errorGuardado?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Botón para guardar solicitud
        Button(
            onClick = {
                viewModel.guardar(onGuardada)
            },
            enabled = !estado.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {

            if (estado.guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )

                Spacer(Modifier.width(8.dp))
            }

            Text(
                text = if (estado.guardando)
                    "Guardando..."
                else
                    "Guardar Solicitud"
            )
        }
    }
}
