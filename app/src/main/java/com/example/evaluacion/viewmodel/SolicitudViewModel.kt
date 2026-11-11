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
        onListo: (codigo: String, titulo: String) -> Unit
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
            var idGuardado = 0L

            try {

                // Copiar la fotografía al almacenamiento privado
                if (imagenUri != null) {
                    rutaImagen = EvidenciaStorage.copiarImagen(
                        context = getApplication<Application>(),
                        uri = imagenUri
                    )
                }

                // Guardar solicitud y ruta de evidencia en Room
                idGuardado = dao.insertar(
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
                val codigo = "SOL-${idGuardado.toString().padStart(4, '0')}"
                onListo(codigo, datos.titulo.trim())
            }
        }
    }

    // Obtener una solicitud por su ID para editarla
    suspend fun obtenerSolicitud(id: Long): SolicitudEntity? {
        return dao.obtenerPorId(id)
    }

    // UPDATE
    fun actualizar(
        original: SolicitudEntity,
        titulo: String,
        descripcion: String,
        categoria: String,
        nuevaImagen: Uri?,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ){
        // Validaciones del formulario
        if (titulo.isBlank()) {
            onError("El titulo es obligatorio")
            return
        }
        if (descripcion.trim().length < 10) {
            onError("La descripcion debe tener al menos 10 caracteres")
            return
        }
        if (categoria.isBlank()) {
            onError("Debe seleccionar una categoria")
            return
        }

        viewModelScope.launch {
            var rutaNueva: String? = null
            var actualizado = false

            try {
                //Comprobar que la solicitud todavia existe
                val solicitudActual = dao.obtenerPorId(original.id)

                if (solicitudActual == null){
                    onError("La solicitud ya no existe")
                    return@launch
                }
                // Guardar una imagen nueva
                if (nuevaImagen != null){
                    rutaNueva = EvidenciaStorage.copiarImagen(
                        context = getApplication<Application>(),
                        uri = nuevaImagen
                    )
                }
                //Conservar la imagen anterior si no se cambio
                val rutaFinal = rutaNueva ?: solicitudActual.evidenciaPath
                // Actualizar los datos utilizando ROOM
                val filas = dao.actualizar(
                    solicitudActual.copy(
                        titulo = titulo.trim(),
                        descripcion = descripcion.trim(),
                        categoria = categoria,
                        evidenciaPath = rutaFinal
                    )
                )
                if (filas != 1){
                    onError("No se pudo encontrar la solicitud")
                    return@launch
                }
                actualizado = true
                // Si se reemplazo la imagen, se borra la anterior
                if (rutaNueva != null){
                    solicitudActual.evidenciaPath?.let { ruta ->
                        withContext(Dispatchers.IO){
                            runCatching { File(ruta).delete() }
                        }
                    }
                }
                onExito()
            } catch (e: CancellationException){
                throw e
            } catch (e: Exception){
                if (!actualizado){
                    onError("No se pudo actualizar la solicitud")
                }
            } finally {
                // Si fallo, eliminara la nueva copia sobrante
                if (!actualizado){
                    withContext(NonCancellable + Dispatchers.IO) {
                        rutaNueva?.let { ruta ->
                            File(ruta).delete()
                        }
                    }
                }
            }

        }
    }

    // DELETE
    fun eliminar(
        solicitud: SolicitudEntity,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ){
        viewModelScope.launch {
            try {
                //Eliminar el registro de ROOM
                val filasEliminadas = dao.eliminar(solicitud)

                if (filasEliminadas != 1 ){
                    onError("La solicitud ya no existe")
                    return@launch
                }
                // Eliminar la imagen asociada
                solicitud.evidenciaPath?.let { ruta ->
                    withContext(Dispatchers.IO) {
                        runCatching { File(ruta).delete() }
                    }
                }
                // Informar eliminacion exitosa
                onExito()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onError("Np se pudo eliminar la solicitud")
            }
        }
    }
}
