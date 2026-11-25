package com.example.evaluacion.model.data.remote

import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.Response

interface SolicitudApiService{
    // Obtener las solicitudes desde Spring Boot
    @GET("api/solicitudes")
    suspend fun listarSolicitudes(): List<SolicitudRemota>

    //Crear solicitud en Mongo mediante Spring Boot
    @POST("api/solicitudes")
    suspend fun crearSolicitud(
        @Body solicitud: SolicitudRemota
    ): SolicitudRemota

    //Actualizar una solicitud existente
    @PUT("api/solicitudes/{id}")
    suspend fun actualizarSolicitud(
        @Path("id") id: String,
        @Body solicitud: SolicitudRemota
    ): SolicitudRemota

    //Eliminar una solicitud de  Mongo
    @DELETE("api/solicitudes/{id}")
    suspend fun eliminarSolicitud(
        @Path("id") id: String
    ): Response<Unit>
}