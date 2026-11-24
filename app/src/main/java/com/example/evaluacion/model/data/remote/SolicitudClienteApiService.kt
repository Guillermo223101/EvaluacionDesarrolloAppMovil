package com.example.evaluacion.model.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class DatosNuevaSolicitud(
    val titulo: String,
    val descripcion: String
)

interface SolicitudClienteApiService{
    //Consultar solamente mis solicitudes
    @GET("api/solicitudes/mis")
    suspend fun consultarPropias(
        @Header("Authorization") autorizacion: String
    ): List<SolicitudRemota>

    //Crear una solicitud asociada al usuario autenticado
    @POST("api/solicitudes/mis")
    suspend fun crearPropia(
        @Header("Authorization") autorizacion: String,
        @Body datos: DatosNuevaSolicitud
    ): SolicitudRemota
}
