package com.example.evaluacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.evaluacion.model.data.remote.DatosCambioEstadoTecnico
import com.example.evaluacion.model.data.remote.RetrofitClient
import com.example.evaluacion.model.data.remote.SesionApi
import com.example.evaluacion.model.data.remote.SolicitudRemota

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import retrofit2.HttpException
import java.io.IOException

data class EstadoSolicitudesTecnico(
    val cargando: Boolean = false,
    val actualizando: Boolean = false,
    val solicitudesAsignadas: List<SolicitudRemota> = emptyList(),
    val mensajeError: String? = null,
    val mensajeExito: String? = null
)

class SolicitudTecnicoViewModel : ViewModel() {

    private val _estado = MutableStateFlow(
        EstadoSolicitudesTecnico()
    )

    val estado: StateFlow<EstadoSolicitudesTecnico> = _estado

    // Recuperar la autorizacion JWT
    private fun obtenerAutorizacion(): String? {

        val token = SesionApi.token

        return if (token.isNullOrBlank()) {
            null
        } else {
            "Bearer $token"
        }
    }

    // Traducir errores HTTP a mensajes comprensibles
    private fun describirError(error: Exception): String {

        return when (error) {

            is HttpException -> when (error.code()) {
                400 -> "El estado solicitado no es valido"
                401 -> "Sesion vencida. Inicia sesion nuevamente"
                403 -> "No tienes permisos de TECNICO"
                404 -> "La solicitud no existe o no esta asignada"
                else -> "Error del servidor: ${error.code()}"
            }

            is IOException -> "No se pudo conectar con Spring Boot"

            else -> "No fue posible completar la operacion"
        }
    }

    // Obtener solicitudes asignadas al tecnico
    fun cargarSolicitudes() {

        if (_estado.value.cargando || _estado.value.actualizando) {
            return
        }

        val autorizacion = obtenerAutorizacion()

        if (autorizacion == null) {
            _estado.update {
                it.copy(
                    solicitudesAsignadas = emptyList(),
                    mensajeError = "Inicia sesion con MongoDB"
                )
            }
            return
        }

        viewModelScope.launch {

            _estado.update {
                it.copy(
                    cargando = true,
                    mensajeError = null,
                    mensajeExito = null
                )
            }

            try {
                val solicitudesAsignadas =
                    RetrofitClient.solicitudesTecnico
                        .consultarAsignadas(autorizacion)

                _estado.update {
                    it.copy(
                        solicitudesAsignadas = solicitudesAsignadas
                    )
                }

            } catch (error: CancellationException) {
                throw error

            } catch (error: Exception) {
                _estado.update {
                    it.copy(
                        mensajeError = describirError(error)
                    )
                }

            } finally {
                _estado.update {
                    it.copy(cargando = false)
                }
            }
        }
    }

    // Actualizar una solicitud asignada
    fun actualizarEstado(
        identificadorSolicitud: String,
        estadoNuevo: String,
        alCompletar: () -> Unit = {}
    ) {

        if (_estado.value.actualizando) return

        if (estadoNuevo !in listOf("EN_PROCESO", "RESUELTA")) {
            _estado.update {
                it.copy(mensajeError = "Estado no permitido")
            }
            return
        }

        val autorizacion = obtenerAutorizacion()

        if (autorizacion == null) {
            _estado.update {
                it.copy(mensajeError = "Inicia sesion nuevamente")
            }
            return
        }

        viewModelScope.launch {

            _estado.update {
                it.copy(
                    actualizando = true,
                    mensajeError = null,
                    mensajeExito = null
                )
            }

            try {
                val solicitudActualizada =
                    RetrofitClient.solicitudesTecnico.actualizarEstado(
                        autorizacion,
                        identificadorSolicitud,
                        DatosCambioEstadoTecnico(estadoNuevo)
                    )

                _estado.update { estadoActual ->
                    estadoActual.copy(
                        solicitudesAsignadas =
                            estadoActual.solicitudesAsignadas.map {
                                    solicitud ->
                                if (solicitud.id == identificadorSolicitud) {
                                    solicitudActualizada
                                } else {
                                    solicitud
                                }
                            },
                        mensajeExito = "Estado actualizado correctamente"
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
                    it.copy(actualizando = false)
                }
            }
        }
    }
}
