package com.example.evaluacion.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.evaluacion.model.data.local.SolicitudEntity
import com.example.evaluacion.viewmodel.SolicitudViewModel

@Composable
fun EditarSolicitudScreen(
    solicitudId: Long,
    viewModel: SolicitudViewModel,
    onActualizada: () -> Unit,
    onVolver: () -> Unit
){
    var cargando by remember(solicitudId) {
        mutableStateOf(true)
    }

    var solicitud by remember(solicitudId) {
        mutableStateOf<SolicitudEntity?>(null)
    }

    //Recuperar los datos existentes desde ROOM
    LaunchedEffect(solicitudId) {
        solicitud = viewModel.obtenerSolicitud(solicitudId)
        cargando = false
    }

    if (cargando){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator()
        }
        return
    }

    val original = solicitud

    if (original == null){
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("La solicitud no existe")
            Button(onClick = onVolver) {
                Text("Volver")
            }
        }
        return
    }

    //Datos actuales del formulario
    var titulo by rememberSaveable(solicitudId) {
        mutableStateOf(original.titulo)
    }

    var descripcion by rememberSaveable(solicitudId) {
        mutableStateOf(original.descripcion)
    }

    var categoria by rememberSaveable(solicitudId) {
        mutableStateOf(original.categoria)
    }

    var menuAbierto by remember {
        mutableStateOf(false)
    }

    var nuevaImagen by remember {
        mutableStateOf<Uri?>(null)
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    var mostrarErrores by remember {
        mutableStateOf(false)
    }

    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    // Validaciones
    val errorTitulo = titulo.isBlank()
    val errorDescripcion = descripcion.trim().length < 10
    val errorCategoria = categoria.isBlank()

    // Selector nativo de fotografías
    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        nuevaImagen = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Editar Solicitud",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            "Código: SOL-${original.id.toString().padStart(4, '0')}"
        )

        Text("Estado: ${original.estado}")

        // Campo título
        OutlinedTextField(
            value = titulo,
            onValueChange = {
                titulo = it
                mensajeError = null
            },
            label = { Text("Título") },
            isError = mostrarErrores && errorTitulo,
            supportingText = {
                if (mostrarErrores && errorTitulo) {
                    Text("El título es obligatorio")
                }
            },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        )

        // Campo descripción
        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                mensajeError = null
            },
            label = { Text("Descripción") },
            minLines = 3,
            isError = mostrarErrores && errorDescripcion,
            supportingText = {
                if (mostrarErrores && errorDescripcion) {
                    Text("Ingresa al menos 10 caracteres")
                }
            },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        )

        // Seleccionar categoría
        Text(
            text = "Categoría",
            style = MaterialTheme.typography.titleMedium
        )

        Box(modifier = Modifier.fillMaxWidth()) {

            OutlinedButton(
                onClick = { menuAbierto = true },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    categoria.ifBlank {
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
                listOf(
                    "Hardware",
                    "Software",
                    "Redes"
                ).forEach { opcion ->

                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            categoria = opcion
                            menuAbierto = false
                        }
                    )
                }
            }
        }

        if (mostrarErrores && errorCategoria) {
            Text(
                "Selecciona una categoría",
                color = MaterialTheme.colorScheme.error
            )
        }

        HorizontalDivider()

        // Evidencia fotográfica
        Text(
            text = when {
                nuevaImagen != null ->
                    "Nueva fotografía seleccionada"

                original.evidenciaPath != null ->
                    "Se conservará la fotografía actual"

                else -> "Solicitud sin fotografía"
            }
        )

        OutlinedButton(
            onClick = {
                selectorImagen.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts
                            .PickVisualMedia.ImageOnly
                    )
                )
            },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reemplazar fotografía (opcional)")
        }

        // Mostrar errores generales
        mensajeError?.let { mensaje ->
            Text(
                mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Guardar cambios
        Button(
            onClick = {

                mostrarErrores = true

                if (
                    errorTitulo ||
                    errorDescripcion ||
                    errorCategoria
                ) {
                    return@Button
                }

                if (guardando) return@Button

                guardando = true

                viewModel.actualizar(
                    original = original,
                    titulo = titulo,
                    descripcion = descripcion,
                    categoria = categoria,
                    nuevaImagen = nuevaImagen,

                    onExito = {
                        guardando = false
                        onActualizada()
                    },

                    onError = { error ->
                        guardando = false
                        mensajeError = error
                    }
                )
            },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (guardando)
                    "Guardando cambios..."
                else
                    "Guardar cambios"
            )
        }

        TextButton(
            onClick = onVolver,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}