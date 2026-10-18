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
    fun guardar(onListo: () -> Unit) {

        // Evita guardar dos veces mientras está procesando
        if (_estado.value.guardando) return

        // No permite guardar si hay errores
        if (!validarFormulario()) return

        val datos = _estado.value

        _estado.update {
            it.copy(
                guardando = true,
                errorGuardado = null
            )
        }

        viewModelScope.launch {

            try {

                dao.insertar(
                    SolicitudEntity(
                        titulo = datos.titulo.trim(),
                        descripcion = datos.descripcion.trim(),
                        categoria = datos.categoria
                    )
                )

                // Limpiar formulario después de guardar
                _estado.value = SolicitudUiState()

                // Avisar a la pantalla que terminó
                onListo()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {

                _estado.update {
                    it.copy(
                        errorGuardado =
                            "No se pudo guardar la solicitud"
                    )
                }

            } finally {

                _estado.update {
                    it.copy(guardando = false)
                }
            }
        }
    }
}
