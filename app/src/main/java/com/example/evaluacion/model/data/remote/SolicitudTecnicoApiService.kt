package com.example.evaluacion.model.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path

data class DatosCambioEstadoTecnico(
    val estado: String
)

interface SolicitudTecnicoApiService {

    // Consultar las solicitudes del tecnico autenticado
    @GET("api/solicitudes/tecnico")
    suspend fun consultarAsignadas(
        @Header("Authorization") autorizacion: String
    ): List<SolicitudRemota>

    // Actualizar el estado de una solicitud asignada
    @PATCH("api/solicitudes/tecnico/{id}/estado")
    suspend fun actualizarEstado(
        @Header("Authorization") autorizacion: String,
        @Path("id") identificadorSolicitud: String,
        @Body datos: DatosCambioEstadoTecnico
    ): SolicitudRemota
}
