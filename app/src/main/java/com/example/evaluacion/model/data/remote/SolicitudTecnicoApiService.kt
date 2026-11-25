package com.example.evaluacion.model.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST

data class DatosCambioEstadoTecnico(
    val estado: String
)

data class DatosNuevoAvance(
    val comentario: String
)

data class AvanceTecnicoRemoto(
    val id: String? = null,
    val solicitudId: String,
    val tecnicoCorreo: String,
    val comentario: String,
    val fechaHora: String? = null
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

    // Registrar un avance en una solicitud asignada
    @POST("api/solicitudes/tecnico/{id}/avances")
    suspend fun registrarAvance(
        @Header("Authorization") autorizacion: String,
        @Path("id") identificadorSolicitud: String,
        @Body datos: DatosNuevoAvance
    ): AvanceTecnicoRemoto

    // Consultar el historial de avances
    @GET("api/solicitudes/tecnico/{id}/avances")
    suspend fun consultarAvances(
        @Header("Authorization") autorizacion: String,
        @Path("id") identificadorSolicitud: String
    ): List<AvanceTecnicoRemoto>

}
