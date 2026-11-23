package com.example.evaluacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evaluacion.model.data.remote.RetrofitClient
import com.example.evaluacion.model.data.remote.SolicitudRemota
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EstadoSolicitudesRemotas (
    val cargando: Boolean = false, //Indica si estamos esperando respuesta
    val solicitudes: List<SolicitudRemota> = emptyList(), //Contiene los documentos obtenidos mediante Retrofit
    val error: String? = null //Informa si Spring Boot no esta disponible
)

class SolicitudRemotaViewModel : ViewModel() {

    private val _estado = MutableStateFlow(
        EstadoSolicitudesRemotas()
    )

    val estado: StateFlow<EstadoSolicitudesRemotas> = _estado

    // Consultar solicitudes desde Spring Boot
    fun cargarSolicitudes() {

        if (_estado.value.cargando) return

        viewModelScope.launch {

            _estado.value = _estado.value.copy(
                cargando = true,
                error = null
            )

            try {
                val resultado = RetrofitClient.api
                    .listarSolicitudes()

                _estado.value = EstadoSolicitudesRemotas(
                    solicitudes = resultado
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _estado.value = _estado.value.copy(
                    cargando = false,
                    error = "No se pudo conectar con el servidor"
                )
            }
        }
    }
}