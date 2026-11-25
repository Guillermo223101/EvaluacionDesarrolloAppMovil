package com.example.evaluacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

import com.example.evaluacion.viewmodel.SolicitudClienteViewModel
import com.example.evaluacion.model.data.remote.SolicitudRemota
import com.example.integraciones.recordarNotificadorNuevaSolicitud
import com.example.evaluacion.utils.EvidenciaStorage

import kotlinx.coroutines.launch

@Composable
fun SolicitudesClienteScreen(viewModel: SolicitudClienteViewModel) {

    val estado by viewModel.estado.collectAsState()

    var titulo by rememberSaveable {mutableStateOf("")}
    var descripcion by rememberSaveable {mutableStateOf("")}
    var solicitudParaEditar by remember { mutableStateOf<SolicitudRemota?>(null) }
    var solicitudParaEliminar by remember { mutableStateOf<SolicitudRemota?>(null) }
    var tituloEditado by remember { mutableStateOf("") }
    var descripcionEditada by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("") }
    var categoriaEditada by remember { mutableStateOf("") }
    val notificarSolicitud = recordarNotificadorNuevaSolicitud()

    val alcance = rememberCoroutineScope()
    var fotografia by remember { mutableStateOf<Bitmap?>(null) }
    var fotografiaBase64 by remember { mutableStateOf<String?>(null) }
    var procesandoFoto by remember { mutableStateOf(false) }
    var errorFoto by remember { mutableStateOf<String?>(null) }


    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { imagen ->
        if (imagen != null) {
            fotografia = imagen
            fotografiaBase64 = null
            errorFoto = null
            procesandoFoto = true
            alcance.launch {
                try {
                    val imagenCodificada = EvidenciaStorage.bitmapABase64(imagen)

                    if (imagenCodificada.length > 1_400_000) {
                        errorFoto = "La fotografía supera el tamaño permitido"
                    } else {
                        fotografiaBase64 = imagenCodificada
                    }
                } catch (error: Exception) {
                    errorFoto = "No fue posible procesar la fotografía"
                } finally {
                    procesandoFoto = false
                }
            }
        }
    }

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

        Text("Categoría")
        SelectorCategoria(
            categoriaSeleccionada = categoriaSeleccionada,
            habilitado = !estado.guardando,
            alSeleccionar = { categoriaSeleccionada = it }
        )

        Text("Evidencia fotográfica (opcional)")

        OutlinedButton(
            onClick = {
                lanzadorCamara.launch(null)
            },
            enabled = !estado.guardando && !procesandoFoto,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (fotografia == null)
                    "Tomar fotografía"
                else
                    "Cambiar fotografía"
            )
        }

        fotografia?.let { imagen ->
            Image(
                bitmap = imagen.asImageBitmap(),
                contentDescription = "Vista previa de la evidencia",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            OutlinedButton(
                onClick = {
                    fotografia = null
                    fotografiaBase64 = null
                    errorFoto = null
                },
                enabled = !estado.guardando && !procesandoFoto
            ) {
                Text("Quitar fotografía")
            }
        }

        if (procesandoFoto) {
            Text("Procesando fotografía...")
        }

        errorFoto?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }


        Button(
            onClick = {
                viewModel.crearSolicitud(
                    titulo = titulo,
                    descripcion = descripcion,
                    categoria = categoriaSeleccionada,
                    evidenciaBase64 = fotografiaBase64
                ) { codigo, tituloCreado ->

                    notificarSolicitud(codigo, tituloCreado)

                    titulo = ""
                    descripcion = ""
                    categoriaSeleccionada = ""

                    fotografia = null
                    fotografiaBase64 = null
                    errorFoto = null
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !estado.guardando &&
                    !procesandoFoto &&
                    errorFoto == null &&
                    (fotografia == null || fotografiaBase64 != null)
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
                        Text("Categoria: " +
                                (solicitud.categoria ?: "Sin categoria"))


                        // Mostrar fotografia almacenada en MongoDB
                        solicitud.evidenciaBase64
                            ?.takeIf { it.isNotBlank() }
                            ?.let { contenidoBase64 ->

                                val imagenGuardada = remember(contenidoBase64) {
                                    EvidenciaStorage.base64ABitmap(contenidoBase64)
                                }

                                imagenGuardada?.let { imagen ->
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Evidencia fotográfica")

                                    Image(
                                        bitmap = imagen.asImageBitmap(),
                                        contentDescription = "Fotografía de la solicitud",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    solicitudParaEditar = solicitud
                                    tituloEditado = solicitud.titulo
                                    descripcionEditada = solicitud.descripcion
                                    categoriaEditada = solicitud.categoria.orEmpty()
                                },
                                enabled = solicitud.id != null && !estado.guardando,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Editar")
                            }

                            OutlinedButton(
                                onClick = {
                                    solicitudParaEliminar = solicitud
                                },
                                enabled = solicitud.id != null && !estado.guardando,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Eliminar")
                            }
                        }

                    }
                }
            }
        }
    }

    solicitudParaEditar?.let { solicitud ->

        AlertDialog(
            onDismissRequest = {
                if (!estado.guardando) {
                    solicitudParaEditar = null
                }
            },

            title = {
                Text("Editar solicitud")
            },

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tituloEditado,
                        onValueChange = {
                            tituloEditado = it
                        },
                        label = { Text("Titulo") }
                    )

                    OutlinedTextField(
                        value = descripcionEditada,
                        onValueChange = {
                            descripcionEditada = it
                        },
                        label = { Text("Descripcion") }
                    )

                    SelectorCategoria(
                        categoriaSeleccionada = categoriaEditada,
                        habilitado = !estado.guardando,
                        alSeleccionar = { categoriaEditada = it }
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
                    enabled = !estado.guardando,
                    onClick = {
                        val identificadorSolicitud =
                            solicitud.id ?: return@TextButton

                        viewModel.actualizarSolicitud(
                            identificadorSolicitud,
                            tituloEditado,
                            descripcionEditada,
                            categoriaEditada.ifBlank { null }
                        ) {
                            solicitudParaEditar = null
                        }
                    }
                ) {
                    Text("Guardar cambios")
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !estado.guardando,
                    onClick = {
                        solicitudParaEditar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }


    solicitudParaEliminar?.let { solicitud ->

        AlertDialog(
            onDismissRequest = {
                if (!estado.guardando) {
                    solicitudParaEliminar = null
                }
            },

            title = {
                Text("Eliminar solicitud")
            },

            text = {
                Text(
                    "¿Eliminar '${solicitud.titulo}'? " +
                            "Esta accion no se puede deshacer."
                )
            },

            confirmButton = {
                TextButton(
                    enabled = !estado.guardando,
                    onClick = {
                        val identificadorSolicitud =
                            solicitud.id ?: return@TextButton

                        viewModel.eliminarSolicitud(
                            identificadorSolicitud
                        ) {
                            solicitudParaEliminar = null
                        }
                    }
                ) {
                    Text("Si, eliminar")
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !estado.guardando,
                    onClick = {
                        solicitudParaEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}


@Composable
private fun SelectorCategoria(
    categoriaSeleccionada: String,
    habilitado: Boolean = true,
    alSeleccionar: (String) -> Unit
) {
    var menuAbierto by remember {mutableStateOf(false)}

    val categorias = listOf("Hardware", "Software", "Redes")

    Box(modifier = Modifier.fillMaxWidth()) {

        OutlinedButton(
            onClick = { menuAbierto = true },
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                categoriaSeleccionada.ifBlank {
                    "Seleccionar categoría"
                }
            )
        }

        DropdownMenu(
            expanded = menuAbierto,
            onDismissRequest = { menuAbierto = false }
        ) {
            categorias.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria) },
                    onClick = {
                        alSeleccionar(categoria)
                        menuAbierto = false
                    }
                )
            }
        }
    }
}
