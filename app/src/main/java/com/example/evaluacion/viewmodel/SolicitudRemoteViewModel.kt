package com.example.evaluacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.evaluacion.model.data.remote.DatosAsignacionTecnico
import com.example.evaluacion.model.data.remote.DatosCambioPrioridad
import com.example.evaluacion.model.data.remote.RetrofitClient
import com.example.evaluacion.model.data.remote.SesionApi
import com.example.evaluacion.model.data.remote.SolicitudRemota

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import retrofit2.HttpException
import java.io.IOException
import java.util.Locale

data class EstadoSolicitudesRemotas (
    val cargando: Boolean = false, //Indica si estamos esperando respuesta
    val solicitudes: List<SolicitudRemota> = emptyList(), //Contiene los documentos obtenidos mediante Retrofit
    val error: String? = null, //Informa si Spring Boot no esta disponible
    val procesando: Boolean = false,
    val mensajeExito: String? = null
)

class SolicitudRemotaViewModel : ViewModel() {

    private val _estado = MutableStateFlow(
        EstadoSolicitudesRemotas()
    )

    val estado: StateFlow<EstadoSolicitudesRemotas> = _estado

    // Obtener el JWT de la sesion remota
    private fun obtenerAutorizacion(): String?{
        val token = SesionApi.token

        return if (token.isNullOrBlank()){
            null
        }else{
            "Bearer $token"
        }
    }

    // Mostrar errores comprensibles
    private fun describirError(error: Exception): String {
        return when (error) {
            is HttpException -> when (error.code()) {
                400 -> "Los datos enviados no son validos"
                401 -> "Sesion vencida. Inicia sesion nuevamente"
                403 -> "No tienes permisos de administrador"
                404 -> "La solicitud ya no existe"
                else -> "Error HTTP: ${error.code()}"
            }
            is IOException -> "No se pudo conectar con Spring Boot"
            else -> "No fue posible completar la operacion"
        }
    }

    // Consultar solicitudes desde Spring Boot
    fun cargarSolicitudes() {

        if (_estado.value.cargando || _estado.value.procesando) {
            return
        }

        val autorizacion = obtenerAutorizacion()

        if (autorizacion == null){
            _estado.value = _estado.value.copy(
                error = "Inicia sesion con Mongo como ADMIN"
            )
            return
        }

        viewModelScope.launch {

            _estado.value = _estado.value.copy(
                cargando = true,
                error = null,
                mensajeExito = null
            )

            try {
                val solicitudes = RetrofitClient.solicitudesAdmin
                    .consultarTodas(autorizacion)

                _estado.value = _estado.value.copy(
                    solicitudes = solicitudes
                )

            } catch (e: CancellationException) {
                throw e
            } catch (error: Exception) {
                _estado.value = _estado.value.copy(
                    error = describirError(error)
                )
            }finally {
                _estado.value = _estado.value.copy(
                    cargando = false
                )
            }
        }
    }

    // Cambiar la prioridad de una solicitud
    fun cambiarPrioridad(
        identificadorSolicitud: String,
        prioridadNueva: String,
        alCompletar: () -> Unit
    ) {
        if (_estado.value.procesando) return

        val autorizacion = obtenerAutorizacion()

        if (autorizacion == null) {
            _estado.value = _estado.value.copy(
                error = "No existe una sesion ADMIN"
            )
            return
        }

        viewModelScope.launch {
            _estado.value = _estado.value.copy(
                procesando = true,
                error = null,
                mensajeExito = null
            )

            try {
                val solicitudActualizada =
                    RetrofitClient.solicitudesAdmin.cambiarPrioridad(
                        autorizacion,
                        identificadorSolicitud,
                        DatosCambioPrioridad(
                            prioridadNueva.uppercase(Locale.ROOT)
                        )
                    )

                _estado.value = _estado.value.copy(
                    solicitudes = _estado.value.solicitudes.map {
                        if (it.id == identificadorSolicitud)
                            solicitudActualizada
                        else it
                    },
                    mensajeExito = "Prioridad actualizada correctamente"
                )

                alCompletar()

            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _estado.value = _estado.value.copy(
                    error = describirError(error)
                )
            } finally {
                _estado.value = _estado.value.copy(
                    procesando = false
                )
            }
        }
    }

    // Asignar una solicitud a un tecnico
    fun asignarTecnico(
        identificadorSolicitud: String,
        correoTecnico: String,
        alCompletar: () -> Unit
    ) {

        if (_estado.value.procesando) return

        if (correoTecnico.isBlank()) {
            _estado.value = _estado.value.copy(
                error = "Ingresa el correo del tecnico"
            )
            return
        }

        val autorizacion = obtenerAutorizacion()

        if (autorizacion == null) {
            _estado.value = _estado.value.copy(
                error = "No existe una sesion ADMIN"
            )
            return
        }

        viewModelScope.launch {

            _estado.value = _estado.value.copy(
                procesando = true,
                error = null,
                mensajeExito = null
            )

            try {
                val solicitudActualizada =
                    RetrofitClient.solicitudesAdmin.asignarTecnico(
                        autorizacion,
                        identificadorSolicitud,
                        DatosAsignacionTecnico(
                            correoTecnico.trim()
                                .lowercase(Locale.ROOT)
                        )
                    )

                _estado.value = _estado.value.copy(
                    solicitudes = _estado.value.solicitudes.map {
                        if (it.id == identificadorSolicitud)
                            solicitudActualizada
                        else it
                    },
                    mensajeExito = "Tecnico asignado correctamente"
                )

                alCompletar()

            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _estado.value = _estado.value.copy(
                    error = describirError(error)
                )
            } finally {
                _estado.value = _estado.value.copy(
                    procesando = false
                )
            }
        }
    }
}