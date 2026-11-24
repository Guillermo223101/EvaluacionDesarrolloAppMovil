package com.example.evaluacion.model.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path

data class DatosCambioPrioridad(
    val prioridad: String
)

data class DatosAsignacionTecnico(
    val correoTecnico: String
)

interface SolicitudAdminApiService {

    @GET("api/solicitudes/admin")
    suspend fun consultarTodas(
        @Header("Authorization") autorizacion: String
    ): List<SolicitudRemota>

    @PATCH("api/solicitudes/admin/{id}/prioridad")
    suspend fun cambiarPrioridad(
        @Header("Authorization") autorizacion: String,
        @Path("id") identificadorSolicitud: String,
        @Body datos: DatosCambioPrioridad
    ): SolicitudRemota

    @PATCH("api/solicitudes/admin/{id}/tecnico")
    suspend fun asignarTecnico(
        @Header("Authorization") autorizacion: String,
        @Path("id") identificadorSolicitud: String,
        @Body datos: DatosAsignacionTecnico
    ): SolicitudRemota
}