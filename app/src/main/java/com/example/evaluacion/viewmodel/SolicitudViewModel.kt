package com.example.evaluacion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.evaluacion.model.SolicitudError
import com.example.evaluacion.model.SolicitudUiState
import com.example.evaluacion.model.data.local.SolicitudEntity
import com.example.evaluacion.model.data.local.UsuarioDatabase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


import android.net.Uri
import com.example.evaluacion.utils.EvidenciaStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File


class SolicitudViewModel(app: Application) : AndroidViewModel(app) {

    // Acceso al DAO de solicitudes
    private val dao =
        UsuarioDatabase.getDatabase(app).solicitudDao()

    // Estado interno del formulario
    private val _estado = MutableStateFlow(SolicitudUiState())

    // Estado que observará la pantalla
    val estado: StateFlow<SolicitudUiState> = _estado

    // Flujo con todas las solicitudes registradas
    val solicitudes = dao.obtenerTodas()

    // Actualizar título y limpiar su error
    fun onTituloChange(valor: String) {
        _estado.update {
            it.copy(
                titulo = valor,
                errores = it.errores.copy(titulo = null)
            )
        }
    }

    // Actualizar descripción
    fun onDescripcionChange(valor: String) {
        _estado.update {
            it.copy(
                descripcion = valor,
                errores = it.errores.copy(descripcion = null)
            )
        }
    }

    // Actualizar categoría
    fun onCategoriaChange(valor: String) {
        _estado.update {
            it.copy(
                categoria = valor,
                errores = it.errores.copy(categoria = null)
            )
        }
    }

    // Validaciones del formulario
    fun validarFormulario(): Boolean {

        val estadoActual = _estado.value

        val errores = SolicitudError(

            titulo = if (estadoActual.titulo.isBlank())
                "El título es obligatorio"
            else null,

            descripcion =
                if (estadoActual.descripcion.trim().length < 10)
                    "Debe ingresar al menos 10 caracteres"
                else null,

            categoria = if (estadoActual.categoria.isBlank())
                "Debe seleccionar una categoría"
            else null
        )

        val hayErrores = listOfNotNull(
            errores.titulo,
            errores.descripcion,
            errores.categoria
        ).isNotEmpty()

        _estado.update {
            it.copy(errores = errores)
        }

        return !hayErrores
    }

    // Guardar solicitud mediante Room
    fun guardar(
        imagenUri: Uri?,
        onListo: () -> Unit
    ) {

        // Evitar guardar dos veces
        if (_estado.value.guardando) return

        // Comprobar los datos del formulario
        if (!validarFormulario()) return

        val datos = _estado.value

        _estado.update {
            it.copy(
                guardando = true,
                errorGuardado = null
            )
        }

        viewModelScope.launch {

            var rutaImagen: String? = null
            var guardadoCorrectamente = false

            try {

                // Copiar la fotografía al almacenamiento privado
                if (imagenUri != null) {
                    rutaImagen = EvidenciaStorage.copiarImagen(
                        context = getApplication<Application>(),
                        uri = imagenUri
                    )
                }

                // Guardar solicitud y ruta de evidencia en Room
                dao.insertar(
                    SolicitudEntity(
                        titulo = datos.titulo.trim(),
                        descripcion = datos.descripcion.trim(),
                        categoria = datos.categoria,
                        evidenciaPath = rutaImagen
                    )
                )

                guardadoCorrectamente = true

                // Limpiar el formulario
                _estado.value = SolicitudUiState()

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _estado.update {
                    it.copy(
                        errorGuardado =
                            "No se pudo guardar la solicitud o su evidencia"
                    )
                }

            } finally {

                // Si falló el guardado, eliminar la copia sobrante
                if (!guardadoCorrectamente) {
                    withContext(NonCancellable + Dispatchers.IO) {
                        rutaImagen?.let { ruta ->
                            File(ruta).delete()
                        }
                    }
                }

                _estado.update {
                    it.copy(guardando = false)
                }
            }

            // Navegar únicamente después de guardar correctamente
            if (guardadoCorrectamente) {
                onListo()
            }
        }
    }

}
