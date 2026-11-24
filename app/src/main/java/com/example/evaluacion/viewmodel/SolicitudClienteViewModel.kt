package com.example.evaluacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.evaluacion.model.data.remote.DatosNuevaSolicitud
import com.example.evaluacion.model.data.remote.RetrofitClient
import com.example.evaluacion.model.data.remote.SesionApi
import com.example.evaluacion.model.data.remote.SolicitudRemota
import com.example.evaluacion.model.data.remote.DatosEditarSolicitud

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import retrofit2.HttpException

data class EstadoSolicitudesCliente(
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val solicitudes: List<SolicitudRemota> = emptyList(),
    val mensajeError: String? = null
)

class SolicitudClienteViewModel : ViewModel(){
    private val _estado = MutableStateFlow(EstadoSolicitudesCliente())

    val estado = _estado.asStateFlow()

    //Obtener el encavezado usando la sesion actual
    private fun obtenerAutorizacion(): String? {
        val token = SesionApi.token

        if (token.isNullOrBlank()){
            return null
        }

        return "Bearer $token"
    }

    //Consultar solicitudes del cliente
    fun consultarSolicitudes(){
        if(_estado.value.cargando) return

        val autorizacion = obtenerAutorizacion()

        if (autorizacion == null){
            _estado.update {
                it.copy(mensajeError = "Debes iniciar sesion con MongoDB")
            }
            return
        }

        viewModelScope.launch {
            _estado.update {
                it.copy(cargando = true, mensajeError = null)
            }

            try {
                val solicitudes = RetrofitClient.solicitudesCliente
                    .consultarPropias(autorizacion)
                _estado.update {
                    it.copy(solicitudes = solicitudes)
                }
            } catch (error: CancellationException){
                throw error
            } catch (error: Exception){
                _estado.update {
                    it.copy(mensajeError = describirError(error))
                }
            } finally {
                _estado.update {
                    it.copy(cargando = false)
                }
            }
        }
    }

    // Crear solicitud utilizando el JWT
    fun crearSolicitud(
        titulo: String,
        descripcion: String,
        alGuardar: () -> Unit
    ) {

        if (_estado.value.guardando) return

        if (titulo.isBlank() || descripcion.isBlank()) {
            _estado.update {
                it.copy(mensajeError = "Completa titulo y descripcion")
            }
            return
        }

        if (titulo.length > 100 || descripcion.length > 1000) {
            _estado.update {
                it.copy(mensajeError = "El texto supera el limite permitido")
            }
            return
        }

        val autorizacion = obtenerAutorizacion()

        if (autorizacion == null) {
            _estado.update {
                it.copy(mensajeError = "Debes iniciar sesion con MongoDB")
            }
            return
        }

        viewModelScope.launch {

            _estado.update {
                it.copy(guardando = true, mensajeError = null)
            }

            try {
                RetrofitClient.solicitudesCliente.crearPropia(
                    autorizacion,
                    DatosNuevaSolicitud(
                        titulo = titulo.trim(),
                        descripcion = descripcion.trim()
                    )
                )

                val solicitudes = RetrofitClient.solicitudesCliente
                    .consultarPropias(autorizacion)

                _estado.update {
                    it.copy(solicitudes = solicitudes)
                }

                alGuardar()

            } catch (error: CancellationException) {
                throw error

            } catch (error: Exception) {
                _estado.update {
                    it.copy(mensajeError = describirError(error))
                }

            } finally {
                _estado.update {
                    it.copy(guardando = false)
                }
            }
        }
    }

    private fun describirError(error: Exception): String {
        return when (error) {
            is HttpException -> when (error.code()) {
                401 -> "Sesion vencida. Inicia sesion nuevamente"
                403 -> "No tienes permiso para esta operacion"
                else -> "Error del servidor: ${error.code()}"
            }
            else -> "No fue posible conectar con el servidor"
        }
    }


    fun actualizarSolicitud(
        identificadorSolicitud: String,
        titulo: String,
        descripcion: String,
        alCompletar: () -> Unit
    ) {
        if (_estado.value.guardando) return

        if (titulo.isBlank() || descripcion.isBlank() ||
            titulo.length > 100 || descripcion.length > 1000) {
            _estado.update {
                it.copy(mensajeError = "Revisa el titulo y la descripcion")
            }
            return
        }

        val autorizacion = obtenerAutorizacion()

        if (autorizacion == null) {
            _estado.update {
                it.copy(mensajeError = "Debes iniciar sesion")
            }
            return
        }

        viewModelScope.launch {
            _estado.update {
                it.copy(guardando = true, mensajeError = null)
            }

            try {
                val actualizada =
                    RetrofitClient.solicitudesCliente.actualizarPropia(
                        autorizacion,
                        identificadorSolicitud,
                        DatosEditarSolicitud(
                            titulo.trim(),
                            descripcion.trim()
                        )
                    )

                _estado.update { estadoActual ->
                    estadoActual.copy(
                        solicitudes = estadoActual.solicitudes.map {
                            if (it.id == identificadorSolicitud)
                                actualizada
                            else it
                        }
                    )
                }

                alCompletar()

            } catch (error: CancellationException) {
                throw error

            } catch (error: Exception) {
                _estado.update {
                    it.copy(mensajeError = describirError(error))
                }

            } finally {
                _estado.update {
                    it.copy(guardando = false)
                }
            }
        }
    }

    fun eliminarSolicitud(
        identificadorSolicitud: String,
        alCompletar: () -> Unit
    ) {
        if (_estado.value.guardando) return

        val autorizacion = obtenerAutorizacion()

        if (autorizacion == null) {
            _estado.update {
                it.copy(mensajeError = "Debes iniciar sesion")
            }
            return
        }

        viewModelScope.launch {
            _estado.update {
                it.copy(guardando = true, mensajeError = null)
            }

            try {
                val respuesta =
                    RetrofitClient.solicitudesCliente.eliminarPropia(
                        autorizacion,
                        identificadorSolicitud
                    )

                if (!respuesta.isSuccessful) {
                    throw HttpException(respuesta)
                }

                _estado.update { estadoActual ->
                    estadoActual.copy(
                        solicitudes = estadoActual.solicitudes.filterNot {
                            it.id == identificadorSolicitud
                        }
                    )
                }

                alCompletar()

            } catch (error: CancellationException) {
                throw error

            } catch (error: Exception) {
                _estado.update {
                    it.copy(mensajeError = describirError(error))
                }

            } finally {
                _estado.update {
                    it.copy(guardando = false)
                }
            }
        }
    }

}