package com.example.evaluacion.model.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path

data class DatosNuevaSolicitud(
    val titulo: String,
    val descripcion: String,
    val categoria: String? = null
)

data class DatosEditarSolicitud(
    val titulo: String,
    val descripcion: String,
    val categoria: String? = null
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

    @PUT("api/solicitudes/mis/{id}")
    suspend fun actualizarPropia(
        @Header("Authorization") autorizacion: String,
        @Path("id") identificadorSolicitud: String,
        @Body datos: DatosEditarSolicitud
    ): SolicitudRemota

    @DELETE("api/solicitudes/mis/{id}")
    suspend fun eliminarPropia(
        @Header("Authorization") autorizacion: String,
        @Path("id") identificadorSolicitud: String
    ): Response<Unit>

}
