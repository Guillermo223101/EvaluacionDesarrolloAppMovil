package com.example.evaluacion.model.data.remote

import retrofit2.http.GET

interface SolicitudApiService{
    // Obtener las solicitudes desde Spring Boot
    @GET("api/solicitudes")
    suspend fun listarSolicitudes(): List<SolicitudRemota>
}